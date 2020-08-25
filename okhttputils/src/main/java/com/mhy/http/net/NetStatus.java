package com.mhy.http.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.mhy.http.okhttp.utils.NetUtils;

/**
 * Created By Mahongyin
 * Date    2020/8/25 15:33
 */
public class NetStatus {
    private NetworkListener networkEvent;

    public void setNetworkListener(Context context,NetworkListener networkEvent) {
        initNetStatus(context);
        this.networkEvent = networkEvent;
    }

    public interface NetworkListener {
        void onStatus(ConnectivityStatus status);
    }
private ConnectivityManager connectivityManager;
    private void initNetStatus(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            if (connectivityManager != null) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();

                NetworkRequest request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .build();

                connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                    /**
                     * 网络可用的回调连接成功
                     * */
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        Log.e("net", "onAvailable ==>网络已连接" + network.toString());
                        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                        if (nc != null) {
                            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {//WIFI
                                networkEvent.onStatus(ConnectivityStatus.WIFI_CONNECTED);
                            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {//移动数据
                                networkEvent.onStatus(ConnectivityStatus.MOBILE_CONNECTED);
                            }else {
                                networkEvent.onStatus(ConnectivityStatus.UNKNOWN);
                            }
                        }
                    }

                    /**
                     * 实践中在网络连接正常的情况下，丢失数据会有回调
                     * */
                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Log.e("net", "onLosing ==>" + network.toString() + " max==>" + maxMsToLive);

                    }

                    /**
                     * 网络不可用时调用和onAvailable成对出现
                     */
                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        Log.e("net", "onLost ==>网络已断开" + network.toString());
                        networkEvent.onStatus(ConnectivityStatus.OFFLINE);
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                       // Log.e("net", "onUnavailable ==>");
                    }

                    /**
                     * 字面直接能理解
                     * @param network 新连接网络
                     * @param networkCapabilities 新连接网络的一些能力参数
                     */
                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
//                        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
//                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                                Log.e("net", "onCapabilitiesChanged: 网络类型为wifi");
//                                networkEvent.onStatus(ConnectivityStatus.WIFI_CONNECTED);
//                            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                                Log.e("net", "onCapabilitiesChanged: 蜂窝网络");
//                                networkEvent.onStatus(ConnectivityStatus.MOBILE_CONNECTED);
//                            } else {
//                                Log.e("net", "onCapabilitiesChanged: 其他网络");
//                                networkEvent.onStatus(ConnectivityStatus.UNKNOWN);
//
//                            }
//                        }
                      //  Log.e("net", "onCapabilitiesChanged ==>" + networkCapabilities.toString());
                    }

//                    @Override
//                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
//                        super.onLinkPropertiesChanged(network, linkProperties);
//                        Log.e("net", "onLinkPropertiesChanged ==>" + linkProperties.toString());
//                    }
                });
            }
        } else {

            ReactiveNetwork reactiveNetwork = new ReactiveNetwork();
            reactiveNetwork.setNetworkEvent(new ReactiveNetwork.NetworkEvent() {
                @Override
                public void event(ConnectivityStatus status) {
                //  ( "网络连接的类型 " + status.status);
                    networkEvent.onStatus(status);
                }
            });
            reactiveNetwork.observeNetworkConnectivity(context);
        }

    }
}
