package com.mhy.websoket;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mhy.http.websocket.listener.WebSoketListener;
import com.mhy.sample_okhttp.R;

import okhttp3.Response;
import okio.ByteString;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    //    private WebSocketUtils client;
    private WebSocketClientService.WebSocketClientBinder binder;
    private WebSocketClientService jWebSClientService;
    private EditText edit_url;
    private EditText et_content;
    private TextView btn_send;
    private TextView tv_content;
    private TextView btn_disconnect, btn_connect;

    private ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("mhylog", "服务与活动成功绑定");
            binder = (WebSocketClientService.WebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
//            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("mhylog", "服务与活动成功断开");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_socket);
        mContext = MainActivity.this;
        initNotfChanal();
        findViewById();
        initService();
        initView();
    }

    private void initNotfChanal() {
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

    private void initService() {
        //启动服务
        startJWebSClientService();
        //绑定服务
        bindService();
        //检测通知是否开启
        checkNotification(mContext);
    }

    /**
     * 启动服务（websocket客户端服务）
     */
    private void startJWebSClientService() {
        Intent intent = new Intent(mContext, WebSocketClientService.class);
        startService(intent);

    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(mContext, WebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }


    private void findViewById() {
        btn_send = findViewById(R.id.btn_send);
        et_content = (EditText) findViewById(R.id.edit_content);
        edit_url = (EditText) findViewById(R.id.edit_url);
        btn_send.setOnClickListener(this);
        tv_content = (TextView) findViewById(R.id.tv_content);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(this);
        btn_connect = findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);
    }

    private void initView() {
        //监听输入框的变化
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (et_content.getText().toString().length() > 0) {
                    btn_send.setVisibility(View.VISIBLE);
                } else {
                    btn_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                String content = et_content.getText().toString();
                if (content.length() <= 0) {
                    Toast.makeText(getBaseContext(), "请填写需要发送的内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (jWebSClientService != null) {
                    boolean isSend = jWebSClientService.sendMsg(content);
                    if (isSend) {
                        //暂时将发送的消息加入消息列表，实际以发送成功为准（也就是服务器返回你发的消息时）
                        tv_content.append(Spanny
                                .spanText("我 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                                        DateUtils.FORMAT_SHOW_TIME) + "\n",
                                        new ForegroundColorSpan(
                                                ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
                        tv_content.append(content + "\n\n");
                        et_content.setText("");
                        showOrHideInputMethod();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "连接已断开，请稍等或重启App哟", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_connect:
                if (jWebSClientService != null) {
                    jWebSClientService.connect(edit_url.getText().toString(),webSoketListener);
                }
                break;
            case R.id.btn_disconnect:
                if (jWebSClientService != null) {
                    jWebSClientService.closeConnect();
                }
                break;
            default:
                break;
        }
    }
    /**
     * 检查锁屏状态，如果锁屏先点亮屏幕
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
        notifyManager.notify(SERVICE_ID, builder.build());
    }
    private final static int SERVICE_ID = 1000;
    private static String TAG = "mhylog";
    private WebSoketListener webSoketListener = new WebSoketListener() {
        @Override
        public void onOpen(Response response) {
            //连接成功 通知
            Log.e(TAG, "websocket连接成功");
            tv_content.append(Spanny.spanText("服务器连接成功\n\n", new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
        }

        final String message = "{\"cmd\":\"msg.ping\"}";

        @Override
        public void onMessage(String message) {
            //接受到 文本消息
            Log.e("JWebSocketClientService", "收到的消息：" + message);
            checkLockAndShowNotification(message);
            //接受到 文本消息
            tv_content.append(Spanny
                    .spanText("服务器 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME) + "\n",
                            new ForegroundColorSpan(
                                    ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
            tv_content.append(message + "\n\n");
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


    //隐藏 键盘
    private void showOrHideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(mContext, WebSocketClientService.class));
        if (serviceConnection!=null){
            unbindService(serviceConnection);
            serviceConnection=null;
        }
          if (null!=jWebSClientService){
            jWebSClientService.closeConnect();
            jWebSClientService=null;
        }
    }

    /**
     * 检测是否开启通知
     *
     * @param context
     */
    private void checkNotification(final Context context) {
        if (!isNotificationEnabled(context)) {
            new AlertDialog.Builder(context).setTitle("温馨提示")
                    .setMessage("你还未开启系统通知，将影响消息的接收，要去开启吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNotification(context);
                        }
                    }).setNegativeButton("取消", null).show();
        }
    }

    /**
     * 获取通知权限,监测是否开启了系统通知
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }

    /**
     * 如果没有开启通知，跳转至设置界面
     *
     * @param context
     */
    private void setNotification(Context context) {
        try {
            Intent localIntent = new Intent();
            //直接跳转到应用通知设置的代码：
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                localIntent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
//            localIntent.putExtra(Notification.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                localIntent.putExtra("app_package", context.getPackageName());
                localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                localIntent.addCategory(Intent.CATEGORY_DEFAULT);
                localIntent.setData(Uri.parse("package:" + context.getPackageName()));
            } else {
                //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
                }
            }
            context.startActivity(localIntent);
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

}
