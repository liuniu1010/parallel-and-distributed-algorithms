package org.cache.code;

import java.security.NoSuchAlgorithmException;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.nio.channels.ClosedByInterruptException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import java.net.SocketException;

import org.cache.code.util.ConfigUtil;
import org.cache.code.util.MyUtil;
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

public class MyFileServer extends Thread {
    private static boolean isRunning = false;
    private ServerSocket serverSock;
    private static MessageFeedbackIFC messageFeedbackIFC;

    private MyFileServer() {
    }

    private static MyFileServer instance = new MyFileServer();

    private static MyFileServer getInstance() {
        return instance;
    }

    public static void registerMessageFeedback(MessageFeedbackIFC mFeedbackIFC) {
        messageFeedbackIFC = mFeedbackIFC;
    }

    private static void reInit() {
        instance = new MyFileServer();
    }

    private ServerSocket getServerSocket() {
        return serverSock;
    }

    public static void startServer() {
        if(false == isRunning()) {
            reInit();
            getInstance().start();
        }
        else {
            String message = "file server is already running at port: " + ConfigUtil.getFileServerPort();
            System.out.println(message);
            feedbackMessage(message);
        }
    }

    public static void stopServer() {
        if(false == isRunning()) {
            String message = "file server is already stopped.";
            System.out.println(message);
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

    public static boolean isRunning() {
        return isRunning;
    }

    public void run() {
        try{
            serverSock = new ServerSocket(ConfigUtil.getFileServerPort());
            isRunning = true;
            String message = "file server started at port: " + ConfigUtil.getFileServerPort();
            System.out.println(message);
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
                String message = "file server stopped.";
                System.out.println(message);
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
                String message = "file server stopped.";
                System.out.println(message);
                feedbackMessage(message);
                break;
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            } 
        }
    }

    private static void feedbackMessage(String message) {
        if(messageFeedbackIFC != null) {
            messageFeedbackIFC.insertMessage(message);
        }
    }

    private void processClientSock(Socket clientSock) {
        ServerHandleThread serverHandleThread = new ServerHandleThread(clientSock);
        serverHandleThread.start();
    }
}

class ServerHandleThread extends RequestHandleThread {
    public ServerHandleThread(Socket clientSock) {
        super(clientSock);
    }

    public Response processRequest(Request request) {
        Response response = null;
        try {
            if(request instanceof FileListRequest) {
                response = processFileListRequest((FileListRequest)request);
            }
            else if(request instanceof FileRequest) {
                response = processFileRequest((FileRequest)request);
            }
            else if(request instanceof BlockRequest) {
                response = processBlockRequest((BlockRequest)request);
            }
            else {
                response = new EmptyResponse();
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            response = new ServerErrorResponse(ioe.getMessage());
        }
        catch(NoSuchAlgorithmException nae) {
            nae.printStackTrace();
            response = new ServerErrorResponse(nae.getMessage());
        }
        return response;
    }

    private String getFilePath() {
        return ConfigUtil.getServerFilePath();
    }

    private FileListResponse processFileListRequest(FileListRequest fileListRequest) {
        List<String> fileList = MyUtil.getFileList(getFilePath());
        FileListResponse fileListResponse = new FileListResponse();
        fileListResponse.setFileList(fileList);
        return fileListResponse;
    }

    private FileResponse processFileRequest(FileRequest fileRequest) throws IOException, NoSuchAlgorithmException {
        FileResponse fileResponse = new FileResponse(fileRequest.getFileName());
        byte[] fileContent = MyUtil.readFile(ConfigUtil.getServerFilePath(), fileRequest.getFileName());
        byte[] md5 = MyUtil.getMD5(fileContent);

        if(fileRequest.getAction() == FileRequest.ACTION_GET_FILE_CONTENT) {
            fileResponse.setFileContent(fileContent);
            fileResponse.setMD5(md5);
        }
        else if(fileRequest.getAction() == FileRequest.ACTION_GET_FILE_MD5) {
            fileResponse.setMD5(md5);
        }

        return fileResponse;
    }

    private BlockResponse processBlockRequest(BlockRequest blockRequest) throws IOException, NoSuchAlgorithmException {
        BlockResponse blockResponse = generateBlockResponse(blockRequest.getFileName());
       
        if(blockRequest.getAction() == BlockRequest.ACTION_GET_BLOCK_CONTENT) {
            if(blockRequest.getHashes() != null && blockRequest.getHashes().length > 0) {
                int[] hashes = blockRequest.getHashes();
                Map<Integer, byte[]> blockContent = blockResponse.getBlockContent();

                // clear content and set the need blocks only
                blockResponse.clearBlockContent();
                for(int hash: hashes) {
                    blockResponse.setBlock(hash, blockContent.get(hash));
                }
            }
        }
        else if(blockRequest.getAction() == BlockRequest.ACTION_GET_BLOCK_HASH) {
            blockResponse.setBlockContent(null);
        }

        return blockResponse;
    }

    private boolean isInArray(int[] array, int value) {
        boolean isIn = false;

        for(int v: array) {
            if(v == value){
                isIn = true;
                break;
            }
        }

        return isIn;
    }

    private BlockResponse generateBlockResponse(String fileName) throws IOException, NoSuchAlgorithmException {
        BlockResponse blockResponse = new BlockResponse(fileName);
        byte[] fileContent = MyUtil.readFile(ConfigUtil.getServerFilePath(), fileName);
        
        List<Integer> boundIndexes = getBoundIndexes(fileContent);
        int[] orderedHashes = new int[boundIndexes.size()];
        int startIndex = 0;
        for(int i = 0;i < boundIndexes.size();i++) {
            int boundIndex = boundIndexes.get(i);
            byte[] block = Arrays.copyOfRange(fileContent, startIndex, boundIndex + 1);
            startIndex = boundIndex + 1;
            int hash = caculateHashOfBlock(block);
            orderedHashes[i] = hash;
            blockResponse.setBlock(hash, block);
        }
        
        blockResponse.setOrderedHashes(orderedHashes);
        for(int hash: blockResponse.getOrderedHashes()) {
            byte[] block = blockResponse.getBlock(hash);
        }

        return blockResponse;
    }

    private int caculateHashOfBlock(byte[] block) throws NoSuchAlgorithmException {
        byte[] md5 = MyUtil.getMD5(block);
        String sHash = "";
        for(byte b: md5) {
            sHash += String.valueOf(Math.abs(b));
        }
        BigInteger bigHash = new BigInteger(sHash);

        return bigHash.intValue();
    }

    private List<Integer> getBoundIndexes(byte[] fileContent) {
        List<Integer> boundIndexes = new ArrayList<Integer>();
        for(int i = 0;i < fileContent.length;i++) {
            byte b = fileContent[i];

            if(b%100 == 1 || i == fileContent.length - 1) {
                boundIndexes.add(Integer.valueOf(i));
            }
        }

        return boundIndexes;
    }
}
