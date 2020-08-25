package com.mhy.sample_okhttp;

import com.alibaba.fastjson.JSON;
import com.mhy.http.okhttp.callback.IGenericsSerializator;

/**
 * Created By Mahongyin
 * Date    2020/8/24 17:02
 */
public class FastGenericsSerializator  implements IGenericsSerializator {

    @Override
    public <T> T transform(String response, Class<T> classOfT) {
        return JSON.parseObject(response,classOfT);
    }
}
