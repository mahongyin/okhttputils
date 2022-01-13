package com.mhy.http.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

/**
 * @author mahongyin
 * Created By Mahongyin
 * Date    2020/8/25 15:33
 * 网络状态监听 USE
 * newStatus= new NetStatus();
 * newStatus.registerObserver(this,callback);
 * newStatus.unRegisterObserver(this);
 */
public class NetStatus {

    private String TAG = "NetStatus";

    public interface NetworkListener {
        void onStatus(ConnectivityStatus status);
    }

    private NetworkListener networkEvent;
    private ReactiveNetwork reactiveNetwork;
    private ConnectivityManager connectivityManager;

    public void registerObserver(Context context, NetworkListener networkEvent) {
        this.networkEvent = networkEvent;
        initNetStatus(context);
    }


    public void unRegisterObserver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
            return;
        }
        if (reactiveNetwork != null) {
            reactiveNetwork.unObserveNetworkConnectivity(context);
        }
    }


    private void initNetStatus(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            if (connectivityManager != null) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                        .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                        .build();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    connectivityManager.registerDefaultNetworkCallback(networkCallback);
//                }
                connectivityManager.registerNetworkCallback(request, networkCallback);

            }
            return;
        }
        //21以下 不适配上面 就用旧的广播方式
        reactiveNetwork = new ReactiveNetwork();
        reactiveNetwork.observeNetworkConnectivity(context, new ReactiveNetwork.NetworkEvent() {
            @Override
            public void event(ConnectivityStatus status) {
                //  ( "网络连接的类型 " + status.status);
                networkEvent.onStatus(status);
            }
        });
    }

    @TargetApi(21)
    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        /**
         * 网络可用
         */
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
            if (nc != null) {
                Log.e(TAG, "onAvailable ==>网络可用" + nc.toString());
                hasTransport(nc);
            }
        }

        /**
         * 实践中在网络连接正常的情况下，丢失数据会有回调
         * */
        @Override
        public void onLosing(Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.e(TAG, "onLosing ==>" + network.toString() + " max==>" + maxMsToLive);
        }

        /**
         * 网络不可用时调用和onAvailable成对出现
         */
        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Log.e(TAG, "onLost ==>网络已断开" + network.toString());
        }

        /**
         * 不可用
         */
        @Override
        public void onUnavailable() {
            super.onUnavailable();
            networkEvent.onStatus(ConnectivityStatus.OFFLINE);//离线
            Log.e(TAG, "onUnavailable ==>网络不可用");
        }

        /**
         * 网络能力改变
         * @param network 新连接网络
         * @param nc 新连接网络的一些能力参数
         */
        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
            super.onCapabilitiesChanged(network, nc);
            Log.e(TAG, "onCapabilitiesChanged ==>网络改变" + nc.toString());
            //太频繁
            //hasTransport(nc);
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            //连接属性更改
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.e(TAG, "onLinkPropertiesChanged ==>" + linkProperties.toString());
        }
    };

    @TargetApi(21)
    private void hasTransport(NetworkCapabilities nc) {
        if (networkEvent != null) {
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {//WIFI
                networkEvent.onStatus(ConnectivityStatus.WIFI_CONNECTED);
                Log.e(TAG, "onAvailable: 网络类型为wifi");
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {//移动数据
                networkEvent.onStatus(ConnectivityStatus.MOBILE_CONNECTED);
                Log.e(TAG, "onAvailable: 蜂窝网络");
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {//以太网
                networkEvent.onStatus(ConnectivityStatus.ETHERNET_CONNECTED);
                Log.e(TAG, "onAvailable: 以太网");
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {//蓝牙
                networkEvent.onStatus(ConnectivityStatus.BLUETOOTH_CONNECTED);
                Log.e(TAG, "onAvailable: 蓝牙网络");
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {//vpn
                networkEvent.onStatus(ConnectivityStatus.VPN_CONNECTED);
                Log.e(TAG, "onAvailable: VPN网络");
            } else {
                networkEvent.onStatus(ConnectivityStatus.UNKNOWN);
                Log.e(TAG, "onAvailable: 未知网络");
            }
        }
    }
}
