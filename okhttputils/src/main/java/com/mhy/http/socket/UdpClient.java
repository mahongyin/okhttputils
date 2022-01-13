package com.mhy.http.socket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created By Mahongyin
 * Date    2022/1/13 12:13
 */
public class UdpClient {

    private final static String SEND_IP = "10.3.3.44";  //发送IP
    private final static int SEND_PORT = 8989;               //发送端口号
    private final static int RECEIVE_PORT = 8080;            //接收端口号

    private boolean listenStatus = true;  //接收线程的循环标识
    private byte[] receiveInfo;     //接收报文信息
    private byte[] buf;

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    private InetAddress serverAddr;
    private SendHandler sendHandler = new SendHandler();
    private ReceiveHandler receiveHandler = new ReceiveHandler();

    private TextView tvMessage;
    private Button btnSendUDP;

    class ReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvMessage.setText("接收到数据了" + receiveInfo.toString());
        }
    }

    class SendHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvMessage.setText("UDP报文发送成功");

        }
    }
    WifiManager.MulticastLock lock;
    protected void open(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lock = manager.createMulticastLock("test wifi");
        lock.acquire();

        btnSendUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮则发送UDP报文
                new UdpSendThread("内容").start();
            }
        });

        //进入Activity时开启接收报文线程
        new UdpReceiveThread().start();
    }


    protected void close() {

        //停止接收线程，关闭套接字连接
        listenStatus = false;
        receiveSocket.close();
        lock.release();
    }

    /**
     *   UDP数据发送线程
     * */
    public class UdpSendThread extends Thread {
        String value;
        UdpSendThread(String msg){
            value=msg;
        }
        @Override
        public void run() {
            try {
                buf = value.getBytes();
                // 创建DatagramSocket对象，使用随机端口
                sendSocket = new DatagramSocket();
                serverAddr = InetAddress.getByName(SEND_IP);
                DatagramPacket outPacket = new DatagramPacket(buf, buf.length, serverAddr, SEND_PORT);
                sendSocket.send(outPacket);
                sendSocket.close();
                sendHandler.sendEmptyMessage(1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *   UDP数据接收线程
     * */
    public class UdpReceiveThread extends Thread {
        @Override
        public void run() {
            try {

                receiveSocket = new DatagramSocket(RECEIVE_PORT);
                serverAddr = InetAddress.getByName(SEND_IP);

                while (listenStatus) {
                    byte[] inBuf = new byte[1024];
                    DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
                    receiveSocket.receive(inPacket);

                    if (!inPacket.getAddress().equals(serverAddr)) {
                        throw new IOException("未知名的报文");
                    }

                    receiveInfo = inPacket.getData();
                    receiveHandler.sendEmptyMessage(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
