package com.mhy.socket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created By Mahongyin
 * Date    2021/5/11 16:59
 * 通过端口 扫描局域网IP
 */
public class ScanIP {
    //本地IP
    private String locAddress;
    //核心线程数
    private int corePoolSize = 2;
    //最大线程数
    private int maximumPoolSize = 5;
    //线程池维护线程所允许的空闲时间
    private long keepAliveTime = 30;
    //线程池维护线程所允许的空闲时间的单位
    private TimeUnit unit = TimeUnit.SECONDS;
    //线程池所使用的缓冲队列
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
    @SuppressWarnings("unused")
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    OnSeekIpListener listener;

    public void scan(final int SERVERPORT) {
        locAddress = getLocAddrIndex();//获取本地ip前缀  192.168.1.

        //开启线程
        threadPoolExecutor.execute(new connectionServer(SERVERPORT, locAddress, 0, 100));
        threadPoolExecutor.execute(new connectionServer(SERVERPORT, locAddress, 101, 200));
        threadPoolExecutor.execute(new connectionServer(SERVERPORT, locAddress, 201, 256));
        threadPoolExecutor.shutdownNow();
        new MyThread().start();
    }
   private class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {//add
                if (threadPoolExecutor.isTerminated()) {
                    onFinish();//("结束了！");
                    break;
                }
                try {
                    Thread.sleep(180);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setOnSeekListener(OnSeekIpListener listener) {
        this.listener = listener;
    }

    private void onSeekIp(String ip) {
        if (listener != null) {
            listener.onSeekIp(ip);
        }
    }

    private void onFinish() {
        if (listener != null) {
            listener.onFinish();
        }
    }

    public interface OnSeekIpListener {
        void onSeekIp(String ip);//在子线程

        void onFinish();//子线程

    }

    /**
     * @return 获取IP前缀
     */

    private String getLocAddrIndex() {
        String str = getLocAddress();

        if (!str.equals("")) {
            return str.substring(0, str.lastIndexOf(".") + 1);
        }
        return null;
    }

    public static String getLocAddress() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip instanceof Inet4Address) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }


        return ipaddress;
    }

    public class connectionServer extends Thread {
        private int sERVERPORT;//端口号
        private String slocAddress;//网段
        private int sNum;//开始扫描点
        private int eNum;//结束扫描点

        public connectionServer(int SERVERPORT, String locAddress, int startNum, int endNum) {
            sERVERPORT = SERVERPORT;
            slocAddress = locAddress;
            sNum = startNum;
            eNum = endNum;
        }

        @Override
        public void run() {
            for (int i = sNum; i < eNum; i++) {
                String current_ip = slocAddress + i;
                if (Call(current_ip, sERVERPORT)) {
                    onSeekIp(current_ip);
                }
            }

        }
    }

    //判断端口是否打开
    public boolean Call(String ip, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 30);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }

    }


}
