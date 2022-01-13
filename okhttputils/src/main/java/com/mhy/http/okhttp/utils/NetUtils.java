package com.mhy.http.okhttp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created By Mahongyin
 * Date    2020/8/25 14:25
 */
public class NetUtils {

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    private enum NetworkType {
        // wifi
        NETWORK_WIFI,
        // 5G 网
        NETWORK_5G,
        // 4G 网
        NETWORK_4G,
        // 3G 网
        NETWORK_3G,
        // 2G 网
        NETWORK_2G,
        // 未知网络
        NETWORK_UNKNOWN,
        // 没有网络
        NETWORK_NO
    }

    /**
     * 获取当前网络类型 2G-5G
     *
     * @return 网络类型
     */
    public static NetworkType getNetworkType(Context context) {
        NetworkType netType = NetworkType.NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NetworkType.NETWORK_2G;
                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NetworkType.NETWORK_3G;
                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NetworkType.NETWORK_4G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_NR:
                        netType = NetworkType.NETWORK_5G;
                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        //  中国移动 联通 电信 三种 3G 制式
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetworkType.NETWORK_3G;
                        } else {
                            netType = NetworkType.NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NetworkType.NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    /**
     * 获取网络运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 运营商名称
     */
    public String getNetworkOperatorName(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }


    //检查网络是否连接 isConnected 网路连接
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo mNetworkInfo = getActiveNetworkInfo(context);
        if (mNetworkInfo != null) {
            //isAvailable表示网络是否可用（与当前有没有连接没关系,包含连上WiFi无网）
            return mNetworkInfo.isConnected();//只判断是否建立连接网络通道，例如局域网。和有没有网络数据没关系
        }

        return false;
    }

    //检查网络是否 isAvailable网络可用
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo mNetworkInfo = getActiveNetworkInfo(context);
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();//isAvailable表示网络是否可用（与当前有没有连接没关系,包含连上WiFi无网）
        }
        return false;
    }

    private static ConnectivityManager getConnectivityManager(Context context) {
        if (context != null) {
            return (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return null;
    }

    //23以下
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        NetworkInfo mNetworkInfo = getConnectivityManager(context).getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo;
        }
        return null;
    }

    //SDK>=23
    private static NetworkCapabilities getNetworkCapabilities(Context context) {
        ConnectivityManager cm = getConnectivityManager(context);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                return nc;
            }
        }
        return null;
    }

    /**
     * 判断移动数据是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isMobileDataEnabled(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
            if (getMobileDataEnabledMethod != null) {
                return (boolean) getMobileDataEnabledMethod.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断 wifi 是否连接
     *
     * @return {@code true}: 连接<br>{@code false}: 未连接
     */
    public static boolean isWifiConnect(Context context) {
        if (context != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NetworkInfo mWiFiNetworkInfo = getActiveNetworkInfo(context);
                if (mWiFiNetworkInfo != null) {
                    if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {//WIFI
//                        return true;
                        return mWiFiNetworkInfo.isConnected();//&&mWiFiNetworkInfo.isAvailable();
                    }
                }
            } else {//>=M
                NetworkCapabilities nc = getNetworkCapabilities(context);
                if (nc != null) {
                    if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {//WIFI
                        return true;
                    }
                }

            }

        }
        return false;
    }

    /**
     * 打开或关闭移动数据
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    public static void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断 wifi 是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开或关闭 wifi
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    public void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        }
    }


    /**
     * 是否连接状态
     */
    public static boolean isConnected(Context context) {
        if (context != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NetworkInfo mWiFiNetworkInfo = getActiveNetworkInfo(context);
                if (mWiFiNetworkInfo != null) {
                    if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {//WIFI
                        return true;
                    } else if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {//移动数据
                        return true;
                    } else if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {//以太网
                        return true;
                    }
                }
            } else {
                NetworkCapabilities nc = getNetworkCapabilities(context);
                if (nc != null) {
                    if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {//WIFI
                        return true;
                    } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {//移动数据
                        return true;
                    } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {//以太网
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断网络是否可用
     * <p>需要异步 ping，如果 ping 不通就说明网络不可用</p>
     *
     * @param ip ip 地址（自己服务器 ip），如果为空，ip 为阿里巴巴公共 ip
     * @return {@code true}: 可用<br>{@code false}: 不可用
     */
    public static boolean isAvailableByPing(String ip) {
        if (ip == null || ip.length() <= 0) {// 阿里巴巴公共 ip
            ip = "223.5.5.5";
        }
        ShellUtil.CommandResult result = ShellUtil.execCommand(String.format("ping -c 1 %s", ip), false);
        boolean ret = result.result == 0;
        if (result.successMsg != null) {
            Log.d("NetUtil", "isAvailableByPing() called" + result.successMsg);
        }
        if (result.errorMsg != null) {
            Log.d("NetUtil", "isAvailableByPing() called" + result.errorMsg);
        }
        return ret;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 通过 wifi 获取本地 IP 地址
     *
     * @return IP 地址
     */
    public String getIpAddressByWifi(Context context) {
        // 获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    /**
     * 获取 IP 地址
     *
     * @param useIPv4 是否用 IPv4
     * @return useIPv4为true返回IPv4，为false返回IPv6
     */
    public String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回 10.0.2.15
                if (!ni.isUp()) {
                    continue;
                }
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 须子线程
     * 根据域名获取IP
     *
     * @param domain 域名
     * @return IP 地址
     */
    public String getDomainAddress(String domain) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(domain);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }


}
