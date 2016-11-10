package org.cache.code;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.cache.code.model.FileResponse;

public class FileCache {
    private static Map<String, FileResponse> cache = new HashMap<String, FileResponse>();

    public static void clearCache() {
        cache = new HashMap<String, FileResponse>();
    }

    public static boolean containsFile(String fileName) {
        return cache.containsKey(fileName);
    }

    public static Set<String> getCachedFileSet() {
        return cache.keySet();
    }

    public static FileResponse getFileResponse(String fileName) {
        return cache.get(fileName);
    }

    public static void putFileResponse(FileResponse fileResponse) {
        cache.put(fileResponse.getFileName(), fileResponse);
    }
}
