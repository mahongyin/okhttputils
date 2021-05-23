package com.mhy.websoket;

/**
 * Created by mahongyin on 2021/5/23.
 *
 * @author mahongyin
 * @date 2021/5/23 12:16
 */
public class test {

}
//package com.cctv.media.service;
//
///**
// * author    : mahongyin
// * e-mail    : mhy.work@foxmail.com
// * date      : 2020-08-25 21:40
// * introduce :
// */
//
//import android.annotation.SuppressLint;
//import android.app.KeyguardManager;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Binder;
//import android.os.Build;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import androidx.annotation.RequiresApi;
//import androidx.core.app.NotificationCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import com.cctv.media.R;
//import com.cctv.media.ui.MainActivity;
//import com.cctv.media.ui.VideoActivity;
//import com.mhy.http.websocket.WebSocketUtils;
//import com.mhy.http.websocket.listener.WebSoketListener;
//
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Response;
//import okio.ByteString;
//
//public class WebSocketClientService extends Service {
//
//    private WebSocketUtils webSocketUtils;
//    private final static int GRAY_SERVICE_ID = 1001;//通知栏固定保活id
//    private final static int SERVICE_ID = 1000;//消息提示
//
//    //灰色保活
//    public static class GrayInnerService extends Service {
//        @Override
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            //本拉起就是为26以下准备的 不需要渠道
//            startForeground(GRAY_SERVICE_ID, new Notification());
//            stopForeground(true);
//            stopSelf();
//            return super.onStartCommand(intent, flags, startId);
//        }
//
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//    }
//
//    /**
//     * 定义在service中的内部类，用于Activity和service通讯
//     */
//    public class WebSocketClientBinder extends Binder {
//        //这里返回activity交互的数据
//        public WebSocketClientService getService() {
//            return WebSocketClientService.this;
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.e("mhyLog", "服务onCreate");
//    }
//
//    //bindService(intent)方法 执行这里
//    @Override
//    public IBinder onBind(Intent intent) {
//        Log.e("mhyLog", "服务onBind");
//        initService(intent);
//        return new WebSocketClientBinder();//相当于给外部提供service实例去交互
//    }
//
//    //startService(intent)方法 执行这里
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e("mhyLog", "服务onStartCommand");
//        initService(intent);
//        return START_STICKY;//如果被杀死 保持启动 不保留意图
//    }
//
//    private void initService(Intent intent) {
//        String url = intent.getStringExtra("url");
//        if (TextUtils.isEmpty(url)) {
//            url = "wss://echo.websocket.org";
//        }
//        connect(url);//"wss://echo.websocket.org" //初始化websocket
//        //设置消息渠道
//        initNotficChannel();
//        initForeground();//保活
//        acquireWakeLock();//息屏仍运行
//    }
//    /**
//     * 连接websocket
//     */
//    public void connect(String url) {
//        if (TextUtils.isEmpty(url)) {
//            url = "";
//        }
//        if (webSocketUtils != null) {
//            webSocketUtils.stopConnect();
//            webSocketUtils = null;
//        }
//        //创建 websocket client
//        webSocketUtils = new WebSocketUtils.Builder(getBaseContext())
//                .client(new OkHttpClient().newBuilder()
//                        .pingInterval(15, TimeUnit.SECONDS)//设置WebSocket连接的保活
//                        .retryOnConnectionFailure(true)
//                        .build())
//                .needReconnect(true)
//                .wsUrl(url)
//                .build();
//        webSocketUtils.setWebSoketListener(webSoketListener);
//        webSocketUtils.startConnect();
//
//    }
//
//    /**
//     * 发送消息
//     *
//     * @param msg
//     */
//    public boolean sendMsg(String msg) {
//        if (webSocketUtils != null && webSocketUtils.isWsConnected()) {
//            boolean isSend = webSocketUtils.sendMessage(msg);
//            return isSend;
//        } else {
//            Toast.makeText(getBaseContext(), "sendmsg:请先连接服务器", Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }
//
//    /**
//     * 断开连接
//     */
//    public void closeConnect() {
//        if (webSocketUtils != null) {
//            webSocketUtils.stopConnect();
//            webSocketUtils = null;
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        closeConnect();
//        super.onDestroy();
//    }
//
//    private static String TAG = "mhylog";
//    private WebSoketListener webSoketListener = new WebSoketListener() {
//        @Override
//        public void onOpen(Response response) {
//            //连接成功 通知
//            Log.e(TAG, "websocket连接成功");
//            startTask();
//        }
//
//        final String message = "{\"cmd\":\"msg.ping\"}";
//        Timer mTimer;
//        TimerTask timerTask;
//
//        //每10秒发送一次心跳
//        private void startTask() {
//            mTimer = new Timer();
//            timerTask = new TimerTask() {
//                public void run() {
//                    if (webSocketUtils != null) {
//                        webSocketUtils.sendMessage(message);
//                    }
//                }
//            };
//            mTimer.schedule(timerTask, 0, 10000);
//
//        }
//
//        @Override
//        public void onMessage(String message) {
//            //接受到 文本消息
//            Log.e("onMessage", "收到的消息：" + message);
//            LocalBroadcastManager loaclBroadManage = LocalBroadcastManager.getInstance(WebSocketClientService.this);
//            Intent intent = new Intent();
//            intent.setAction("com.mhy.servicecallback.content");
//            intent.putExtra("message", message);
//            //本地广播
//            loaclBroadManage.sendBroadcast(intent);
//           // checkLockAndShowNotification(message);
//            chatData("收到消息", message, channel3);
//        }
//
//        @Override
//        public void onMessage(ByteString bytes) {
//            //接收到 文件
//        }
//
//        @Override
//        public void onReconnect() {
//            Log.e(TAG, "Websocket-----onReconnect");
//
//        }
//
//        @Override
//        public void onClosing(int code, String reason) {
//            Log.e(TAG, "Websocket-----onClosing");
//            if (timerTask != null) {
//                timerTask.cancel();
//            }
//            if (mTimer != null) {
//                mTimer.cancel();
//            }
//        }
//
//        @Override
//        public void onClosed(int code, String reason) {
//            Log.e(TAG, "Websocket-----onClosed");
//        }
//
//        @Override
//        public void onFailure(Throwable t, Response response) {
//            Log.e(TAG, "Websocket-----onFailure");
//            //失败会重连 重建定时
//            if (timerTask != null) {
//                timerTask.cancel();
//            }
//            if (mTimer != null) {
//                mTimer.purge();
//                mTimer.cancel();
//            }
//            //stopSelf();//自行结束服务
//        }
//    };
//
//    /**
//     * 初始化 通知渠道
//     */
//    private final String channel1="message";
//    private final String channel2="notification";
//    private final String channel3="subscribe";
//    private void initNotficChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            // 第三个参数表示通知的重要程度，默认则只在通知栏闪烁一下
//            NotificationChannel channel01 = new NotificationChannel(channel1, "消息", NotificationManager.IMPORTANCE_HIGH);
//            channel01.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//            // 注册通道，注册后除非卸载再安装否则不改变
//            notifyManager.createNotificationChannel(channel01);
//            NotificationChannel notificationChannel = new NotificationChannel(channel2, "通知", NotificationManager.IMPORTANCE_DEFAULT);
//            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//            // 注册通道，注册后除非卸载再安装否则不改变
//            notifyManager.createNotificationChannel(notificationChannel);
//            NotificationChannel notification = new NotificationChannel(channel3, "订阅", NotificationManager.IMPORTANCE_LOW);
//            notification.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//            // 注册通道，注册后除非卸载再安装否则不改变
//            notifyManager.createNotificationChannel(notification);
//        }
//    }
//
//    /**
//     * 前台服务
//     */
//    private void initForeground() {
//        NotificationCompat.Builder builder = null;
//        //设置service为前台服务，提高优先级
//        if (Build.VERSION.SDK_INT < 18) {
//            //Android4.3以下 ，隐藏Notification上的图标
//            builder = new NotificationCompat.Builder(this);
//        } else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT <= 25) {
//            //Android4.3 - Android7.0，隐藏Notification上的图标
//            Intent innerIntent = new Intent(this, GrayInnerService.class);
//            startService(innerIntent);
//            builder = new NotificationCompat.Builder(this);
//        } else if (Build.VERSION.SDK_INT >= 26) {
//            //Android7.0以上app启动后通知栏会出现一条"正在运行"的通知
//            builder = new NotificationCompat.Builder(this, channel1);
//        }
//        if (builder != null) {
//            builder.setPriority(NotificationCompat.PRIORITY_MAX)//设置优先级
//                    .setContentTitle(getString(R.string.app_name))
//                    .setContentText("大屏已连接")
//                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                    .setWhen(System.currentTimeMillis())
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setAutoCancel(false);
//            startForeground(GRAY_SERVICE_ID, builder.build());
//        }
//    }
//
//    /*-----------------------------------消息通知--------------------------------------------------------*/
//
//    PowerManager.WakeLock wakeLock;//锁屏唤醒
//    private static final String CONNECT_TAG = "Service:WakeLock";
//
//    /**
//     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
//     * 标记名称应使用唯一的前缀，后跟一个冒号（找到了AcquisitionWakeLock）。例如myapp：mywakelocktag。这将有助于调试
//     */
//    @SuppressLint("WakelockTimeout")
//    private void acquireWakeLock() {
//        if (null == wakeLock) {
//            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, CONNECT_TAG);
//            if (null != wakeLock) {
//                wakeLock.acquire();
//            }
//        }
//    }
//
//    /**
//     * 检查锁屏状态，如果锁屏先点亮屏幕
//     */
//    private void checkLockAndShowNotification(String content) {
//        //管理锁屏的一个服务
//        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//            if (km.isKeyguardLocked()) {//锁屏
//                //获取电源管理器对象
//                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//                if (!pm.isInteractive()) {//非交互
//                    @SuppressLint("InvalidWakeLockTag")
//                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "bright");
//                    wl.acquire(60 * 1000L);  //点亮屏幕1分钟
//                    wl.release();  //任务结束后释放
//                }
//                sendNotification("收到消息", content, channel2);
//            } else {
//                sendNotification("收到消息", content, channel3);
//            }
//        } else {
//            if (km.inKeyguardRestrictedInputMode()) {//锁屏
//                //获取电源管理器对象
//                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//                if (!pm.isScreenOn()) {//非亮屏
//                    @SuppressLint("InvalidWakeLockTag")
//                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
//                    wl.acquire();  //点亮屏幕
//                    wl.release();  //任务结束后释放
//                }
//                sendNotification("收到消息", content, channel2);
//            } else {
//                sendNotification("收到消息", content, channel3);
//            }
//        }
//    }
//
//    /**
//     * 发送通知
//     */
//    private void sendNotification(String title, String content, String cannnalid) {
//        Intent intent = new Intent();
//        intent.setClass(this, VideoActivity.class);
//        intent.putExtra("video","");
//        intent.putExtra("islive",false);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder = new NotificationCompat.Builder(this, cannnalid);
//        } else {
//            builder = new NotificationCompat.Builder(this);
//        }
//        builder.setAutoCancel(true)
//                .setSmallIcon(R.drawable.ic_back)
//                .setContentTitle(title)
//                .setContentText(content)
//                .setWhen(System.currentTimeMillis())
//                // 向通知添加声音、闪灯和振动效果
//                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
//                .setContentIntent(pendingIntent);
//        notifyManager.notify(SERVICE_ID, builder.build());//id要保证唯一
//    }
//
//    private PendingIntent pendingIntent;
//
//    public void chatData(String title, String content, String channelId) {
//        Intent intent = new Intent(this, MainActivity.class);
//        pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Notification notification = getNotification_26(title, content, channelId);
//            manager.notify(SERVICE_ID, notification);
//        } else {
//            Notification notification_25 = getNotification_25(title, content);
//            manager.notify(SERVICE_ID, notification_25);
//        }
//    }
//
//    /**
//     * 创建通知 26
//     */
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private Notification getNotification_26(String title, String content, String channelId) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            //修改安卓8.1以上系统报错
////            NotificationChannel notificationChannel = new NotificationChannel(channelId, "订阅", NotificationManager.IMPORTANCE_MIN);
////            notificationChannel.enableLights(true);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
////            notificationChannel.setShowBadge(true);//是否显示角标
////            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//通知可见性
////            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
////            manager.createNotificationChannel(notificationChannel);
////            builder.setChannelId(channelId);
////        }
//        return builder
//                .setPriority(NotificationCompat.PRIORITY_MAX)//设置优先级
//                .setContentTitle(title)//设置标题
//                .setContentText(content)//设置内容
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setContentIntent(pendingIntent)//设置跳转界面
//                .setWhen(System.currentTimeMillis())//设置被创建的时间(毫秒)
//                .setSmallIcon(R.mipmap.ic_launcher)//设置小icon
//                .setNumber(10)
//                // .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .setAutoCancel(true)//是否自动消失，true表示响应点击之后自动消失。
//                .setVibrate(new long[]{0, 300, 500, 700})//设置震动，注意这里需要在AndroidManifest.xml中设置
//                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)//设置默认的三色灯与振动器
//                .setLights(Color.BLUE, 2000, 1000)//设置LED闪烁
//                // .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)))
//                .build();
//    }
//
//    /**
//     * 不需要渠道 <25
//     */
//    private Notification getNotification_25(String title, String content) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        return builder.setContentTitle(title)
//                .setContentText(content)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                // .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
////                .setStyle(new NotificationCompat.BigTextStyle().bigText("setStyle()方法，这个方法允许我们构建出富文本的通知内容"))
//                .build();
//    }
//
//}

