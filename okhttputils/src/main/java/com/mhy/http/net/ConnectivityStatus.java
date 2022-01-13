package com.mhy.http.net;

/**
 * @author CTVIT
 */
public enum ConnectivityStatus {
    /**
     *  网络类型
     */
    UNKNOWN("unknown"),
    WIFI_CONNECTED("wifi"),
    MOBILE_CONNECTED("mobile"),
    ETHERNET_CONNECTED("ethernet"),
    BLUETOOTH_CONNECTED("bluetooth"),
    VPN_CONNECTED("vpn"),
    OFFLINE("offline");//离线

    private String status;

    ConnectivityStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ConnectivityStatus{" + "status='" + status + '\'' + '}';
    }

}
