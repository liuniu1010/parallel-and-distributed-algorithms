package org.cache.code.model;

import java.util.List;
import java.util.ArrayList;

public class FileListResponse implements Response {
    private List<String> fileList;
    public List<String> getFileList() {
        return fileList;
    }

    public void setFileList(List<String> fList) {
        fileList = fList;
    }

    public String toString() {
        String str = "";
        for(String file: fileList) {
            if(str.equals("")) {
                str += file;
            }
            else {
                str += ", " + file;
            }
        }

        return str;
    }
}
