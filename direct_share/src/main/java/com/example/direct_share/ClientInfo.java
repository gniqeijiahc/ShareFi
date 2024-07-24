package com.example.direct_share;

public class ClientInfo {
    public String ipAddress;
    public long connectionTime;
    public long dataUsage;
    public boolean isRestricted;

    public ClientInfo(String ipAddress) {
        this.ipAddress = ipAddress;
        this.connectionTime = System.currentTimeMillis();
        this.dataUsage = 0;
        this.isRestricted = false;
    }
}
