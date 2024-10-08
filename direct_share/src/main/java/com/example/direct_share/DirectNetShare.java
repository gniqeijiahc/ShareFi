package com.example.direct_share;


import android.content.Context;
import android.net.wifi.WifiSsid;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author shinilms
 */

public final class DirectNetShare {

    private final String TAG = getClass().getSimpleName();

    private Context applicationContext;
    private WifiP2pManager p2pManager;
    private WifiP2pConfig config;
    private WifiP2pManager.Channel channel;
    private GroupCreatedListener listener;
    private StartProxyThread proxyThread;
    private HashMap<String, ClientInfo> connectedClients = new HashMap<>();
    private String wifiSsid = null;
    private String wifiPassword = null;


    public DirectNetShare(Context context, GroupCreatedListener listener) {
        this.listener = listener;
        applicationContext = context.getApplicationContext();
         proxyThread = new StartProxyThread(connectedClients);
    }

    public void start() {
        initP2p(applicationContext);
        startP2pGroup();
    }

    public void stop() {
        proxyThread.stopProxy();
        proxyThread.interrupt(); //restart crash problem
        try {
            p2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "P2p removeGroup success");
                }
                @Override
                public void onFailure(int reason) {
                    Log.i(TAG, "P2p removeGroup failed. Reason : "+reason);
                }
            });
        } catch (Exception e) {/*ignore*/}
    }

    private void initP2p(Context context) {
        if (this.p2pManager == null || this.channel == null) {
            this.p2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            this.channel = this.p2pManager.initialize(applicationContext, context.getMainLooper(),
                    new WifiP2pManager.ChannelListener() {
                        public void onChannelDisconnected() {
                            Log.i(TAG, "P2p channel initialization failed");
                        }
                    });
        }
    }

    private void startP2pGroup() {
        Thread createGroupThread = new Thread(createGroupRunnable);
        createGroupThread.start();
    }

    private Runnable createGroupRunnable = new Runnable() {
        @Override
        public void run() {
            if((wifiSsid == null) ){
                wifiSsid = "DIRECT-SF-ShareFi";
            }
            if(!wifiSsid.startsWith("DIRECT-SF")){
                wifiSsid = "DIRECT-SF-" + wifiSsid;
            }
            if((wifiPassword == null)){
                wifiPassword = "12345678";
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                 config = new WifiP2pConfig.Builder()
                        .setNetworkName(wifiSsid)
                        .setPassphrase(wifiPassword)
                        .setGroupOperatingFrequency(WifiP2pConfig.GROUP_OWNER_BAND_AUTO)
                        .build();

                p2pManager.createGroup(channel, config, new WifiP2pManager.ActionListener() {
                    public void onFailure(int error) {
                        Log.i(TAG, "createGroup failed. Error code : "+error);
                    }
                    public void onSuccess() {
                        p2pManager.requestGroupInfo(channel, groupInfoListener);
                    }
                });
            }
            else{
                p2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                    public void onFailure(int error) {
                        Log.i(TAG, "createGroup failed. Error code : "+error);
                    }
                    public void onSuccess() {
                        p2pManager.requestGroupInfo(channel, groupInfoListener);
                    }
                });
            }

        }
    };

    private WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if (group != null) {
                if(group.isGroupOwner()) {
                    Log.i(TAG, "group created with ssid = "+group.getNetworkName() +
                            "\n and password = "+group.getPassphrase());
                    if(listener != null)
                        listener.onGroupCreated(group.getNetworkName(), group.getPassphrase());

                    //restart crash problem
                    try{
                        if (proxyThread.getState() != Thread.State.NEW) {
                            proxyThread = new StartProxyThread(connectedClients);
                        }
                        proxyThread.start();
                    } catch (Exception e) {
                        Log.e("ThreadError", "Thread already started", e);
                    }

                }
            } else
                p2pManager.requestGroupInfo(channel, groupInfoListener);
        }
    };
    public HashMap<String, ClientInfo>  getClientInfo() {
//        List<ClientInfo> clientInfoList = new ArrayList<>();
//        for (String key : connectedClients.keySet()) {
//            clientInfoList.add(connectedClients.get(key));
//        }
        return connectedClients;
    }
    public void setGroupCreatedListener(GroupCreatedListener listener) {
        this.listener = listener;
    }
    public interface GroupCreatedListener {
        void onGroupCreated(String ssid, String password);
    }
    public void setSsid(String ssid) {
        this.wifiSsid = ssid;
    }

    public void setPassphrase(String passphrase) {
        this.wifiPassword = passphrase;
    }

}
