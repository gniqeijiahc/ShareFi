package com.example.direct_share;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;


/**
 * @author shinilms
 */

public final class StartProxyThread extends Thread {

    private ServerSocket serverSocket;
    public boolean isTrue = true;
    HashMap<String, ClientInfo> connectedClients;

    public StartProxyThread(HashMap<String, ClientInfo> connectedClients) {
        this.connectedClients = connectedClients;
        try {
            this.serverSocket = new ServerSocket(Constants.PROXY_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        while (isTrue) {
            try {
                new ProxyConnectionThread(serverSocket.accept(), connectedClients);
            } catch (Exception e) {/*ignore*/}
        }
    }

    public void stopProxy() {
        isTrue = false;
        try {
            serverSocket.close();
            serverSocket = null;
        } catch (Exception e) {/*ignore*/}
    }
}
