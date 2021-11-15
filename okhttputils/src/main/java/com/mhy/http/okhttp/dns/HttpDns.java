package com.mhy.http.okhttp.dns;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Created By Mahongyin
 * Date    2021/11/15 11:02
 */
public class HttpDns implements Dns {
    private static final Dns SYSTEM = Dns.SYSTEM;
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        Log.e("HttpDns", "lookup:" + hostname);
        //将域名转换为ip 转换过程涉及到缓存
        String ip = getIpByHost(hostname);
        if (ip != null && !ip.equals("")) {
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            Log.e("HttpDns", "inetAddresses:" + inetAddresses);
            return inetAddresses;
        }
        return SYSTEM.lookup(hostname);
    }

    private String getIpByHost(String hostname) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
        return address.getHostAddress();
    }

}
