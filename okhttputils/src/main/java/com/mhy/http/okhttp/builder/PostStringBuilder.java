package com.mhy.http.okhttp.builder;

import com.mhy.http.okhttp.request.PostStringRequest;
import com.mhy.http.okhttp.request.RequestCall;

import okhttp3.MediaType;

/**
 * Created by zhy on 15/12/14.
 */
public class PostStringBuilder extends OkHttpRequestBuilder<PostStringBuilder> {
    private String content;//json
    private MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//    private MediaType mediaType = MediaType.parse("application/xml; charset=utf-8");
//    private MediaType mediaType = MediaType.parse("application/javascript; charset=utf-8");
//    private MediaType mediaType = MediaType.parse("text/html; charset=utf-8");
//    private MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");


    public PostStringBuilder content(String content) {
        this.content = content;
        return this;
    }

    public PostStringBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostStringRequest(url, tag, params, headers, content, mediaType, id).build();
    }


}
