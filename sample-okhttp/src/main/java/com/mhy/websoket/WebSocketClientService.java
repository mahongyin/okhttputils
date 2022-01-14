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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.mhy.http.websocket.WebSocketUtils;
import com.mhy.http.websocket.listener.WebSoketListener;
import com.mhy.sample_okhttp.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class WebSocketClientService extends Service {
    private static final String CONNECT_TAG = "Service:WakeLock";
    private WebSocketUtils client;
    private WebSocketClientBinder mBinder = new WebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;

    //灰色保活 26以下
    private static class GrayInnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
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
        Log.i("mhyLog", "服务onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("mhyLog", "服务onStartCommand");
        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < 18) {
            //Android4.3以下 ，隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if (Build.VERSION.SDK_INT > 18 && Build.VERSION.SDK_INT <= 25) {
            //Android4.3 - Android7.0，隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if (Build.VERSION.SDK_INT >= 26){
            //Android7.0以上app启动后通知栏会出现一条"正在运行"的通知
            startForeground(GRAY_SERVICE_ID, getChannelNotification(getApplicationInfo().name,"程序运行中","message"));
        }

        acquireWakeLock();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }


    /**
     * 连接websocket
     */
    public void connect(String url,WebSoketListener listener) {
        if (TextUtils.isEmpty(url)) {
            url = "";
        }
        if (client != null) {
            client.stopConnect();
            client = null;
        }
        //创建 websocket client
        client = new WebSocketUtils.Builder(getBaseContext())
                .client(new OkHttpClient().newBuilder()
                        .pingInterval(15, TimeUnit.SECONDS)//设置WebSocket连接的保活15s
                        .retryOnConnectionFailure(true)
                        .build())
                .needReconnect(true)
                .wsUrl(url)
                .build();
        client.setWebSoketListener(listener);
        client.startConnect();

    }

    /**
     * 发送消息
     */
    public boolean sendMsg(String msg) {
        if (client != null && client.isWsConnected()) {
            boolean isSend = client.sendMessage(msg);
            return isSend;
        } else {
            Toast.makeText(getBaseContext(), "发送失败:请先连接服务器", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 断开连接
     */
    public void closeConnect() {
        if (client != null) {
            client.stopConnect();
            client = null;
        }
    }


    //    -----------------------------------消息通知--------------------------------------------------------
    private PowerManager.WakeLock wakeLock;//锁屏唤醒

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    /*标记名称应使用唯一的前缀，后跟一个冒号（找到了AcquisitionWakeLock）。
    例如myapp：mywakelocktag。这将有助于调试*/
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, CONNECT_TAG);
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }


    PendingIntent pendingIntent;

    public void chatData(String title, String content, String channelId) {
        Intent intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = getChannelNotification( title,  content,  channelId);
            manager.notify(1, notification);
        } else {
            Notification notification_25 = getNotification_25( title,content);
            manager.notify(1, notification_25);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getChannelNotification(String title, String content, String channelId) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
		       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "通知", NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(channelId);
        }
        return builder
//设置优先级
                .setPriority(NotificationCompat.PRIORITY_MAX)
//设置标题
                .setContentTitle(title)
//设置内容
                .setContentText(content)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//设置跳转界面
                .setContentIntent(pendingIntent)
//设置被创建的时间(毫秒)
                .setWhen(System.currentTimeMillis())
//设置小icon
                .setSmallIcon(R.mipmap.ic_launcher)
                .setNumber(10)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//是否自动消失，true表示响应点击之后自动消失。
                .setAutoCancel(true)
//设置震动，注意这里需要在AndroidManifest.xml中设置
                .setVibrate(new long[]{0, 300, 500, 700})
//设置默认的三色灯与振动器
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
////设置LED闪烁
                .setLights(Color.BLUE, 2000, 1000)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(
                        BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)))
                .build();

    }
/**不需要渠道*/
    private Notification getNotification_25(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        return builder.setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("setStyle()方法，这个方法允许我们构建出富文本的通知内容"))
                .build();
    }

}