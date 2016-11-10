package org.cache.code;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.nio.channels.ClosedByInterruptException;
import java.net.SocketException;

import org.cache.code.model.Request;
import org.cache.code.model.Response;
import org.cache.code.model.FileListRequest;
import org.cache.code.model.FileListResponse;
import org.cache.code.model.FileRequest;
import org.cache.code.model.FileResponse;
import org.cache.code.model.BlockRequest;
import org.cache.code.model.BlockResponse;
import org.cache.code.model.EmptyResponse;
import org.cache.code.model.ServerErrorResponse;
import org.cache.code.util.MyUtil;
import org.cache.code.util.ConfigUtil;
import org.cache.code.exception.MyException;

public class MyCacheServer extends Thread {
    private static boolean isRunning = false;
    private ServerSocket serverSock;
    private static MessageFeedbackIFC messageFeedbackIFC;
    private static CachedFileRefreshIFC cachedFileRefreshIFC;

    public final static int POLICY_FILE = 1;
    public final static int POLICY_BLOCK = 2;
    private static int cachePolicy = POLICY_FILE;

    public static int getCachePolicy() {
        return cachePolicy;
    }

    public static void setCachePolicy(int policy) {
        cachePolicy = policy;
        refreshCachedFile();
    }

    private MyCacheServer() {
    }

    private static MyCacheServer instance = new MyCacheServer();

    private static MyCacheServer getInstance() {
        return instance;
    }

    public static void registerMessageFeedback(MessageFeedbackIFC mFeedbackIFC) {
        messageFeedbackIFC = mFeedbackIFC;
    }

    public static void registerCachedFileRefresh(CachedFileRefreshIFC cRefreshIFC) {
        cachedFileRefreshIFC = cRefreshIFC;
    }

    private static void reInit() {
        instance = new MyCacheServer();
    }

