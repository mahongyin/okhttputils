package com.mhy.websoket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mhy.http.websocket.ByteFileType;
import com.mhy.http.websocket.ByteStringUtils;
import com.mhy.http.websocket.WebSocketUtils;
import com.mhy.http.websocket.listener.WebSoketListener;
import com.mhy.sample_okhttp.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class SocketActivity extends AppCompatActivity {

    private final static String TAG = "SocketActivity";
    private TextView btn_send, btn_clear, tv_content;
    private Button btn_connect, btn_disconnect;
    private EditText edit_url, edit_content;
    private ImageView imv;
    private WebSocketUtils webSocketUtils;
    private WebSoketListener webSoketListener = new WebSoketListener() {
        @Override
        public void onOpen(Response response) {
            //连接成功 通知
            tv_content.append(Spanny.spanText("服务器连接成功\n\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
        }

        @Override
        public void onMessage(String text) {
            //接受到 文本消息
            tv_content.append(Spanny
                    .spanText("服务器 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME) + "\n",
                            new ForegroundColorSpan(
                                    ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
            tv_content.append(fromHtmlText(text) + "\n\n");
        }

        @Override
        public void onMessage(final ByteString bytes) {
            // 开始时间
//            long begin = System.currentTimeMillis();
//            String path = getExternalFilesDir("").getAbsolutePath();
//            String end = ByteFileType.getFileTypeByStream(bytes.substring(0, 10).toByteArray());
//
//            byte[] bytes1 = bytes.toByteArray();

            Bitmap bitmap64 = ByteStringUtils.base64ToBitmap(bytes.base64());
//            Bitmap bitmap= ByteStringUtils.bytesToBitmap(bytes1);
            imv.setImageBitmap(bitmap64);
//            ByteStringUtils.bytesToFile(bytes1, path, begin+"."+end);
//            ByteStringUtils.outToFile(bytes,path,begin+"."+end);
//            new DownloadTask(bytes1,path,begin+"."+end).execute();
            //接收到 文件
        }

        class DownloadTask extends AsyncTask<Void, Integer, File> {
            byte[] bytes;
            String dirPath;
            String name;

            DownloadTask(byte[] bytes, String dirPath, String name) {
                this.bytes = bytes;
                this.dirPath = dirPath;
                this.name = name;
            }

            @Override
            protected void onPreExecute() {
                Log.i("mhyLog", "开始");
            }

            @Override
            protected File doInBackground(Void... voids) {
                Log.i("mhyLog", "child开始下载");
                return ByteStringUtils.bytesToFile(bytes, dirPath, name);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                Log.i("mhyLog", "进度" + values[0]);
            }

            @Override
            protected void onPostExecute(File file) {
                if (null != file && file.exists()) {
                    Log.i("mhyLog", "下载成功");
                } else {
                    Log.i("mhyLog", "下载失败");
                }
            }
        }

        @Override
        public void onReconnect() {
            Log.d(TAG, "Websocket-----onReconnect");
            tv_content.append(Spanny.spanText("服务器重连接中...\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onClosing(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosing");
            tv_content.append(Spanny.spanText("服务器连接关闭中...\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onClosed(int code, String reason) {
            Log.d(TAG, "Websocket-----onClosed");
            tv_content.append(Spanny.spanText("服务器连接已关闭\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            Log.d(TAG, "Websocket-----onFailure");
            tv_content.append(Spanny.spanText("服务器连接失败\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        initView();
        initEvent();

    }

    private void initEvent() {
        //连接
        btn_connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = edit_url.getText().toString();
                if (!TextUtils.isEmpty(url) && url.contains("ws")) {
                    if (webSocketUtils != null) {
                        webSocketUtils.stopConnect();
                        webSocketUtils = null;
                    }
                    //创建 websocket client
                    webSocketUtils = new WebSocketUtils.Builder(getBaseContext())
                            .client(new OkHttpClient().newBuilder()
                                    .pingInterval(15, TimeUnit.SECONDS)
                                    .retryOnConnectionFailure(true)
                                    .build())
                            .needReconnect(true)
                            .wsUrl(url)
                            .build();
                    webSocketUtils.setWebSoketListener(webSoketListener);
                    webSocketUtils.startConnect();
                } else {
                    Toast.makeText(getBaseContext(), "请填写需要链接的地址", Toast.LENGTH_SHORT).show();
                }
            }
        });
//断开
        btn_disconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketUtils != null) {
                    webSocketUtils.stopConnect();
                    webSocketUtils = null;
                }
            }
        });
//发送
        btn_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edit_content.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    if (webSocketUtils != null && webSocketUtils.isWsConnected()) {
                        boolean isSend = webSocketUtils.sendMessage(content);
                        if (isSend) {
                            tv_content.append(Spanny.spanText(
                                    "我 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                                            DateUtils.FORMAT_SHOW_TIME) + "\n", new ForegroundColorSpan(
                                            ContextCompat.getColor(getBaseContext(), android.R.color.holo_green_light))));
                            tv_content.append(content + "\n\n");
                        } else {
                            tv_content.append(Spanny.spanText("消息发送失败\n", new ForegroundColorSpan(
                                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
                        }
                        showOrHideInputMethod();
                        edit_content.setText("");
                    } else {
                        Toast.makeText(getBaseContext(), "请先连接服务器", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "请填写需要发送的内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_send.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory(), "parser0.jpg");
                if (!file.exists()) {
                    Toast.makeText(SocketActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (webSocketUtils != null && webSocketUtils.isWsConnected()) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
//
                        BufferedInputStream bis = new BufferedInputStream(inputStream);
//String b64=Base64Utils.encode(Base64Utils.getBytesByFile(file.getAbsolutePath()));
//Log.i("mhyLog",b64);
//                        ByteString data=ByteString.decodeBase64(b64);
                        ByteString data2 = ByteString.read(bis, (int) file.length());
                        boolean isSend = webSocketUtils.sendMessage(data2);
                        if (isSend) {
                            tv_content.append(Spanny.spanText(
                                    "我 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                                            DateUtils.FORMAT_SHOW_TIME) + "\n", new ForegroundColorSpan(
                                            ContextCompat.getColor(getBaseContext(), android.R.color.holo_green_light))));
                            tv_content.append("发图" + "\n\n");
                        }
                        bis.close();
                        inputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_content.setText("");
            }
        });
    }

    private void initView() {
        btn_send = (TextView) findViewById(R.id.btn_send);
        btn_clear = (TextView) findViewById(R.id.btn_clear);
        tv_content = (TextView) findViewById(R.id.tv_content);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        edit_url = (EditText) findViewById(R.id.edit_url);
        edit_content = (EditText) findViewById(R.id.edit_content);
        imv = findViewById(R.id.imv);

    }

    @Override
    protected void onDestroy() {
        if (webSocketUtils != null) {
            webSocketUtils.stopConnect();
            webSocketUtils = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_source:
                //调起浏览器更新app
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri url = Uri.parse("https://github.com/mahongyin");
                intent.setData(url);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Spanned fromHtmlText(String s) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(s);
        }
        return result;
    }

    //隐藏 键盘
    private void showOrHideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
