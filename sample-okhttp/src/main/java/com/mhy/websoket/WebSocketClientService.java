package com.mhy.websoket;

/**
 * author    : mahongyin
 * e-mail    : mhy.work@foxmail.com
 * date      : 2020-08-25 21:40
 * introduce :
 */

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mhy.http.websocket.WebSocketUtils;
import com.mhy.http.websocket.listener.WebSoketListener;
import com.mhy.sample_okhttp.R;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class WebSocketClientService extends Service {
    public WebSocketUtils client;
    private WebSocketClientBinder mBinder = new WebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;
    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new MyNotification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
    PowerManager.WakeLock wakeLock;//锁屏唤醒
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    //用于Activity和service通讯
    public class WebSocketClientBinder extends Binder {
        public WebSocketClientService getService() {
            return WebSocketClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化websocket
        initSocketClient();

        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < 18) {
            //Android4.3以下 ，隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if(Build.VERSION.SDK_INT>18 && Build.VERSION.SDK_INT<25){
            //Android4.3 - Android7.0，隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }else{
            //Android7.0以上app启动后通知栏会出现一条"正在运行"的通知
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        acquireWakeLock();
        return START_STICKY;
    }
    public static class MyNotification extends Notification{

    }
    private Notification getNotification(){
        // 获取系统 通知管理 服务
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 构建 Notification
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("程序运行中")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("程序运行中");

// 兼容  API 26，Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // 第三个参数表示通知的重要程度，默认则只在通知栏闪烁一下
            NotificationChannel notificationChannel = new NotificationChannel("AppTestNotificationId", "AppTestNotificationName", NotificationManager.IMPORTANCE_DEFAULT);
            // 注册通道，注册后除非卸载再安装否则不改变
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId("AppTestNotificationId");
        }
// 发出通知
//notificationManager.notify(1, builder.build());
        return builder.build();
    }

    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }
private static String TAG ="mhylog";
    private WebSoketListener webSoketListener = new WebSoketListener() {
        @Override
        public void onOpen(Response response) {
            //连接成功 通知
            Log.e(TAG, "websocket连接成功");
        }

        @Override
        public void onMessage(String message) {
            //接受到 文本消息
            Log.e("JWebSocketClientService", "收到的消息：" + message);

            Intent intent = new Intent();
            intent.setAction("com.mhy.servicecallback.content");
            intent.putExtra("message", message);
            sendBroadcast(intent);

            checkLockAndShowNotification(message);
        }

        @Override
        public void onMessage(ByteString bytes) {
            try {

//                https://www.cnblogs.com/shuaiguoguo/p/8883862.html
//                FileInputStream inputStream = new FileInputStream("f://滑板//HEEL_FLIP.mp4");
//                BufferedInputStream bis = new BufferedInputStream(inputStream);
                String path =getExternalCacheDir().getAbsolutePath()+"/download/file.png";
                Log.i(TAG,path);
                FileOutputStream outputStream = new FileOutputStream(path);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
//                int len;
//                byte[] bs = new byte[1024];
//                // 开始时间
//                long begin = System.currentTimeMillis();
//                while ((len = bis.read(bs)) != -1) {
//                    bos.write(bs, 0, len);
//                }
//                // 用时毫秒
//                System.out.println(System.currentTimeMillis() - begin);// 78
                bytes.write(bos);

//                bis.close();
                bos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            //接收到 文件
        }

        @Override
        public void onReconnect() {
            Log.d(TAG, "Websocket-----onReconnect");

        }

        @Override
        public void onClosing(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosing");

        }

        @Override
        public void onClosed(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosed");

        }

        @Override
        public void onFailure(Throwable t, Response response) {
            Log.d(TAG, "Websocket-----onFailure");

        }
    };

    /**
     * 初始化websocket连接
     */
    private void initSocketClient() {
        connect();
    }

    /**
     * 连接websocket
     */
    private void connect() {
        if (client != null) {
            client.stopConnect();
            client = null;
        }
        //创建 websocket client
        client = new WebSocketUtils.Builder(getBaseContext())
                .client(new OkHttpClient().newBuilder()
                        .pingInterval(15, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build())
                .needReconnect(true)
                .wsUrl("ws://echo.websocket.org")
                .build();
        client.setWebSoketListener(webSoketListener);
        client.startConnect();

    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(String msg) {
        if (client != null && client.isWsConnected()) {
            boolean isSend = client.sendMessage(msg);
            if (isSend) {
            showOrHideInputMethod();}
        } else {
            Toast.makeText(getBaseContext(), "请先连接服务器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 断开连接
     */
    private void closeConnect() {
        if (client != null) {
            client.stopConnect();
            client = null;
        }
    }


//    -----------------------------------消息通知--------------------------------------------------------

    /**
     * 检查锁屏状态，如果锁屏先点亮屏幕
     *
     * @param content
     */
    private void checkLockAndShowNotification(String content) {
        //管理锁屏的一个服务
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {//锁屏
            //获取电源管理器对象
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()) {
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                wl.acquire();  //点亮屏幕
                wl.release();  //任务结束后释放
            }
            sendNotification(content);
        } else {
            sendNotification(content);
        }
    }

    /**
     * 发送通知
     *
     * @param content
     */
    private void sendNotification(String content) {
        Intent intent = new Intent();
        intent.setClass(this, SocketActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this,"abc")
                .setAutoCancel(true)
                // 设置该通知优先级
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_clear)
                .setContentTitle("服务器")
                .setContentText(content)
                .setVisibility(VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                // 向通知添加声音、闪灯和振动效果
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .build();
        notifyManager.notify(1, notification);//id要保证唯一
    }


    //隐藏 键盘
    private void showOrHideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}