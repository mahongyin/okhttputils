package com.mhy.http.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.获得远程服务器的IP 地址.
 * InetAddress inetAddress = socket.getInetAddress();
 * 2.获得远程服务器的端口.
 * int port = socket.getPort();
 * 3. 获得客户本地的IP 地址.
 * InetAddress localAddress = socket.getLocalAddress();
 * 4.获得客户本地的端口.
 * int localPort = socket.getLocalPort();
 * 5.获取本地的地址和端口号
 * SocketAddress localSocketAddress = socket.getLocalSocketAddress();
 * 6.获得远程的地址和端口号
 * SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
 */
public class SocketServer {
    // 管理连接到服务器中的手机类
    private static ServerThread serverThread = null;
    private static int sum = 0;
    private static Map<String, Socket> clientMap = new HashMap<>();
    private static List<String> clientList = new ArrayList<>();

    private static class ServerThread implements Runnable {
        private ServerSocket server;
        private int port = 8899;
        private boolean isExit = false;// 一个boolean类型的判断 默认是退出状态false

        // 构造方法初始化
        public ServerThread() {
            try {
                server = new ServerSocket(port);
                Log.e("启动server","端口号：" + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                while (!isExit) {
                    // 等待连接
                    Log.e("Socket","等待手机的连接中... ...");
                    final Socket socket = server.accept();
                    Log.e("Socket","获取的手机IP地址及端口号：" + socket.getRemoteSocketAddress().toString());
                    /**
                     * 因为考虑到多手机连接的情况 所以加入线程锁 只允许单线程工作
                     */
                    new Thread(new Runnable() {
                        private String text;

                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    // 在这里考虑到线程总数的计算 也代表着连接手机的数量
                                    ++sum;
                                    // 存入到集合和Map中为群发和单独发送做准备
                                    String string = socket.getRemoteSocketAddress().toString();
                                    clientList.add(string);
                                    clientMap.put(string, socket);
                                }

                                // 定义输入输出流
                                InputStream is = socket.getInputStream();
                                OutputStream os = socket.getOutputStream();

                                // 接下来考虑输入流的读取显示到PC端和返回是否收到
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    text = new String(buffer, 0, len);
                                    Log.e("Socket","收到的数据为：" + text);
                                    os.write("已收到消息".getBytes("utf-8"));

                                    if (text.contains("8899")) {
                                        String[] urls = text.split(":");

                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                Log.e("Socket","关闭连接："+socket.getRemoteSocketAddress().toString());
                                synchronized (this) {
                                    --sum;
                                    String string = socket.getRemoteSocketAddress().toString();
                                    clientMap.remove(string);
                                    clientList.remove(string);
                                }
                            }
                        }
                    }).start();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 关闭server
        public void stop() {
            isExit = true;
            if (server != null) {
                try {
                    server.close();
                    Log.e("Socket","已关闭server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 启动server
    public static void startServer() {
        Log.e("Socket","开启server");
        if (serverThread != null) {
            Log.e("Socket","server不为null正在重启server");
            // 以下为关闭server和socket
            shutDown();
        }
        // 初始化
        serverThread = new ServerThread();
        new Thread(serverThread).start();
        Log.e("Socket","开启server成功");
    }

    // 发送消息的方法
    public static boolean sendMessage(String name, String mag) {
        try {
            Socket socket = clientMap.get(name);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(mag.getBytes("utf-8"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 群发的方法
    public static boolean sendMsgAll(String msg) {
        try {
            for (Socket socket : clientMap.values()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msg.getBytes("utf-8"));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取线程总数的方法，也等同于<获取已连接了多少台手机>的方法+
    public static int sumTotal() {
        return sum;
    }

    // 一个获取list集合的方法，取到所有连接server的手机的ip和端口号的集合
    public static List<String> getTotalClients() {
        return clientList;
    }

    public static void shutDown() {
        for (Socket socket : clientMap.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverThread.stop();
        clientMap.clear();
        clientList.clear();
    }
}
