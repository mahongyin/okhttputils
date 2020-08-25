package com.mhy.http.okhttp.callback;

/**
 * Created by JimGong on 2016/6/23.
 * 泛型序列化
 */
public interface IGenericsSerializator {
    <T> T transform(String response, Class<T> classOfT);
}
