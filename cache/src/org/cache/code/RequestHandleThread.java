package org.cache.code;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.cache.code.model.Request;
import org.cache.code.model.Response;
import org.cache.code.util.MyUtil;
import org.cache.code.util.ConfigUtil;

abstract class RequestHandleThread extends Thread {
    Socket clientSock = null;
    public RequestHandleThread(Socket socket) {
        clientSock = socket;
    }

    abstract public Response processRequest(Request request); 

    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            // get the request
            in = new ObjectInputStream(clientSock.getInputStream());
            Request request = (Request)in.readObject();

            // send back the response
            Response response = processRequest(request);
            out = new ObjectOutputStream(clientSock.getOutputStream());
            out.writeObject(response);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cne) {
            cne.printStackTrace();
        }
        finally {
            MyUtil.closeInputStream(in);
            MyUtil.closeOutputStream(out);
        }
    }
}
