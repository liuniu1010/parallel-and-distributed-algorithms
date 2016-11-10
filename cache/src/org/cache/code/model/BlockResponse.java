package org.cache.code.model;

import java.util.Map;
import java.util.HashMap;

public class BlockResponse implements Response {
    private String fileName;
    private int[] orderedHashes;
    private Map<Integer, byte[]> blockContent;

    public BlockResponse(String fName) {
        fileName = fName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fName) {
        fileName = fName;
    }

    public int[] getOrderedHashes() {
        return orderedHashes;
    }

    public void setOrderedHashes(int[] oHashes) {
        orderedHashes = oHashes;
    }

    public Map<Integer, byte[]> getBlockContent() {
        return blockContent;
    }

    public void setBlockContent(Map<Integer, byte[]> bContent) {
        blockContent = bContent;
    }

    public byte[] getBlock(int blockHash) {
        if(blockContent == null) 
            return null;

        return blockContent.get(Integer.valueOf(blockHash));
    }

    public void setBlock(int blockHash, byte[] block) {
        if(blockContent == null) {
            blockContent = new HashMap<Integer, byte[]>();
        }

        blockContent.put(Integer.valueOf(blockHash), block);
    }

    public void removeBlock(int blockHash) {
        if(blockContent == null) {
            blockContent = new HashMap<Integer, byte[]>();
        }

        blockContent.remove(Integer.valueOf(blockHash));
    }

    public void clearBlockContent() {
        blockContent = new HashMap<Integer, byte[]>();
    }

    public boolean containsHash(int blockHash) {
        if(blockContent == null) 
            return false;

        return blockContent.containsKey(Integer.valueOf(blockHash));
    }
}
