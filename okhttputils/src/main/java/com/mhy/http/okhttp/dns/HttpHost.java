package com.mhy.http.okhttp.dns;

import android.util.Log;

import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * @author mahongyin
 * 动态替换/改变host，schema等参数
 * 判断 host 是否 目标host 合适的地方或每次啥操作加个接口 调用.replaceOriginalRequest(call, request)
 */

public class HttpHost {

    public static void replaceOriginalRequest(Call call, Request request) {
        HttpUrl httpUrl = request.url().newBuilder()
                .host("目标host")
                .scheme("http")
                .build();
        Request newRequest = request.newBuilder()
                .url(httpUrl)
                .build();
        try {
            //反射修改Call
            Class<?> callClass = call.getClass();
            Field field = callClass.getDeclaredField("originalRequest");
            field.setAccessible(true);
            field.set(call, newRequest);
        } catch (Exception e) {
            String err = e.getLocalizedMessage();
            Log.e("error", err);
        }
    }
}