//private var webSocketUtils: WebSocketUtils? = null
//private val webSoketListener = object : WebSoketListener() {
//        override fun onOpen(response: Response) {
//        myLog("Websocket-----连接成功")
//        //连接成功  登录ws
//        loginWs(ACacheUtils.getToken())
//        /*   viewModelScope.launch(Dispatchers.Main) {      //  在 UI 线程开始
//         *//*val isSend =*//* withContext(Dispatchers.IO) {  // 切换到 IO 线程，并在执行完成后切回 UI 线程
////                    while (webSocketUtils?.isWsConnected == true) {
////                        delay(50000)
////                        webSocketUtils?.sendMessage(message)// 将会运行在 IO 线程
////                    }
////                    sendMessage("{\"cmd\":\"发消息啦\"}")
//                } // (isSend)
//            // 回到 UI 线程更新 UI
//
//            }*/
//        //发心跳包
//        startTask()
//        }
//
//        val message = "{\"cmd\":\"msg.ping\"}"
//        var mTimer: Timer? = null //取消后只能新建
//        var timerTask: TimerTask? = null
//
////每10秒发送一次心跳
//private fun startTask() {
//        mTimer = Timer()
//        timerTask = object : TimerTask() {
//        override fun run() {
//        if (webSocketUtils == null) return
//        webSocketUtils?.sendMessage(message)
//        }
//        }
//        mTimer?.schedule(timerTask, 0, 50000)
//
//        }
//
//        override fun onMessage(text: String) {
//        //接受到 文本消息
//        myLog("Websocket-----$text")
//        if (text.contains("\"cmd\":")) {
//        val json = JSONObject(text)
//        val type = json.getString("cmd")
//        when (type) {
//        "cmd.ping" -> {
////                    val message = "{\"cmd\":\"msg.ping\"}"
////                    val timer = Timer()
////                    timer.schedule(object : TimerTask() {
////                        override fun run() {
////                            webSocketUtils?.sendMessage(message)
////                        }
////                    }, 50000)
//        }
//        "comment.get" -> {//获取评论
//        val data = json.getJSONObject("data")
//        val code = data.getString("code")
//        val list = data.getJSONArray("list")
//        myLog("comment.get==$code")
//        }
//        "push.comment" -> {//主动推送
//        val bean = Gson().fromJson<PushCMD>(text, PushCMD::class.java)
//        for (datum in bean.data) {
//        myLog("push.comment的评论==${datum.content}稿件id${datum.liveid}")
//        }
//
//        }
//        else -> {
//        }
//        }
//        }
//        }
//
//        override fun onMessage(bytes: ByteString) {
//        myLog("Websocket-----ByteString")
//        //val bitmap64 = ByteStringUtils.base64ToBitmap(bytes.base64())
//        // imv.setImageBitmap(bitmap64)
//
////            val bytes1 = bytes.toByteArray();
////            val bitmap= ByteStringUtils.bytesToBitmap(bytes1);
////            ByteStringUtils.bytesToFile(bytes1, path, begin+"."+end);
//
////            ByteStringUtils.outToFile(bytes,path,begin+"."+end);
//
//        }
//
//        override fun onReconnect() {
//        myLog("Websocket-----onReconnect")
//        }
//
//        override fun onClosing(code: Int, reason: String) {
//        myLog("Websocket-----onClosing")
//        timerTask?.cancel()
//        mTimer?.cancel()
//        mTimer?.purge()//清空队列
//        }
//
//        override fun onClosed(code: Int, reason: String) {
//        myLog("Websocket-----onClosed")
//        }
//
//        override fun onFailure(t: Throwable?, response: Response?) {
//        myLog("Websocket-----onFailure")
//        timerTask?.cancel()
//        mTimer?.cancel()
//        }
//        }
//
//        /**
//         *  连接ws
//         * @param content 大屏ws地址
//         * @param jwtToken wstoken
//         * @param liveId 视频直播或者广播id
//         */
//        fun connectWs(url: String, jwtToken: String, liveId: String) {
////        val wsurl1 = "ws://39.96.7.202/live?jwt_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcyI6InNwb3J0IiwidWRpZCI6Ijg2NjQxMzAzMDczMjE2NiIsImlzcyI6Imh0dHBzOlwvXC9hY3Rpdml0eS41Y2x1Yi5jY3R2LmNuIiwiYXVkIjoiY2N0di5jbiIsImV4cCI6MTYyMDk2OTQxNCwiaWF0IjoiMTYyMDgxMjQ1MiIsInNjb3JlIjoiMTYyMDgxMjQ1MjYwMDc5Iiwic3ViIjoiNTczZWY0NzU0MzFjZTA5ZGE1OGEzYTY1N2Q2ZTBhYTgifQ.k4qc_5XcWgmy4cEUblRGydIHGS-Sz9I5WCmyJs-vDw7DE4eZ2kNhmbQAlCpy5gzSvCv9C28avXckZUNnyqkO-JujVNAnh2QHsdMroMxT2GeUDE6d4dxWLGPgmAUnn3tR5RSGjYzPsn74aA6fktV0idQh962vaNYFhd59oJN6GYA&contribution_id=OlyL20210408093300000CH00000033"
//        val wsurl = "$url?jwt_token=$jwtToken&contribution_id=$liveId"
////        val url = "wss://echo.websocket.org"
//        myLog("==\n连接的ws==$wsurl")
//        if (webSocketUtils != null) {
//        webSocketUtils?.stopConnect()
//        webSocketUtils = null
//        }
//        //创建 websocket client
//        webSocketUtils = WebSocketUtils.Builder(App.app.applicationContext)
//        .client(OkHttpClient().newBuilder()
//        .pingInterval(50, TimeUnit.SECONDS)//设置WebSocket连接的保活
//        .retryOnConnectionFailure(true)
//        .build())
//        .needReconnect(true)
//        .wsUrl(wsurl)
//        .build()
//        webSocketUtils?.setWebSoketListener(webSoketListener)
//        webSocketUtils?.startConnect()
//        }
//
//        fun disConnect() {
//        if (webSocketUtils != null) {
//        webSocketUtils?.stopConnect()
//        webSocketUtils = null
//        }
//        }
//
//        fun sendMessage(content: String) {
//        if (!TextUtils.isEmpty(content)) {
//        if (webSocketUtils != null && webSocketUtils?.isWsConnected == true) {
//        val isSend: Boolean = webSocketUtils?.sendMessage(content) ?: false
//        if (isSend) {
//        myLog("消息发送成功==" + content)
//        } else {
//        myLog("消息发送失败\n" + content)
//        }
//        } else {
//        // "请先连接服务器"
//        myLog("请先连接服务器")
//        }
//        }
//        }


