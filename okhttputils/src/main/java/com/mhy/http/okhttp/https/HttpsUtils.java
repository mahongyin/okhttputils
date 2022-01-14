package com.mhy.http.okhttp.https;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Create by LEO
 * on 2018/3/20
 * at 14:40
 */
public class HttpsUtils {

    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }
    /**
     * 读取证书文件 Assets/cert.pem
     */
    public static InputStream getAssetsCer(Context context, String certName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(certName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
    public static InputStream getRawCer(Context context, int cerResID) {
        InputStream inputStream = null;
        inputStream = context.getResources().openRawResource(cerResID);
        return inputStream;
    }

    /**
     * 信任指定证书（传入字符串）
     */
    public static InputStream getStrCer(Context context, String cerStr) {
        InputStream inputStream = null;
        inputStream = new ByteArrayInputStream(cerStr.getBytes());
        return inputStream;
    }
    /**
     * 获取 SSLParams
     * null  null  null 为信任所有证书
     * notnull  null  null  验证证书
     * notnull  notnull  notnull 双向认证
     * @param certificates 证书的inputStream 可多个
     * @param bksFile 本地证书的inputStream
     * @param bksPassword 本地证书的密码
     */
    public static SSLParams getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String bksPassword) {
        SSLParams sslParams = new SSLParams();
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, bksPassword);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager;
            if (trustManagers != null) {//需要验证证书
                trustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else {//不验证
                trustManager = new UnSafeTrustManager();
            }
            sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);//new SecureRandom()
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            return sslParams;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        } catch (KeyStoreException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 不验证 信任全部
     */
    private static class UnSafeTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }


        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 准备信任管理器
     */
    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) {
            return null;
        }
        try {
//用我们的证书创建一个keystore
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            TrustManagerFactory trustManagerFactory = null;
//创建一个trustmanager，只信任我们创建的keystore
            trustManagerFactory = TrustManagerFactory.
                    getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            return trustManagers;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 准备 key管理器
     */
    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile == null || password == null) {
                return null;
            }
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 选择信任管理器
     */
    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    /**
     * 自己的的信任管理器
     */
    private static class MyTrustManager implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
            factory.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(factory.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }

        /**
         * 验证客户端证书
         */
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        /**
         * 验证服务端证书
         */
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //判断证书是否是本地信任列表里颁发的证书
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
            //mhyTag add的
            //获取网络中的证书信息
            X509Certificate certificate = chain[0];
            // 证书拥有者
            String subject = certificate.getSubjectDN().getName();
            // 证书颁发者
            String issuer = certificate.getIssuerDN().getName();
            Log.e("证书","证书拥有者"+subject+"\n证书颁发者"+issuer);
            //TODO
            //if (!"证书拥有者".equals(subject) || !"证书颁发者".equals(issuer)){
               // throw new CertificateException();
            //}

        }

        /**
         * 得到公认的发行人
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


    /**
     * 支持okhttp明文通信  okhttpclient.connectionSpecs(.getUnSafeConnectionSpecs())
     */
    public static ArrayList<ConnectionSpec> getUnSafeConnectionSpecs() {
        ArrayList<ConnectionSpec> connectionSpecs = new ArrayList<>();
        ConnectionSpec unSafeConnectionSpec = new ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT).build();
        connectionSpecs.add(unSafeConnectionSpec);
        ConnectionSpec safeConnectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .build();
        connectionSpecs.add(safeConnectionSpec);
        return connectionSpecs;
    }


    /************************************* 信任所有证书 *******************************************/

    /**信任所有的证书的OkHttpClient 示例*/
    public OkHttpClient getTrustAllClient() {
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory(), mMyTrustManager)
                .hostnameVerifier(new TrustAllHostnameVerifier());
        return mBuilder.build();
    }
    private AllTrustManager mMyTrustManager;

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            mMyTrustManager = new AllTrustManager();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    /**实现X509TrustManager接口*/
    public static class AllTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**实现HostnameVerifier接口*/
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;//始终验证成功
        }
    }


}