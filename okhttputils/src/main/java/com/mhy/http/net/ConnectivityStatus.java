package com.mhy.http.net;

/**
 * @author CTVIT
 */
public enum ConnectivityStatus {
    /**
     *
     */
    UNKNOWN("unknown"),
    WIFI_CONNECTED("wifi"),
    MOBILE_CONNECTED("mobile"),
    OFFLINE("offline");

    public final String status;

    ConnectivityStatus(final String status) {
        this.status = status;
    }

    @Override public String toString() {
        return "ConnectivityStatus{" + "status='" + status + '\'' + '}';
    }

}
