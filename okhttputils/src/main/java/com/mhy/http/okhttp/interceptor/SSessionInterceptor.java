package com.mhy.http.okhttp.interceptor;


import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created By Mahongyin
 * Date    2022/5/18 10:42
 * 保持会话 跟headers一样
 */

public class SSessionInterceptor implements Interceptor {
    /**
     * 必须的请求头
     */
    private Map<String, String> headers;

    public SSessionInterceptor(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (headers != null) {
            // 将请求头添加至网络请求中
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder.build();
        return chain.proceed(request);
    }
}