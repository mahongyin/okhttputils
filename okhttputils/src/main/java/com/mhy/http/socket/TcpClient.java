package com.mhy.http.socket;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * TcpClient.java：tcp-socket的客户端，略微做了通用封装，主要是连接，发送，接收，然后设置监听
 * Function: tcp client  长度无限制
 */
public class TcpClient {

    public static void main(String[] args) {
        getInstance().setOnDataReceiveListener(new OnDataReceiveListener() {
            @Override
            public void onConnectSuccess() {

            }

            @Override
            public void onConnectFail() {

            }

            @Override
            public void onDataReceive(byte[] buffer, int size, int requestCode) {
//获取有效长度的数据
                byte[] data = new byte[size];
                System.arraycopy(buffer, 0, data, 0, size);
                String oxValue = Arrays.toString(data);
            }
        });
        getInstance().connect("127.0.0.1", 8899);
        if (getInstance().isConnect()) {
            byte[] data = "asdddddd".getBytes();
            getInstance().sendByteCmd(data, 1001);
        }
    }
    /**
     * single instance TcpClient
     * */
    private static TcpClient mSocketClient = null;
    private TcpClient(){}
    public static TcpClient getInstance(){
        if(mSocketClient == null){
            synchronized (TcpClient.class) {
                mSocketClient = new TcpClient();
            }
        }
        return mSocketClient;
    }


    String TAG_log = "Socket";
    private Socket mSocket;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private SocketThread mSocketThread;
    private boolean isStop = false;//thread flag


    /**
     * 128 - 数据按照最长接收，一次性
     * */
    private class SocketThread extends Thread {

        private String ip;
        private int port;
        public SocketThread(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            Log.e(TAG_log,"SocketThread start连接 ");
            super.run();

            //connect ...
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }

                InetAddress ipAddress = InetAddress.getByName(ip);
                mSocket = new Socket(ipAddress, port);

                //设置不延时发送
                //mSocket.setTcpNoDelay(true);
                //设置输入输出缓冲流大小
                //mSocket.setSendBufferSize(8*1024);
                //mSocket.setReceiveBufferSize(8*1024);

                if(isConnect()){
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

                    isStop = false;

                    uiHandler.sendEmptyMessage(1);
                }
                /* 此处这样做没什么意义不大，真正的socket未连接还是靠心跳发送，等待服务端回应比较好，一段时间内未回应，则socket未连接成功 */
                else{
                    uiHandler.sendEmptyMessage(-1);
                    Log.e(TAG_log,"SocketThread connect fail失败");
                    return;
                }

            }
            catch (IOException e) {
                uiHandler.sendEmptyMessage(-1);
                Log.e(TAG_log,"SocketThread connect io exception = "+e.getMessage());
                e.printStackTrace();
                return;
            }
            Log.e(TAG_log,"SocketThread connect over ");


            //read ...
            while (isConnect() && !isStop && !isInterrupted()) {

                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);//null data -1 , zrd serial rule size default 10
                    if (size > 0) {
                        Message msg = new Message();
                        msg.what = 100;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data",buffer);
                        bundle.putInt("size",size);
                        bundle.putInt("requestCode",requestCode);
                        msg.setData(bundle);
                        uiHandler.sendMessage(msg);
                    }
                    Log.i(TAG_log, "SocketThread read listening");
                    //Thread.sleep(100);//log eof
                }
                catch (IOException e) {
                    uiHandler.sendEmptyMessage(-1);
                    Log.e(TAG_log,"SocketThread read io exception = "+e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }



    //==============================socket connect============================
    /**
     * connect socket in thread
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void connect(String ip, int port){
        mSocketThread = new SocketThread(ip, port);
        mSocketThread.start();
    }

    /**
     * socket is connect
     * */
    public boolean isConnect(){
        boolean flag = false;
        if (mSocket != null) {
            flag = mSocket.isConnected();
        }
        return flag;
    }

    /**
     * socket disconnect
     * */
    public void disconnect() {
        isStop = true;
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }

            if (mInputStream != null) {
                mInputStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSocketThread != null) {
            mSocketThread.interrupt();//not intime destory thread,so need a flag
        }
    }



    /**
     * send byte[] cmd
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void sendByteCmd(final byte[] mBuffer,int requestCode) {
        this.requestCode = requestCode;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBuffer);
                        mOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    /**
     * send string cmd to serial
     */
    public void sendStrCmds(String cmd, int requestCode) {
        byte[] mBuffer = cmd.getBytes();
        sendByteCmd(mBuffer,requestCode);
    }


    /**
     * send prt content cmd to serial
     */
    public void sendChsPrtCmds(String content, int requestCode) {
        try {
            byte[] mBuffer = content.getBytes("GB2312");
            sendByteCmd(mBuffer,requestCode);
        }
        catch (UnsupportedEncodingException e1){
            e1.printStackTrace();
        }
    }


    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                //connect error
                case -1:
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectFail();
                        disconnect();
                    }
                    break;

                //connect success
                case 1:
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectSuccess();
                    }
                    break;

                //receive data
                case 100:
                    Bundle bundle = msg.getData();
                    byte[] buffer = bundle.getByteArray("data");
                    int size = bundle.getInt("size");
                    int mequestCode = bundle.getInt("requestCode");
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onDataReceive(buffer, size, mequestCode);
                    }
                    break;
            }
        }
    };


    /**
     * socket response data listener
     * */
    private OnDataReceiveListener onDataReceiveListener = null;
    private int requestCode = -1;
    public interface OnDataReceiveListener {
        public void onConnectSuccess();
        public void onConnectFail();
        public void onDataReceive(byte[] buffer, int size, int requestCode);
    }
    public void setOnDataReceiveListener(
            OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }


}
