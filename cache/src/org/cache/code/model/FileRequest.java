package org.cache.code.model;

public class FileRequest implements Request {
    public static final int ACTION_GET_FILE_CONTENT = 1;
    public static final int ACTION_GET_FILE_MD5 = 2;

    private String fileName;
    private int action;

    public FileRequest(String fName, int act) {
        fileName = fName;
        action = act;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fName) {
        fileName = fName;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int act) {
        action = act;
    }
}
