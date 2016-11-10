package org.cache.code.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.File;
import org.cache.code.model.Request;
import org.cache.code.model.Response;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

public class MyUtil {
    public static byte[] objToBytes(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        return bos.toByteArray();
    }

    public static Object bytesToObj(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        return in.readObject();
    }

    public static void closeInputStream(InputStream in) {
        if(in == null)
            return;
        try {
            in.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void closeOutputStream(OutputStream out) {
        if(out == null)
            return;
        try {
            out.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void closeBufferedReader(BufferedReader br) {
        if(br == null)
            return;
        try {
            br.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static Response sendRequest(Request request, String host, int port) throws IOException, ClassNotFoundException{
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Response response = null;
        try {
            // send the request
            Socket sock = new Socket(host, port);
            out = new ObjectOutputStream(sock.getOutputStream());
            out.writeObject(request);

            // get the response
            in = new ObjectInputStream(sock.getInputStream());
            response = (Response)in.readObject();
        }
        finally {
            MyUtil.closeInputStream(in);
            MyUtil.closeOutputStream(out);
        }
        return response;
    }

    public static void saveFile(String savePath, String fileName, byte[] fileContent) throws IOException {
        ByteArrayInputStream bis = null;
        FileOutputStream fos = null;
        try {
            bis = new ByteArrayInputStream(fileContent);
            fos = new FileOutputStream(savePath + File.separator + fileName);
            byte[] buffer = new byte[1024000];

            int readLen;
            while((readLen = bis.read(buffer, 0, buffer.length)) > 0) {
                fos.write(buffer, 0, readLen);
            }
        }
        finally {
            MyUtil.closeOutputStream(fos);
            MyUtil.closeInputStream(bis);
        }
    }

    public static byte[] readFile(String filePath, String fileName) throws IOException {
        ByteArrayOutputStream bos = null;
        FileInputStream fis = null;
        try {
            bos = new ByteArrayOutputStream();
            fis = new FileInputStream(filePath + File.separator + fileName);
            byte[] buffer = new byte[1024000];

            int readLen;
            while((readLen = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, readLen);
            }
            return bos.toByteArray();
        }
        finally {
            MyUtil.closeInputStream(fis);
            MyUtil.closeOutputStream(bos);
        }
    }

    public static List<String> getFileList(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        List<String> fileList = new ArrayList<String>();

        for(File file: listOfFiles) {
            if(file.isDirectory()) {
                continue;
            }

            fileList.add(file.getName());
        }
        return fileList;
    }

    public static byte[] getMD5(byte[] origBytes) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(origBytes);
    }

    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
    public static String getDateWithFormat() {
        return format.format(new Date());
    }

    public static boolean isEqual(byte[] bytes1, byte[] bytes2) {
        if(bytes1 == null && bytes2 == null)
            return true;

        if(bytes1 != null && bytes2 == null)
            return false;

        if(bytes1 == null && bytes2 != null)
            return false;

        if(bytes1.length != bytes2.length)
            return false;

        boolean isEqual = true;
        for(int i = 0;i < bytes1.length;i++) {
            if(bytes1[i] != bytes2[i]) {
                isEqual = false;
                break;
            }
        }
    
        return isEqual;
    }
}
