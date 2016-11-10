package org.cache.code.model;

public class BlockRequest implements Request {
    public static final int ACTION_GET_BLOCK_CONTENT = 1;
    public static final int ACTION_GET_BLOCK_HASH = 2;

    private String fileName;
    private int action;
    private int[] hashes;

    public BlockRequest(String fName, int act) {
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

    public int[] getHashes() {
        return hashes;
    }

    public void setHashes(int[] inHashes) {
        hashes = inHashes;
    }
}
