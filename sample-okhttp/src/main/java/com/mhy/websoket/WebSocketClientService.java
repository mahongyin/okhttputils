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
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class WebSocketClientService extends Service {
    private static final String CONNECT_TAG = "Service:WakeLock";
    private WebSocketUtils client;
    private WebSocketClientBinder mBinder = new WebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;
    private final static int SERVICE_ID = 1000;

    //灰色保活 26以下
    public static class GrayInnerService extends Service {
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
        //初始化websocket
        initSocketClient();
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
            startForeground(GRAY_SERVICE_ID, getChannelNotification("appname","程序运行中","message"));
        }

        acquireWakeLock();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

    private static String TAG = "mhylog";
    private WebSoketListener webSoketListener = new WebSoketListener() {
        @Override
        public void onOpen(Response response) {
            //连接成功 通知
            Log.e(TAG, "websocket连接成功");
        }

       final String message = "{\"cmd\":\"msg.ping\"}";
        Timer mTimer ;
        TimerTask timerTask ;

        //每10秒发送一次心跳
        private void startTask() {
			  mTimer = new Timer();
         timerTask = new TimerTask() {
            public void run() {
                if (client != null) {
                    client.sendMessage(message);
                }
            }
        };
            mTimer.schedule(timerTask, 0, 10000);

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
            //接收到 文件
        }

        @Override
        public void onReconnect() {
            Log.d(TAG, "Websocket-----onReconnect");

        }

        @Override
        public void onClosing(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosing");
timerTask.cancel()；
            mTimer.cancel()；
        }

        @Override
        public void onClosed(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosed");

        }

        @Override
        public void onFailure(Throwable t, Response response) {
            Log.d(TAG, "Websocket-----onFailure");
  //stopSelf();//自行结束服务
  timerTask.cancel()；
            mTimer.cancel()；
        }
    };

    /**
     * 初始化websocket连接
     */
    private void initSocketClient() {
        connect("wss://echo.websocket.org");
        //设置消息渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 第三个参数表示通知的重要程度，默认则只在通知栏闪烁一下
            NotificationChannel channel01 = new NotificationChannel(
                    "message", "消息通知",
                    NotificationManager.IMPORTANCE_HIGH);
            channel01.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            // 注册通道，注册后除非卸载再安装否则不改变
            notifyManager.createNotificationChannel(channel01);
            NotificationChannel notificationChannel = new NotificationChannel(
                    "notification", "通知",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            // 注册通道，注册后除非卸载再安装否则不改变
            notifyManager.createNotificationChannel(notificationChannel);
            NotificationChannel notification = new NotificationChannel(
                    "subscribe", "订阅",
                    NotificationManager.IMPORTANCE_LOW);
            notification.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            // 注册通道，注册后除非卸载再安装否则不改变
            notifyManager.createNotificationChannel(notification);
        }
    }

    /**
     * 连接websocket
     */
    public void connect(String url) {
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
                        .pingInterval(15, TimeUnit.SECONDS)//设置WebSocket连接的保活
                        .retryOnConnectionFailure(true)
                        .build())
                .needReconnect(true)
                .wsUrl(url)
                .build();
        client.setWebSoketListener(webSoketListener);
        client.startConnect();

    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public boolean sendMsg(String msg) {
        if (client != null && client.isWsConnected()) {
            boolean isSend = client.sendMessage(msg);
            return isSend;
        } else {
            Toast.makeText(getBaseContext(), "sendmsg:请先连接服务器", Toast.LENGTH_SHORT).show();
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
    PowerManager.WakeLock wakeLock;//锁屏唤醒

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

    /**
     * 检查锁屏状态，如果锁屏先点亮屏幕
     *
     * @param content
     */
    private void checkLockAndShowNotification(String content) {
        //管理锁屏的一个服务
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (km.isKeyguardLocked()) {//锁屏
                //获取电源管理器对象
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                if (!pm.isInteractive()) {
                    @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "bright");
                    wl.acquire(60 * 1000L);  //点亮屏幕1分钟
                    wl.release();  //任务结束后释放
                }
                sendNotification("收到消息",content,"message");
            } else {
                sendNotification("收到消息",content,"subscribe");
            }
        } else {
            if (km.inKeyguardRestrictedInputMode()) {//锁屏
                //获取电源管理器对象
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                if (!pm.isScreenOn()) {
                    @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                    wl.acquire();  //点亮屏幕
                    wl.release();  //任务结束后释放
                }
                sendNotification("收到消息",content,"message");
            } else {
                sendNotification("收到消息",content,"subscribe");
            }
        }
    }

    /**
     * 发送通知
     *
     * @param content
     */
    private void sendNotification(String title,String content,String Cannnalid) {
        Intent intent = new Intent();
        intent.setClass(this, SocketActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, Cannnalid);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_clear)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                // 向通知添加声音、闪灯和振动效果
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);
        notifyManager.notify(SERVICE_ID, builder.build());//id要保证唯一
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