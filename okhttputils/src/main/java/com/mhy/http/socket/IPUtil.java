package com.mhy.http.socket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created By Mahongyin
 * Date    2021/5/12 1:19
 */
public class IPUtil {
    private static final String TAG = "IPUtil";

    /**
     * string类型ip转int类型ip
     */
    public static int stringIpToIntIp(String ip) {
        String[] ips = ip.split("\\.");
        if (ips.length != 4) {
            throw new IllegalArgumentException("请传入正确的ipv4地址");
        }
        StringBuilder str = new StringBuilder();
        for (String s : ips) {
            int i = Integer.parseInt(s);
            if (i > 255 || i < 0) {
                throw new IllegalArgumentException("请传入正确的ipv4地址");
            }
            String bs = Integer.toBinaryString(i);
            str.append(String.format("%8s", bs).replace(" ", "0"));
        }

        //二进制字符串转10进制,因为Integer.parseInt对负数转的问题,所以自己手写了转化的方法
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            String a = str.substring(i, i + 1);
            n = n << 1;
            if (a.equals("1")) {
                n = n | 1;
            }
        }
        return n;
    }

    /**
     * int类型ip转string类型ip
     */
    private static String intIpToStringIp(int intIp) {
        String str = Integer.toBinaryString(intIp);
        StringBuilder strIp = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int sIp = Integer.parseInt(str.substring(i * 8, (i + 1) * 8), 2);
            strIp.append(sIp).append(".");
        }
        return strIp.substring(0, strIp.length() - 1);
    }

    /**
     * 将得到的int类型的IP转换为String类型
     */
    private static String intToIp(int ip) {
        return (ip & 0xFF ) + "." +
                ((ip >> 8 ) & 0xFF) + "." +
                ((ip >> 16 ) & 0xFF) + "." +
                ( ip >> 24 & 0xFF) ;
    }
    /** 会打开wifi
     * wifi本地网络IP地址（局域网地址）
     */
    public static String getLocalIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return intToIp(wifiInfo.getIpAddress());
        }
        return "0.0.0.0";
    }

    /**移动网络获取有限网络IP地址*/
    private static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "0.0.0.0";
    }
    private static String getIpv4() {
        String hostIp = "0.0.0.0";
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6 跳过
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e( "SocketException",e.getMessage()+"");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 获取外网ip地址（非本地局域网地址）的方法
     * var returnCitySN = {"cip": "202.108.16.79", "cid": "110000", "cname": "北京市"};
     */
    private static String GetNetIp() {
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                // 从反馈的结果中提取出IP地址
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        line = jsonObject.optString("cip");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    /** 根据网络类型集成方法*/
    public static String getIpAddress(Context context) {
        if (context == null) {
            return "0.0.0.0";
        }
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo info = conManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 3/4g网络
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return getHostIp();
                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                 return getLocalIPAddress(context); // 局域网地址
//                    return GetNetIp(); // 外网地址
                } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    // 以太网有线网络
                    return getHostIp();
                }
            }
        } catch (Exception e) {
            return "0.0.0.0";
        }
        return "0.0.0.0";
    }


    /**
     * 获取网络移动和 WiFi的 IP地址
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE||info.getType()==ConnectivityManager.TYPE_ETHERNET) {//当前使用2G/3G/4G网络 有线
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intToIp(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return "0.0.0.0";
    }

    /*********************************************************************************************/
    public static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress == true) {
            String ips[] = addr.split("\\.");
            if (ips.length == 4) {
                try {
                    for (String ip : ips) {
                        if (Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        return ipAddress;
    }

    /**
     * 比较目标和本网是否同一网段
     */
    public boolean sameHIp(Context context, String IpA) {
        String lIP = IPUtil.getLocalIPAddress(context);
        lIP = lIP.substring(0, lIP.lastIndexOf("."));
        String jIP = IpA.substring(0, lIP.lastIndexOf("."));
        return jIP.equals(lIP);
    }
    /**********************************************************************************************
     * 获取IP
     *
     * @param context
     * @return
     */
    public static String getIP(Context context) {
        String ip = "0.0.0.0";
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        int type = info.getType();
        if (type == ConnectivityManager.TYPE_ETHERNET) {
            ip = getEtherNetIP();
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            ip = getWifiIP(context);
        }
        return ip;
    }

    /**
     * 获取有线地址
     *
     * @return
     */
    public static String getEtherNetIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference", "IpAddress"+ ex.toString());
        }
        return "0.0.0.0";
    }

    /**
     * 获取wifiIP地址
     *
     * @param context
     * @return
     */
    public static String getWifiIP(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiinfo = wifi.getConnectionInfo();
        int intaddr = wifiinfo.getIpAddress();
        byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff),
                                       (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff),
                                       (byte) (intaddr >> 24 & 0xff) };
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(byteaddr);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String mobileIp = addr.getHostAddress();
        return mobileIp;
    }
}
