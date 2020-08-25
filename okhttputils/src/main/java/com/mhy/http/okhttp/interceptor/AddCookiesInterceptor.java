package com.mhy.http.okhttp.interceptor;

/**
 * Created By Mahongyin
 * Date    2020/8/24 17:36
 */
import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author    : mahongyin
 * e-mail    : mhy.work@qq.com
 * date      : 2019/8/14 13:55
 * introduce :cookie 添加器
 */

public class AddCookiesInterceptor implements Interceptor {
    private Context context;

    public AddCookiesInterceptor(Context context) {
        super();
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request.Builder builder = chain.request().newBuilder();
        SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);

        String cookie = sharedPreferences.getString("cookie", "android");
        builder.addHeader("Cookie", cookie);
        return chain.proceed(builder.build());
    }
}