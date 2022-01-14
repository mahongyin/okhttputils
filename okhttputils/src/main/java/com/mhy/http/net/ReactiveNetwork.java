package com.mhy.http.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import com.mhy.http.okhttp.utils.NetUtils;

/**
 * 21以下使用
 * ReactiveNetwork reactiveNetwork = new ReactiveNetwork() ;
 * reactiveNetwork.setNetworkEvent(this,new ReactiveNetwork.NetworkEvent() {
 *
 * @Override public void event(ConnectivityStatus status) {
 * textView .setText( "网络连接的类型 " + status.status);
 * }
 * });
 * reactiveNetwork.observeNetworkConnectivity( this ) ;
 * reactiveNetwork.unObserveNetworkConnectivity( this ) ;
 */
class ReactiveNetwork {

    interface NetworkEvent {
        void event(ConnectivityStatus status);
    }

    private ConnectivityStatus status = ConnectivityStatus.OFFLINE;

    /**
     * 注册观察网络类型
     */
    private BroadcastReceiver receiver;

    public void observeNetworkConnectivity(final Context context, final NetworkEvent networkEvent) {
        final IntentFilter filter = new IntentFilter();
        //网络变化网络的连接（包括wifi和移动网络） //动态注册，此广播只能动态注册才能接收到
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {//网络变更时判断一下当前网络状态
                Log.e("NetworkEvent", "onReceive: " + intent.getAction());
                final ConnectivityStatus newStatus = getConnectivityStatus(context, intent);
                // 我们需要在下面执行检查, 因为脱机之后，onReceive（）被调用了两次
                if (newStatus != status) {
                    status = newStatus;
                    if (networkEvent != null) {
                        networkEvent.event(newStatus);
                    }
                }
            }
        };
        context.registerReceiver(receiver, filter);
    }

    /**
     * 取消广播接收
     *
     * @param context
     */
    public void unObserveNetworkConnectivity(final Context context) {
        if (null != receiver) {
            context.unregisterReceiver(receiver);
        }
    }

    /**
     * 获取当前的网络连接状态
     *
     * @param context Application Context is recommended here
     * @return ConnectivityStatus, 可以是WIFI_CONNECTED，MOBILE_CONNECTED或OFFLINE
     */
    private ConnectivityStatus getConnectivityStatus(final Context context, Intent intent) {

        // 监听wifi的打开与关闭，与wifi的连接无关
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {//wifi关闭
                Log.d("netstatus", "wifi已关闭");
            } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {//wifi开启
                Log.d("netstatus", "wifi已开启");
            } else if (wifiState == WifiManager.WIFI_STATE_ENABLING) {//wifi开启中
                Log.d("netstatus", "wifi开启中");
            } else if (wifiState == WifiManager.WIFI_STATE_DISABLING) {//wifi关闭中
                Log.d("netstatus", "wifi关闭中");
            }
        }
        // 监听wifi的连接状态即是否连上了一个有效无线路由
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelableExtra != null) {
                Log.d("netstatus", "wifi parcelableExtra不为空");
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {//已连接网络
                    Log.d("netstatus", "wifi 已连接网络");
                    if (networkInfo.isAvailable()) {//并且网络可用
                        Log.d("netstatus", "wifi 已连接网络，并且可用");
                    } else {//并且网络不可用
                        Log.d("netstatus", "wifi 已连接网络，但不可用");
                    }
                } else {//网络未连接
                    Log.d("netstatus", "wifi 未连接网络");
                }
            } else {
                Log.d("netstatus", "wifi parcelableExtra为空");
            }
        }
        // 监听网络连接，总网络判断，即包括wifi和移动网络的监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            //连上的网络类型判断：wifi还是移动网络
            if (networkInfo == null) {
                Log.e("netstatus", "总网络 info为空");
                return ConnectivityStatus.OFFLINE;
            }
            //具体连接状态判断
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {//已连接网络
                Log.e("netstatus", "总网络 已连接网络");
                if (networkInfo.isAvailable()) {//并且网络可用
                    Log.d("netstatus", "总网络 已连接网络，并且可用");
                } else {//并且网络不可用
                    Log.d("netstatus", "总网络 已连接网络，但不可用");
                }
                //区分连接类型
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.d("netstatus", "总网络 连接的是wifi网络");
                    return ConnectivityStatus.WIFI_CONNECTED;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Log.d("netstatus", "总网络 连接的是移动网络");
                    return ConnectivityStatus.MOBILE_CONNECTED;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {//以太网 和移动网获取ip的方式一致
                    Log.d("netstatus", "总网络 连接的是以太网");
                    return ConnectivityStatus.ETHERNET_CONNECTED;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                    return ConnectivityStatus.BLUETOOTH_CONNECTED;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_VPN) {
                    return ConnectivityStatus.VPN_CONNECTED;
                } else {
                    return ConnectivityStatus.UNKNOWN;
                }
            } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {//网络未连接
                Log.e("netstatus", "总网络 未连接网络");
                return ConnectivityStatus.OFFLINE;
            }

        }
        return ConnectivityStatus.OFFLINE;
    }

}
