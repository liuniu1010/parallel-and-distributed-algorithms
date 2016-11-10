package org.cache.code.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class ConfigUtil {
    private final static String CONSTANT_FILE_SERVER_IP = "fileServerIP";
    private final static String CONSTANT_FILE_SERVER_PORT="fileServerPort";
    private final static String CONSTANT_SERVER_FILE_PATH = "serverFilePath";
    private final static String CONSTANT_CACHE_SERVER_IP = "cacheServerIP";
    private final static String CONSTANT_CACHE_SERVER_PORT = "cacheServerPort";
    private final static String CONSTANT_CLIENT_FILE_PATH = "clientFilePath";

    public static String getFileServerIP() {
        return getConfigCache().get(CONSTANT_FILE_SERVER_IP);
    }

    public static int getFileServerPort() {
        return Integer.valueOf(getConfigCache().get(CONSTANT_FILE_SERVER_PORT));
    }

    public static String getCacheServerIP() {
        return getConfigCache().get(CONSTANT_CACHE_SERVER_IP);
    }

    public static int getCacheServerPort() {
        return Integer.valueOf(getConfigCache().get(CONSTANT_CACHE_SERVER_PORT));
    }

    public static String getServerFilePath() {
        return getConfigCache().get(CONSTANT_SERVER_FILE_PATH);
    }

    public static String getClientFilePath() {
        return getConfigCache().get(CONSTANT_CLIENT_FILE_PATH);
    }

    public static void clearConfigCache() {
        configList = null;
        configCache = null;
    }

    private static List<String> configList = null;
    private static List<String> getConfigList() {
        if(configList == null) {
            String exePath = System.getProperty("user.dir");
            String confPath = exePath + File.separator + ".." + File.separator + "conf";
            String confFile = confPath + File.separator + "setting.conf";
            readFile(confFile);
        }
        return configList;
    }

    public static List<String> getTextFileContent(String fileName) {
        BufferedReader br = null;
        List<String> contentList = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = null;
            while((line = br.readLine()) != null) {
                contentList.add(line);
            }
        } 
        catch(FileNotFoundException fne) {
            fne.printStackTrace();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            MyUtil.closeBufferedReader(br);
        }
        return contentList;
    }

    private static void readFile(String fileName) {
        configList = getTextFileContent(fileName);
    }

    private static Map<String, String> configCache = null;
    private static Map<String, String> getConfigCache() {
        if(configCache != null) 
            return configCache;

        configList = getConfigList();
        configCache = new HashMap<String, String>();
        for(String line: configList) {
            String[] parts = line.split("=");
            if(parts.length == 2) {
                configCache.put(parts[0].trim(), parts[1].trim());
            }
        }

        return configCache;
    }
}
