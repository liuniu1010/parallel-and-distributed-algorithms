package org.cache.code.model;

public class FileResponse implements Response {
    private String fileName;
    private byte[] fileContent;
    private byte[] md5;

    public FileResponse(String fName) {
        fileName = fName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fName) {
        fileName = fName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] content) {
        fileContent = content;
    }

    public byte[] getMD5() {
        return md5;
    }

    public void setMD5(byte[] digestmd5) {
        md5 = digestmd5;
    }
}
