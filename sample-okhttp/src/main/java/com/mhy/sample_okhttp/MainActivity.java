package com.mhy.sample_okhttp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mhy.http.net.ConnectivityStatus;
import com.mhy.http.net.NetStatus;
import com.mhy.http.okhttp.OkHttpUtils;
import com.mhy.http.okhttp.callback.BitmapCallback;
import com.mhy.http.okhttp.callback.FileCallBack;
import com.mhy.http.okhttp.callback.GenericsCallback;
import com.mhy.http.okhttp.callback.StringCallback;
import com.mhy.http.okhttp.cookie.CookieJarImpl;
import com.mhy.http.okhttp.utils.NetUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private String mBaseUrl = "https://wanandroid.com/";

    private static final String TAG = "MainActivity";

    private TextView mTv;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public void webSocket1(View view) {
        startActivity(new Intent(this, com.mhy.websoket.MainActivity.class));
    }
    public void webSocket2(View view) {
        startActivity(new Intent(this, com.mhy.websoket.SocketActivity.class));
    }


    public class MyStringCallback extends StringCallback {
        @Override
        public void onBefore(Request request, int id) {
            setTitle("loading...");
        }

        @Override
        public void onAfter(int id) {
            setTitle("Sample-okHttp");
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            mTv.setText("onError:" + e.getMessage());
        }

        @Override
        public void onResponse(String response, int id) {
            Log.e(TAG, "onResponse：complete");
            mTv.setText("onResponse:" + response);

            switch (id) {
                case 100:
                    Toast.makeText(MainActivity.this, "http", Toast.LENGTH_SHORT).show();
                    break;
                case 101:
                    Toast.makeText(MainActivity.this, "https", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void inProgress(float progress, long total, int id) {
            Log.e(TAG, "inProgress:" + progress);
            mProgressBar.setProgress((int) (100 * progress));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTv = (TextView) findViewById(R.id.id_textview);
        mImageView = (ImageView) findViewById(R.id.id_imageview);
        mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
        mProgressBar.setMax(100);

        initNetStatus();
    }
   private NetStatus newStatus;
    void initNetStatus() {
        newStatus= new NetStatus();
//        newStatus.registerObserver(this, new NetStatus.NetworkListener() {
//            @Override
//            public void onStatus(ConnectivityStatus status) {
//                Log.e("net状态", status.getStatus());
//                Toast.makeText(MainActivity.this,status.getStatus() , Toast.LENGTH_SHORT).show();
//            }
//        });
        Toast.makeText(MainActivity.this,NetStatus.getNetType(this).getStatus() , Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "当前是移动网络="+NetUtils.isMobileisAvailable(this), Toast.LENGTH_SHORT).show();
    }

    public void getHtml(View view) {
        String url = "http://www.wanandroid.com/blog/show/2";
        OkHttpUtils
                .get()
                .url(url)
                .id(100)
                .build()
                .execute(new MyStringCallback());
    }
    public void getHttpsHtml(View view) {
        String url = "https://www.wanandroid.com/blog/show/2";

        OkHttpUtils
                .get()//
                .url(url)//
                .id(101)
                .build()//
                .execute(new MyStringCallback());

    }

    public void postString(View view) {
        String url = mBaseUrl + "user!postString";
        OkHttpUtils
                .postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(new Gson().toJson(new User("mhy", "123456")))
                .build()
                .execute(new MyStringCallback());

    }

    public void postFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "parser0.jpg");
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "https://a1000.top/up";
        OkHttpUtils
                .postFile()
                .url(url)
                .file(file)
                .build()
                .execute(new MyStringCallback());

    }

    public void getUser(View view) {
        String url = "https://www.wanandroid.com/user/login";
        OkHttpUtils
                .post()//
                .url(url)//
                .addParams("username", "name")//
                .addParams("password", "password")//
                .build()//
                .execute(new GenericsCallback<User>(new GsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(User response, int id) {
                        mTv.setText("onResponse:" + response.getErrorCode());
                    }
                });
    }
    public void getDemo(View view) {
        String url =  "https://www.wanandroid.com//hotkey/json";
        String url1 = "https://www.wanandroid.com/friend/json";
        String url2 =  "https://www.wanandroid.com/banner/json";
        OkHttpUtils
                .get()//
                .url(url)//
                .build()//
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        mTv.setText("onResponse:" + response);
                    }
                });
    }


    public void getUsers(View view) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", "mhy");
        params.put("password", "123");
        String url = "https://www.wanandroid.com/user/login";
        OkHttpUtils//
                .post()//
                .url(url)//
                .params(params)//
                .build()//
                .execute(new StringCallback()//
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        mTv.setText("onResponse:" + response);
                    }
                });
    }


    public void getImage(View view) {
        mTv.setText("");
        String url = "https://images.csdn.net/20150817/1.jpg";
        OkHttpUtils
                .get()//
                .url(url)//
                .tag(this)//
                .build()//
                .connTimeOut(20000)
                .readTimeOut(20000)
                .writeTimeOut(20000)
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        Log.e("TAG", "onResponse：complete");
                        mImageView.setImageBitmap(bitmap);
                    }
                });
    }


    public void uploadFile(View view) {

        File file = new File(Environment.getExternalStorageDirectory(), "parser0.jpg");
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("user", "123456");
        params.put("token", "123456abc");

        Map<String, String> headers = new HashMap<>();
        headers.put("APP-Key", "APP-Secret222");
        headers.put("APP-Secret", "APP-Secret111");

        String url = "https://a1000.top/up";

        OkHttpUtils.post()//
                .addFile("file", "parser0.jpg", file)//
                .url(url)//
                .params(params)//
                .headers(headers)//
                .build()//
                .execute(new MyStringCallback());
    }


    public void multiFileUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "parser0.jpg");
        File file2 = new File(Environment.getExternalStorageDirectory(), "parser1.jpg");
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("user", "123");
        params.put("token", "123abc");

        String url = "https://a1000.top/up/index.php";
        OkHttpUtils.post()//
                .addFile("file", "parser0.jpg", file)//
                .addFile("mFile", "test1.txt", file2)//
                .url(url)
                .params(params)//
                .build()//
                .execute(new MyStringCallback());
    }


    public void downloadFile(View view) {
        String url = "https://github.com/hongyangAndroid/okhttp-utils/blob/master/okhttputils-2_6_2.jar?raw=true";
        OkHttpUtils//
                .get()//
                .url(url)//
                .build()//
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "gson-2.2.1.jar")//
                {

                    @Override
                    public void onBefore(Request request, int id) {
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        mProgressBar.setProgress((int) (100 * progress));
                        Log.e(TAG, "inProgress :" + (int) (100 * progress));
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        Log.e(TAG, "onResponse :" + file.getAbsolutePath());
                    }
                });
    }


    public void otherRequestDemo(View view) {
        //also can use delete ,head , patch
        /*
        OkHttpUtils
                .put()//
                .url("http://11111.com")
                .requestBody
                        ("may be something")//
                .build()//
                .execute(new MyStringCallback());



        OkHttpUtils
                .head()//
                .url(url)
                .addParams("name", "zhy")
                .build()
                .execute();

       */


    }

    public void clearSession(View view) {
        CookieJar cookieJar = OkHttpUtils.getInstance().getOkHttpClient().cookieJar();
        if (cookieJar instanceof CookieJarImpl) {
            ((CookieJarImpl) cookieJar).getCookieStore().removeAll();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
        if (newStatus!=null){
            newStatus.unRegisterObserver(this);
        }
    }
}
