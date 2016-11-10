package org.cache.code;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.cache.code.model.Request;
import org.cache.code.model.Response;
import org.cache.code.model.FileListRequest;
import org.cache.code.model.FileListResponse;
import org.cache.code.model.FileRequest;
import org.cache.code.model.FileResponse;
import org.cache.code.model.EmptyResponse;
import org.cache.code.model.ServerErrorResponse;
import org.cache.code.exception.MyException;
import org.cache.code.util.MyUtil;
import org.cache.code.util.ConfigUtil;

public class MyFileClient {
    private Socket sock;

    private MyFileClient() {
    }

    private static MyFileClient instance = new MyFileClient();

    public static MyFileClient getInstance() {
        return instance;
    }

    public List<String> getFileList() throws MyException, IOException, ClassNotFoundException {
        FileListRequest fileListRequest = new FileListRequest();
        Response response = sendRequest(fileListRequest);
        if(response instanceof ServerErrorResponse) {
            ServerErrorResponse serverErrorResponse = (ServerErrorResponse)response;
            throw new MyException(serverErrorResponse.getErrMsg());
        }
        return ((FileListResponse)response).getFileList();
    }

    public String getFile(String fileName) throws MyException, IOException, ClassNotFoundException {
        FileRequest fileRequest = new FileRequest(fileName, FileRequest.ACTION_GET_FILE_CONTENT);
        long begin = System.currentTimeMillis();
        FileResponse fileResponse = (FileResponse)sendRequest(fileRequest);
        long end = System.currentTimeMillis();
        System.out.println("it took " + (end - begin) + " milliseconds to download this file");

        // save the file to local path
        MyUtil.saveFile(ConfigUtil.getClientFilePath(), fileResponse.getFileName(), fileResponse.getFileContent());
        return fileResponse.getFileName();
    }


    private Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        Response response = MyUtil.sendRequest(request, ConfigUtil.getCacheServerIP(), ConfigUtil.getCacheServerPort());
        if(response instanceof ServerErrorResponse) {
            throw new MyException(((ServerErrorResponse)response).getErrMsg());
        }
        return response;
    }
}
