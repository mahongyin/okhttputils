package com.mhy.http.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.mhy.http.okhttp.utils.NetUtils;
import com.mhy.http.websocket.listener.WebSoketListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * websocket 使用
 */

public class WebSocketUtils implements IWebSocket {

    private final static int RECONNECT_INTERVAL = 10 * 1000;    //重连自增步长
    private final static long RECONNECT_MAX_TIME = 120 * 1000;   //最大重连间隔
    private Context mContext;
    private String wsUrl;
    private WebSocket mWebSocket;
    private OkHttpClient mOkHttpClient;
    private Request mRequest;
    private int mCurrentStatus = WebSocketStatus.DISCONNECTED;     //websocket连接状态
    private boolean isNeedReconnect;          //是否需要断线自动重连
    private boolean isManualClose = false;         //是否为手动关闭websocket连接
    private WebSoketListener webSoketListener;
    private Lock mLock;
    private Handler wsMainHandler = new Handler(Looper.getMainLooper());
    private int reconnectCount = 0;   //重连次数
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (webSoketListener != null) {
                webSoketListener.onReconnect();
            }
            buildConnect();
        }
    };
    private WebSocketListener mWebSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, final Response response) {
            mWebSocket = webSocket;
            setCurrentStatus(WebSocketStatus.CONNECTED);
            connected();
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onOpen(response);
                        }
                    });
                } else {
                    webSoketListener.onOpen(response);
                }
            }
        }

        /**
         * 图片文件
         * @param webSocket
         * @param bytes
         */
        @Override
        public void onMessage(WebSocket webSocket, final ByteString bytes) {
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onMessage(bytes);
                        }
                    });
                } else {
                    webSoketListener.onMessage(bytes);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onMessage(text);
                        }
                    });
                } else {
                    webSoketListener.onMessage(text);
                }
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, final int code, final String reason) {
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onClosing(code, reason);
                        }
                    });
                } else {
                    webSoketListener.onClosing(code, reason);
                }
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, final int code, final String reason) {
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onClosed(code, reason);
                        }
                    });
                } else {
                    webSoketListener.onClosed(code, reason);
                }
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, final Response response) {
            tryReconnect();
            if (webSoketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSoketListener.onFailure(t, response);
                        }
                    });
                } else {
                    webSoketListener.onFailure(t, response);
                }
            }
        }
    };

    public WebSocketUtils(Builder builder) {
        mContext = builder.mContext;
        wsUrl = builder.wsUrl;
        isNeedReconnect = builder.needReconnect;
        mOkHttpClient = builder.mOkHttpClient;
        this.mLock = new ReentrantLock();
    }

    private void initWebSocket() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }
        if (mRequest == null) {
            mRequest = new Request.Builder()
                    .url(wsUrl)
                    .build();
        }
        mOkHttpClient.dispatcher().cancelAll();
        try {
            mLock.lockInterruptibly();
            try {
                mOkHttpClient.newWebSocket(mRequest, mWebSocketListener);
            } finally {
                mLock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WebSocket getWebSocket() {
        return mWebSocket;
    }


    public void setWebSoketListener(WebSoketListener webSoketListener) {
        this.webSoketListener = webSoketListener;
    }

    @Override
    public synchronized boolean isWsConnected() {
        return mCurrentStatus == WebSocketStatus.CONNECTED;
    }

    @Override
    public synchronized int getCurrentStatus() {
        return mCurrentStatus;
    }

    @Override
    public synchronized void setCurrentStatus(int currentStatus) {
        this.mCurrentStatus = currentStatus;
    }

    @Override
    public void startConnect() {
        isManualClose = false;
        buildConnect();
    }

    @Override
    public void stopConnect() {
        isManualClose = true;
        disconnect();
    }

    private void tryReconnect() {
        if (!isNeedReconnect | isManualClose) {
            return;
        }

        if (!NetUtils.isNetworkConnected(mContext)) {
            setCurrentStatus(WebSocketStatus.DISCONNECTED);
            return;
        }

        setCurrentStatus(WebSocketStatus.RECONNECT);

        long delay = reconnectCount * RECONNECT_INTERVAL;
        wsMainHandler.postDelayed(reconnectRunnable, Math.min(delay, RECONNECT_MAX_TIME));/*取小*/
        reconnectCount++;
    }

    private void cancelReconnect() {
        wsMainHandler.removeCallbacks(reconnectRunnable);
        reconnectCount = 0;
    }

    private void connected() {
        cancelReconnect();
    }

    private void disconnect() {
        if (mCurrentStatus == WebSocketStatus.DISCONNECTED) {
            return;
        }
        cancelReconnect();
        if (mOkHttpClient != null) {
            mOkHttpClient.dispatcher().cancelAll();
        }
        if (mWebSocket != null) {
            boolean isClosed = mWebSocket.close(WebSocketStatus.CODE.NORMAL_CLOSE, WebSocketStatus.TIP.NORMAL_CLOSE);
            //非正常关闭连接
            if (!isClosed) {
                if (webSoketListener != null) {
                    webSoketListener.onClosed(WebSocketStatus.CODE.ABNORMAL_CLOSE, WebSocketStatus.TIP.ABNORMAL_CLOSE);
                }
            }
        }
        setCurrentStatus(WebSocketStatus.DISCONNECTED);
    }

    private synchronized void buildConnect() {
        if (!NetUtils.isNetworkConnected(mContext)) {
            setCurrentStatus(WebSocketStatus.DISCONNECTED);
            return;
        }
        switch (getCurrentStatus()) {
            case WebSocketStatus.CONNECTED:
            case WebSocketStatus.CONNECTING:
                break;
            default:
                setCurrentStatus(WebSocketStatus.CONNECTING);
                initWebSocket();
        }
    }

    //发送消息
    @Override
    public boolean sendMessage(String msg) {
        return send(msg);
    }

    /**
     * 发送 文件 、图片
     *
     * @param byteString
     * @return
     */
    @Override
    public boolean sendMessage(ByteString byteString) {
        return send(byteString);
    }

    private boolean send(Object msg) {
        boolean isSend = false;
        if (mWebSocket != null && mCurrentStatus == WebSocketStatus.CONNECTED) {
            if (msg instanceof String) {
                isSend = mWebSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = mWebSocket.send((ByteString) msg);
            }
            //发送消息失败，尝试重连
            if (!isSend) {
                tryReconnect();
            }
        }
        return isSend;
    }

    public static final class Builder {

        private Context mContext;
        private String wsUrl;
        private boolean needReconnect = true;
        private OkHttpClient mOkHttpClient;

        public Builder(Context val) {
            mContext = val;
        }

        public Builder wsUrl(String val) {
            wsUrl = val;
            return this;
        }

        public Builder client(OkHttpClient val) {
            mOkHttpClient = val;
            return this;
        }

        public Builder needReconnect(boolean val) {
            needReconnect = val;
            return this;
        }

        public WebSocketUtils build() {
            return new WebSocketUtils(this);
        }
    }
}
