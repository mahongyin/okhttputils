package com.mhy.http.okhttp.builder;

import com.mhy.http.okhttp.OkHttpUtils;
import com.mhy.http.okhttp.request.OtherRequest;
import com.mhy.http.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder {
    @Override
    public RequestCall build() {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers, id).build();
    }
}
