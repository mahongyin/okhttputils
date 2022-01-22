package com.mhy.sample_okhttp;

import android.app.Application;

import com.mhy.http.okhttp.OkHttpUtils;
import com.mhy.http.okhttp.cookie.CookieJarImpl;
import com.mhy.http.okhttp.cookie.store.PersistentCookieStore;
import com.mhy.http.okhttp.dns.HttpDns;
import com.mhy.http.okhttp.dns.TimeoutDNS;
import com.mhy.http.okhttp.https.HttpsUtils;
import com.mhy.http.okhttp.interceptor.RetryIntercepter;
import com.mhy.http.okhttp.log.LoggerInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

/**
 * Created by mhy on 20/8/25.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initOkHttp();
    }

    /**验证域名*/
    private final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
//            if ("域名host.com".equals(hostname)) {
//                return true;
//            } else {
//                HostnameVerifier hv= HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify("域名host.com", session);
//            }
            //强行返回true 强行验证成功
            return true;
        }
    };
    private final ProxySelector proxySelector = new ProxySelector() {
        @Override
        public List<Proxy> select(URI uri) {
//            DebugLog.e("url"+uri.getHost());
//wifi只能设置一个代理  蜂窝网络可以设置多个  
//可以根据uri值针对敏感接口返回null,不敏感接口就使用默认代理
            return getDefault().select(uri); //null 不要任何代理
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

        }
    };

    private void initOkHttp() {
//        CookieJarImpl cookieJar  = new CookieJarImpl(new MemoryCookieStore());//内存中
        // 指定缓存路径,缓存大小30Mb
//        Cache cache = new Cache(new File(getCacheDir(),"Httpcache"), 1024 * 1024 * 30);
//        Cache cache = new Cache(new File(Environment.getExternalStorageDirectory(), "Httpcache"), 1024 * 1024 * 30);
        CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext())); //配置cookie //持久化cookie
        Cache cache = new Cache(new File(getExternalCacheDir(), "Httpcache"), 1024 * 1024 * 30);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(new InputStream[]{HttpsUtils.getAssetsCer(getApplicationContext(),
//                "zhy_server.cer")}, null,null);
        ArrayList<ConnectionSpec> unSafeConnectionSpecs = HttpsUtils.getUnSafeConnectionSpecs();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggerInterceptor("OkHttp",true))
                .cookieJar(cookieJar)
                .connectionSpecs(unSafeConnectionSpecs)
                .cache(cache)
                .retryOnConnectionFailure(true)
                .addInterceptor(new RetryIntercepter(3))//重试
                .dns(new TimeoutDNS(3, TimeUnit.SECONDS))//dns解析3秒
//                .dns(new HttpDns())
                .hostnameVerifier(hostnameVerifier)
//                .proxySelector(proxySelector)
//                .proxy(Proxy.NO_PROXY)//防代理 抓包
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
}
