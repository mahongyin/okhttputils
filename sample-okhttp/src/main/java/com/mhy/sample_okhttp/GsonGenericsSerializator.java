package com.mhy.sample_okhttp;

import com.google.gson.Gson;
import com.mhy.http.okhttp.callback.IGenericsSerializator;

/**
 * Created by JimGong on 2016/6/23.
 */
public class GsonGenericsSerializator implements IGenericsSerializator {
    Gson mGson = new Gson();
    @Override
    public <T> T transform(String response, Class<T> classOfT) {
        return mGson.fromJson(response, classOfT);
    }
}