    private ServerSocket getServerSocket() {
        return serverSock;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void startServer() {
        if(false == isRunning()) {
            reInit();
            getInstance().start();
        }
        else {
            String message = "cache server is already running at port: " + ConfigUtil.getCacheServerPort();
            feedbackMessage(message);
        }
    }

    public static void feedbackMessage(String message) {
        System.out.println(message);
        if(messageFeedbackIFC != null) {
            messageFeedbackIFC.insertMessage(message);
        }
    }

    public static void refreshCachedFile() {
        if(cachedFileRefreshIFC != null) {
            cachedFileRefreshIFC.refresh();
        }
    }

    public static void stopServer() {
        if(false == isRunning()) {
            String message = "cache server is already stopped.";
            feedbackMessage(message);
            return;
        }
        ServerSocket sock = getInstance().getServerSocket();
        if(sock != null) {
            try{
                sock.close();
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void run() {
        try{
            serverSock = new ServerSocket(ConfigUtil.getCacheServerPort());
            isRunning = true;
            String message = "cache server started at port: " + ConfigUtil.getCacheServerPort();
            feedbackMessage(message);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        while(true) {
            try{
                Socket clientSock = serverSock.accept();
                // generate a thread to handle this socket
                processClientSock(clientSock);
            }
            catch(SocketException se) {
                serverSock = null;
                isRunning = false;
                FileCache.clearCache();
                BlockCache.clearCache();
                refreshCachedFile();
                String message = "cache server stopped.";
                feedbackMessage(message);
                break;
            }
            catch(ClosedByInterruptException ce) {
                try{
                    serverSock.close();
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                serverSock = null;
                isRunning = false;
                FileCache.clearCache();
                BlockCache.clearCache();
                refreshCachedFile();
                String message = "cache server stopped.";
                feedbackMessage(message);
                break;
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    private void processClientSock(Socket clientSock) {
        CacheHandleThread cacheHandleThread = new CacheHandleThread(clientSock);
        cacheHandleThread.start();
    }
}

class CacheHandleThread extends RequestHandleThread {
    private Response sendRequest(Request request) throws IOException, ClassNotFoundException, MyException {
        Response response = MyUtil.sendRequest(request, ConfigUtil.getFileServerIP(), ConfigUtil.getFileServerPort());
        if(response instanceof ServerErrorResponse) {
            ServerErrorResponse serverErrorResponse = (ServerErrorResponse)response;
            String message = "meet server error: " + serverErrorResponse.getErrMsg();
            MyCacheServer.feedbackMessage(message); 
            throw new MyException(serverErrorResponse.getErrMsg());
        }
        return response;
    }

    public CacheHandleThread(Socket clientSock) {
        super(clientSock);
    }

    public Response processRequest(Request request) {
        Response response = null;
        String message = "-------------------------------------";
        MyCacheServer.feedbackMessage(message);
        try {
            if(request instanceof FileListRequest) {
                response = processFileListRequest((FileListRequest)request);
            }
            else if(request instanceof FileRequest) {
                response = processFileRequest((FileRequest)request);
            }
            else {
                response = new EmptyResponse();
            } 
        }
        catch(MyException myException) {
            response = new ServerErrorResponse(myException.getMessage());
        }
        MyCacheServer.refreshCachedFile();
        return response;
    }

    public Response processFileRequestWithFilePolicy(FileRequest fileRequest) throws IOException, ClassNotFoundException {
        FileResponse responseToReturn = null;
        if(FileCache.containsFile(fileRequest.getFileName())) {
            String message = "cache contains this file: " + fileRequest.getFileName();
            MyCacheServer.feedbackMessage(message);
            FileResponse cachedResponse = FileCache.getFileResponse(fileRequest.getFileName());

            FileRequest md5Request = new FileRequest(fileRequest.getFileName(), FileRequest.ACTION_GET_FILE_MD5);
            FileResponse md5Response = (FileResponse)sendRequest(md5Request);

            // compare
            if(MyUtil.isEqual(cachedResponse.getMD5(), md5Response.getMD5())) {
                // the md5 is the same, return the cached file
                message = "the md5 is the same as file server, it didn't changed";
                MyCacheServer.feedbackMessage(message);
                message = "respone: cached file " + fileRequest.getFileName();
                MyCacheServer.feedbackMessage(message);
                responseToReturn = cachedResponse;
            }
            else {
                // the md5 is not the same, request the whole file
                message = "the md5 is different with server's, retrieve the file from file server again...";
                MyCacheServer.feedbackMessage(message);
                FileRequest contentRequest = new FileRequest(fileRequest.getFileName(), FileRequest.ACTION_GET_FILE_CONTENT);
                responseToReturn = (FileResponse)sendRequest(contentRequest);
                // save the new file in cache
                message = "respone: file " + fileRequest.getFileName() + " downloaded from file server";
                MyCacheServer.feedbackMessage(message);
                message = "save the new file content into cache";
                MyCacheServer.feedbackMessage(message);
                FileCache.putFileResponse(responseToReturn);
            }
        }
        else {
            String message = "cache doesn't contain this file: " + fileRequest.getFileName();
            MyCacheServer.feedbackMessage(message);

            message = "retrieve file " + fileRequest.getFileName() + " from file server...";
            MyCacheServer.feedbackMessage(message);
            FileRequest contentRequest = new FileRequest(fileRequest.getFileName(), FileRequest.ACTION_GET_FILE_CONTENT);
            responseToReturn = (FileResponse)sendRequest(contentRequest);
            message = "response: file " + fileRequest.getFileName() + " downloaded from file server";
            MyCacheServer.feedbackMessage(message);
            message = "save the file content into cache";
            MyCacheServer.feedbackMessage(message);
            FileCache.putFileResponse(responseToReturn);
        }
        return responseToReturn;
    }

    public Response processFileRequestWithBlockPolicy(FileRequest fileRequest) throws IOException, ClassNotFoundException {
        FileResponse fileResponseToReturn = null;
        String message;
        if(BlockCache.containsFile(fileRequest.getFileName())) {
            message = "the file is cached, check if it has been changed...";
            MyCacheServer.feedbackMessage(message);
            // send request to file server to get the hash of all blocks of this file
            BlockRequest hashRequest = new BlockRequest(fileRequest.getFileName(), BlockRequest.ACTION_GET_BLOCK_HASH);
            BlockResponse hashResponse = (BlockResponse)sendRequest(hashRequest);
            // compare the returned blockResponse with the cached Blockresponse
            BlockResponse cachedResponse = BlockCache.getBlockResponse(fileRequest.getFileName());
            int[] orderedHashes = hashResponse.getOrderedHashes();
            int newBlockNumber = 0;
            for(int hash: orderedHashes) {
                if(!cachedResponse.containsHash(hash)) {
                    newBlockNumber++;
                }
            }
            if(newBlockNumber == 0) {
                // no new block needed, just assemble blocks from cache
                message = "all blocks of this file are not changed, assemble the blocks in cache to return to client";
                MyCacheServer.feedbackMessage(message);
                message = "response: 100% of file " + fileRequest.getFileName() + " was constructed with the cached Data";
                MyCacheServer.feedbackMessage(message);
                cachedResponse.setOrderedHashes(orderedHashes);  // the hash order might changed, so it should update it
                fileResponseToReturn = transBlockResponseToFileResponse(cachedResponse);
            }
            else {
                // some block changed, request the changed or new blockes
                int totalBlockNumber = orderedHashes.length;
                message = "total block number of " + fileRequest.getFileName() + " is " + totalBlockNumber;
                MyCacheServer.feedbackMessage(message);
                message = "There are " + newBlockNumber + " blocks are changed, try to get the changed blocks from file server...";
                MyCacheServer.feedbackMessage(message);
                message = "response: " + (int)(((float)(totalBlockNumber - newBlockNumber)*100)/totalBlockNumber) + "% of file " + fileRequest.getFileName() + " was constructed with the cached data";
                MyCacheServer.feedbackMessage(message);
                BlockRequest blockRequest = new BlockRequest(fileRequest.getFileName(), BlockRequest.ACTION_GET_BLOCK_CONTENT);
                int[] hashes = new int[newBlockNumber];
                int index = 0;
                for(int hash: orderedHashes) {
                    if(cachedResponse.containsHash(hash)) {
                        continue;
                    }

                    hashes[index] = hash;
                    index++;
                }
                blockRequest.setHashes(hashes); // indicate what blocks needed
                BlockResponse blockResponse = (BlockResponse)sendRequest(blockRequest);
                // merge the return blockRespose into cached Response
                cachedResponse.setOrderedHashes(blockResponse.getOrderedHashes());
                for(int hash: hashes) {
                     cachedResponse.setBlock(hash, blockResponse.getBlock(hash));
                }
                fileResponseToReturn = transBlockResponseToFileResponse(cachedResponse);
            }
        }
        else {
            // send request to file server to get the file with all blocks
            message = "The file is not in cache, try to get it from file server...";
            MyCacheServer.feedbackMessage(message);
            BlockRequest contentRequest = new BlockRequest(fileRequest.getFileName(), BlockRequest.ACTION_GET_BLOCK_CONTENT);
            BlockResponse contentResponse = (BlockResponse)sendRequest(contentRequest);
            // transform blockresponse to fileresponse
            fileResponseToReturn = transBlockResponseToFileResponse(contentResponse);
            // save into cache
            message = "response: file " + fileRequest.getFileName() + " was downloaded from file server and returned to client";
            MyCacheServer.feedbackMessage(message);
            BlockCache.putBlockResponse(contentResponse);
        }
        return fileResponseToReturn;
    }

    private FileResponse transBlockResponseToFileResponse(BlockResponse blockResponse) {
        FileResponse fileResponse = new FileResponse(blockResponse.getFileName());
        int[] orderedHashes = blockResponse.getOrderedHashes();
        int totalLength = 0;
        for(int hash: orderedHashes) {
            byte[] block = blockResponse.getBlock(hash);
            totalLength += block.length;
        }

        int index = 0;
        byte[] wholeContent = new byte[totalLength];
        for(int hash: orderedHashes) {
            byte[] block = blockResponse.getBlock(hash);
            for(byte b: block) {
                wholeContent[index] = b;
                index++;
            }
        }

        fileResponse.setFileContent(wholeContent);
        return fileResponse;
    }

    public Response processFileRequest(FileRequest fileRequest) {
        String message = "user request file: " + fileRequest.getFileName() + " at " + MyUtil.getDateWithFormat();
        MyCacheServer.feedbackMessage(message);

        FileResponse responseToReturn = null;
        try {
            if(MyCacheServer.getCachePolicy() == MyCacheServer.POLICY_FILE) {
                responseToReturn = (FileResponse)processFileRequestWithFilePolicy(fileRequest);
            }
            else if(MyCacheServer.getCachePolicy() == MyCacheServer.POLICY_BLOCK) {
                responseToReturn = (FileResponse)processFileRequestWithBlockPolicy(fileRequest);
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            MyCacheServer.feedbackMessage(ioe.getMessage());
            ServerErrorResponse response = new ServerErrorResponse(ioe.getMessage());
            return response;
        }
        catch(ClassNotFoundException cne) {
            cne.printStackTrace();
            MyCacheServer.feedbackMessage(cne.getMessage());
            ServerErrorResponse response = new ServerErrorResponse(cne.getMessage());
            return response;
        }

        return responseToReturn;
    }

    public Response processFileListRequest(FileListRequest fileListRequest) {
        try {
            String message = "user request file list at " + MyUtil.getDateWithFormat();
            MyCacheServer.feedbackMessage(message);
            return sendRequest(fileListRequest);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            ServerErrorResponse response = new ServerErrorResponse(ioe.getMessage());
            return response;
        }
        catch(ClassNotFoundException cne) {
            cne.printStackTrace();
            ServerErrorResponse response = new ServerErrorResponse(cne.getMessage());
            return response;
        }
    }
}
