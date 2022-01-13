package com.mhy.http.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/** 21以下使用
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

    private ConnectivityStatus status = ConnectivityStatus.UNKNOWN;

    /**
     * 注册观察网络类型
     */
    private BroadcastReceiver receiver;

    public void observeNetworkConnectivity(final Context context, final NetworkEvent networkEvent) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//网络变化
         receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("NetworkEvent", "onReceive: "+intent.getDataString());
                final ConnectivityStatus newStatus = getConnectivityStatus(context);
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
     * @param context
     */
    public void unObserveNetworkConnectivity(final Context context) {
        if (null!=receiver) {
            context.unregisterReceiver(receiver);
        }
    }

    /**
     * 获取当前的网络连接状态
     *
     * @param context Application Context is recommended here
     * @return ConnectivityStatus, 可以是WIFI_CONNECTED，MOBILE_CONNECTED或OFFLINE
     */
    private ConnectivityStatus getConnectivityStatus(final Context context) {
        final String service = Context.CONNECTIVITY_SERVICE;
        final ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(service);
        final NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return ConnectivityStatus.OFFLINE;
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return ConnectivityStatus.WIFI_CONNECTED;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return ConnectivityStatus.MOBILE_CONNECTED;
        }else if (networkInfo.getType()==ConnectivityManager.TYPE_ETHERNET){//以太网 和移动网获取ip的方式一致
            return ConnectivityStatus.ETHERNET_CONNECTED;
        }else if (networkInfo.getType()==ConnectivityManager.TYPE_BLUETOOTH){
            return ConnectivityStatus.BLUETOOTH_CONNECTED;
        }else if (networkInfo.getType()==ConnectivityManager.TYPE_VPN){
            return ConnectivityStatus.VPN_CONNECTED;
        }
        return ConnectivityStatus.UNKNOWN;
    }

}
