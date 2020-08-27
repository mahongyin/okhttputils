package com.mhy.websoket;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mhy.sample_okhttp.R;


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
    private ChatMessageReceiver chatMessageReceiver;

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
        initService();
        findViewById();
        initView();
    }

    private void initService() {
        //启动服务
        startJWebSClientService();
        //绑定服务
        bindService();
        //注册广播
        doRegisterReceiver();
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
//        serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                Log.e("mhylog", "服务与活动成功绑定");
//                binder = (WebSocketClientService.WebSocketClientBinder) iBinder;
//                jWebSClientService = binder.getService();
////                client = jWebSClientService.client;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName componentName) {
//                Log.e("mhylog", "服务与活动成功断开");
//            }
//        };
        Intent bindIntent = new Intent(mContext, WebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 动态注册广播
     */
    private void doRegisterReceiver() {
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("com.mhy.servicecallback.content");
        registerReceiver(chatMessageReceiver, filter);
    }

    /**
     * 广播接收
     */
    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            //接受到 文本消息
            tv_content.append(Spanny
                    .spanText("服务器 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME) + "\n",
                            new ForegroundColorSpan(
                                    ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
            tv_content.append(message + "\n\n");
        }
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
                    jWebSClientService.connect(edit_url.getText().toString());
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


    //隐藏 键盘
    private void showOrHideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(chatMessageReceiver);

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
