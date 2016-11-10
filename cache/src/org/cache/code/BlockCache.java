package org.cache.code;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.cache.code.model.BlockResponse;

public class BlockCache {
    private static Map<String, BlockResponse> cache = new HashMap<String, BlockResponse>();

    public static void clearCache() {
        cache = new HashMap<String, BlockResponse>();
    }

    public static boolean containsFile(String fileName) {
        return cache.containsKey(fileName);
    }

    public static Set<String> getCachedFileSet() {
        return cache.keySet();
    }

    public static BlockResponse getBlockResponse(String fileName) {
        return cache.get(fileName);
    }

    public static void putBlockResponse(BlockResponse blockResponse) {
        cache.put(blockResponse.getFileName(), blockResponse);
    }
}
