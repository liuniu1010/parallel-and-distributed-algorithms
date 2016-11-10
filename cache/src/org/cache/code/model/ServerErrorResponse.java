package org.cache.code.model;

public class ServerErrorResponse implements Response {
    private String errMsg;

    public ServerErrorResponse(String msg) {
        errMsg = msg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String msg) {
        errMsg = msg;
    }
}
