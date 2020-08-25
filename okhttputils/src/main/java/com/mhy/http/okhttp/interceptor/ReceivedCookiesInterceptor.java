package com.mhy.http.okhttp.interceptor;

/**
 * Created By Mahongyin
 * Date    2020/8/24 17:42
 */
import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * author    : mahongyin
 * e-mail    : mhy.work@qq.com
 * date      : 2019/8/14 13:55
 * introduce :cookie拦截器
 */

public class ReceivedCookiesInterceptor implements Interceptor {

    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        super();
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response originalResponse = chain.proceed(chain.request());
        //这里获取请求返回的cookie
        if (!originalResponse.headers("set-cookie").isEmpty()) {

            String cookie = originalResponse.header("set-cookie").split(";")[0];
            SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("cookie", cookie);
            editor.apply();
        }

        return originalResponse;
    }
}