//
//override fun onDestroy() {
//    mainViewModel.disConnect()
//    mainViewModel.disconnect()
//    super.onDestroy()
//        val loaclBroadManage = LocalBroadcastManager.getInstance(this)//本地广播
//        loaclBroadManage.unregisterReceiver(chatMessageReceiver)
//        //  stopService(Intent(this, WebSocketClientService::class.java))
//        unbindService(serviceConnection)
//        if (null != jWebSClientService) {
//            jWebSClientService?.closeConnect()
//            jWebSClientService = null
//        }

//}

//    private var jWebSClientService: WebSocketClientService? = null
//
//    //    private var chatMessageReceiver: ChatMessageReceiver = ChatMessageReceiver()
//    private var chatMessageReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val message = intent?.getStringExtra("message")
//            //接受到 文本消息
//            myLog("接收 $message")
//        }
//    }
//
//    /**
//     * 动态注册广播
//     */
//    private fun doRegisterReceiver() {
//        val loaclBroadManage = LocalBroadcastManager.getInstance(this)//本地广播
//        val filter = IntentFilter("com.mhy.servicecallback.content")
//        loaclBroadManage.registerReceiver(chatMessageReceiver, filter)
//    }
//
//    /**
//     * 广播接收
//     */
//    private class ChatMessageReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            val message = intent.getStringExtra("message")
//            //接受到 文本消息
//            myLog("接收 $message")
//        }
//    }
//
//    /**
//     * 检测是否开启通知
//     *
//     * @param context
//     */
//    private fun checkNotification(context: Context) {
//        if (!isNotificationEnabled(context)) {
//            AlertDialog.Builder(context).setTitle("温馨提示")
//                    .setMessage("你还未开启系统通知，将影响消息的接收，要去开启吗？")
//                    .setPositiveButton("确定") { _, _ -> setNotification(context) }.setNegativeButton("取消", null).show()
//        }
//    }
//
//    /**
//     * 获取通知权限,监测是否开启了系统通知
//     *
//     * @param context
//     */
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private fun isNotificationEnabled(context: Context): Boolean {
//        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
//        return manager.areNotificationsEnabled()
//    }
//
//    /**
//     * 如果没有开启通知，跳转至设置界面
//     *
//     * @param context
//     */
//    private fun setNotification(context: Context) {
//        try {
//            val localIntent = Intent()
//            //直接跳转到应用通知设置的代码：
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
//                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
//                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//                localIntent.putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
//                //            localIntent.putExtra(Notification.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                localIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
//                localIntent.putExtra("app_package", context.packageName)
//                localIntent.putExtra("app_uid", context.applicationInfo.uid)
//            } else if (Build.VERSION.SDK_INT === Build.VERSION_CODES.KITKAT) {
//                localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                localIntent.addCategory(Intent.CATEGORY_DEFAULT)
//                localIntent.data = Uri.parse("package:" + context.packageName)
//            } else {
//                //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
//                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                if (Build.VERSION.SDK_INT >= 9) {
//                    localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    localIntent.data = Uri.fromParts("package", context.packageName, null)
//                } else if (Build.VERSION.SDK_INT <= 8) {
//                    localIntent.action = Intent.ACTION_VIEW
//                    localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
//                    localIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
//                }
//            }
//            context.startActivity(localIntent)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
//            val intent = Intent()
//            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//            val uri: Uri = Uri.fromParts("package", context.packageName, null)
//            intent.data = uri
//            context.startActivity(intent)
//        }
//    }
//
//    private var serviceConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
//            myLog("服务与活动成功绑定")
//            val binder = iBinder as WebSocketClientService.WebSocketClientBinder
//            jWebSClientService = binder.service
//            //            client = jWebSClientService.client;
//        }
//
//        override fun onServiceDisconnected(componentName: ComponentName) {
//            myLog("服务与活动成功断开")
//        }
//    }
//
//    private fun initService() {
//        //启动服务
//        // startService()
//        //绑定服务
//        bindService()
//        //注册广播
//        doRegisterReceiver()
//        //检测通知是否开启
//        checkNotification(this)
//
//    }
//
//    /**
//     * 启动服务（websocket客户端服务）  对应stopService
//     */
//    private fun startService() {
//        val intent = Intent(this, WebSocketClientService::class.java)
//        intent.putExtra("url", "wss://echo.websocket.org")
//        if (Build.VERSION.SDK_INT >= 26) {
//            startForegroundService(intent)//需要5秒内 启动forground通知
//        } else {
//            startService(intent)
//        }
//    }
//
//    /**
//     * 需要交互使用 绑定服务   不需要forground
//     */
//    private fun bindService() {
//        val bindIntent = Intent(this, WebSocketClientService::class.java)
//        intent.putExtra("url", "wss://echo.websocket.org")
//        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE)
//    }
//
//
//    fun sendMessage(content: String) {
//        if (jWebSClientService != null) {
//            val isSend = jWebSClientService?.sendMsg(content)
//            if (isSend == true) {
//
//            }
//        } else {
//            Toast.makeText(baseContext, "连接已断开，请稍等或重启App哟", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    fun connect(wsUrl: String) {
//        if (jWebSClientService != null) {
//            jWebSClientService?.connect(wsUrl);
//        }
//    }
//
//    fun disconnect() {
//        if (jWebSClientService != null) {
//            jWebSClientService?.closeConnect();
//        }
//    }
