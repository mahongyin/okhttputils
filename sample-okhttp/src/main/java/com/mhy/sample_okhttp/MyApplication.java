package com.mhy.sample_okhttp;

import android.app.Application;
import android.os.Environment;

import com.mhy.http.okhttp.OkHttpUtils;
import com.mhy.http.okhttp.cookie.CookieJarImpl;
import com.mhy.http.okhttp.cookie.store.PersistentCookieStore;
import com.mhy.http.okhttp.dns.TimeoutDNS;
import com.mhy.http.okhttp.https.HttpsUtils;
import com.mhy.http.okhttp.interceptor.RetryIntercepter;
import com.mhy.http.okhttp.log.LoggerInterceptor;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

/**
 * Created by zhy on 15/8/25.
 */
public class MyApplication extends Application {

    private String CER_12306 = "-----BEGIN CERTIFICATE-----\n" +
            "MIICmjCCAgOgAwIBAgIIbyZr5/jKH6QwDQYJKoZIhvcNAQEFBQAwRzELMAkGA1UEBhMCQ04xKTAn\n" +
            "BgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMB4X\n" +
            "DTA5MDUyNTA2NTYwMFoXDTI5MDUyMDA2NTYwMFowRzELMAkGA1UEBhMCQ04xKTAnBgNVBAoTIFNp\n" +
            "bm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMIGfMA0GCSqGSIb3\n" +
            "DQEBAQUAA4GNADCBiQKBgQDMpbNeb34p0GvLkZ6t72/OOba4mX2K/eZRWFfnuk8e5jKDH+9BgCb2\n" +
            "9bSotqPqTbxXWPxIOz8EjyUO3bfR5pQ8ovNTOlks2rS5BdMhoi4sUjCKi5ELiqtyww/XgY5iFqv6\n" +
            "D4Pw9QvOUcdRVSbPWo1DwMmH75It6pk/rARIFHEjWwIDAQABo4GOMIGLMB8GA1UdIwQYMBaAFHle\n" +
            "tne34lKDQ+3HUYhMY4UsAENYMAwGA1UdEwQFMAMBAf8wLgYDVR0fBCcwJTAjoCGgH4YdaHR0cDov\n" +
            "LzE5Mi4xNjguOS4xNDkvY3JsMS5jcmwwCwYDVR0PBAQDAgH+MB0GA1UdDgQWBBR5XrZ3t+JSg0Pt\n" +
            "x1GITGOFLABDWDANBgkqhkiG9w0BAQUFAAOBgQDGrAm2U/of1LbOnG2bnnQtgcVaBXiVJF8LKPaV\n" +
            "23XQ96HU8xfgSZMJS6U00WHAI7zp0q208RSUft9wDq9ee///VOhzR6Tebg9QfyPSohkBrhXQenvQ\n" +
            "og555S+C3eJAAVeNCTeMS3N/M5hzBRJAoffn3qoYdAO1Q8bTguOi+2849A==\n" +
            "-----END CERTIFICATE-----";


    @Override
    public void onCreate() {
        super.onCreate();
        initOkHttp();


    }

    private void initOkHttp() {
        //配置cookie
        CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
//        CookieJarImpl cookieJar  = new CookieJarImpl(new MemoryCookieStore());
        // 指定缓存路径,缓存大小30Mb
//        Cache cache = new Cache(new File(getCacheDir(),"Httpcache"), 1024 * 1024 * 20);
        Cache cache2 = new Cache(new File(Environment.getExternalStorageDirectory(), "Httpcache"), 1024 * 1024 * 30);

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);

//        CookieJarImpl cookieJar1 = new CookieJarImpl(new MemoryCookieStore());
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggerInterceptor("OkHttp"))
                .cookieJar(cookieJar).cache(cache2)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        //强行返回true 强行验证成功
                        return true;
                    }
                }).proxy(Proxy.NO_PROXY)//防代理 抓包
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }


    private void initHttp() {
        ArrayList<ConnectionSpec> unSafeConnectionSpecs = HttpsUtils.getUnSafeConnectionSpecs();
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .connectionSpecs(unSafeConnectionSpecs)
                .dns(new TimeoutDNS(3, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .addInterceptor(new RetryIntercepter(2))//重试
                .addInterceptor(new LoggerInterceptor("OKHTTP", true))
                .proxy(Proxy.NO_PROXY)
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
}
