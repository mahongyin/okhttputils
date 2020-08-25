package com.mhy.http.okhttp.interceptor;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Create by LEO
 * on 2018/11/19
 * at 10:36
 * 缓存优先
 */
public class CachePriorityInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String cacheControl = request.cacheControl().toString();
        if (TextUtils.isEmpty(cacheControl)) {
            cacheControl = "public, max-age=86400";
        }
        return response.newBuilder()
                .header("Cache-Control", cacheControl)
                .removeHeader("Pragma")
                .build();
    }
}
