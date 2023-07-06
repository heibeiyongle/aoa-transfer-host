package com.zhe.req.reghelper.network;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

public class HttpManagerNew {
    private static final int CONNECTION_TIME_OUT = 10;
    private static final int CONNECTION_TIME_OUT_3 = 3;
    private static final String TAG = "HttpManagerNew";
    private static HttpManagerNew mInstance;
    private static OkHttpClient mOkHttpClient;
    /**
     * 3秒超时时间请求客户端
     */
    private static OkHttpClient mOkHttpClient2;
    private static CommonDelegate commonDelegate;
    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    private final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain;charset=utf-8");
    private final String X_VERSION = "x-version";
    private final String X_VERSION_CODE = "zhe.com";
    private final ResultCallback DEFAULT_RESULT_CALLBACK = new ResultCallback() {

        @Override
        public void onCodeLoginError() {
        }

        @Override
        public void onCodeError(int code) {

        }

        @Override
        public void onNetError(Request request, Exception e) {

        }

        @Override
        public void onSuccess(String response) {

        }

        @Override
        public void onNotBindPhoneErr() {

        }
    };
    private Handler mDelivery;


    /**
     * 是否是短连接超时模式
     */
    private static boolean mIsShortMode = false;

    private HttpManagerNew() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        mDelivery = new Handler(Looper.getMainLooper());
        commonDelegate = new CommonDelegate();
    }

    /**
     * 注意在该模式使用完毕之后要切换到正常态，该方法会影响到同时请求的其它请求
     *
     * @param switchTo
     */
    public static void switchToShortConnectTimeMode(boolean switchTo) {
        synchronized (HttpManagerNew.class) {
            if (mOkHttpClient2 == null) {
                mOkHttpClient2 = new OkHttpClient.Builder()
                        .connectTimeout(CONNECTION_TIME_OUT_3, TimeUnit.SECONDS)
                        .readTimeout(CONNECTION_TIME_OUT_3, TimeUnit.SECONDS)
                        .writeTimeout(CONNECTION_TIME_OUT_3, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(false)
                        .build();
            }
        }

        mIsShortMode = switchTo;
    }

    /**
     * 此方法需要在application中调用来启动
     */

    public static void init() {
        if (mInstance == null) {
            synchronized (HttpManagerNew.class) {
                if (mInstance == null) {
                    mInstance = new HttpManagerNew();
                }
            }
        }
    }

    /**
     * 非阻塞get请求
     * 使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param resultCallback
     */
    public static void getAsyn(Object tag, String url, Map<String, String> urlParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.GET, tag, url, urlParams, null, resultCallback);
    }

    /**
     * 非阻塞get请求
     * 使用默认url参数 2016年9月19日17:46:18
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param resultCallback
     */
    public static void getAsyn(Object tag, String url, Map<String, String> urlParams, JSONObject postParams, boolean isUseDefaultParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.GET, tag, url, urlParams, postParams,isUseDefaultParams, resultCallback);
    }

    /**
     * 非阻塞get请求
     * 使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     */
    public static void getAsyn(Object tag, String url, Map<String, String> urlParams, Callback callback) {
        commonDelegate.commonAsyn(HttpMethod.GET, tag, url, urlParams, null, callback);
    }

    /**
     * 非阻塞get请求
     * 不使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param resultCallback
     */
    public static void getAsynNoDefParams(Object tag, String url, Map<String, String> urlParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.GET, tag, url, urlParams, null, resultCallback);
    }

    /**
     * 非阻塞post请求
     * 使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param postParams
     * @param resultCallback
     */
    public static void postAsyn(Object tag, String url, Map<String, String> urlParams, JSONObject postParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.POST, tag, url, urlParams, postParams, resultCallback);
    }

    /**
     * 非阻塞post请求
     * 不使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param postParams
     * @param resultCallback
     */
    public static void postAsynNoDefParams(Object tag, String url, Map<String, String> urlParams, JSONObject postParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.POST, tag, url, urlParams, postParams, false, resultCallback);
    }

    /**
     * 非阻塞post请求
     * 使用默认url参数
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param postParams
     * @param resultCallback
     */
    public static void postAsynSecurity(Object tag, String url, Map<String, String> urlParams, JSONObject postParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.POST, tag, url, urlParams, postParams, resultCallback);
    }


    /**
     * 非阻塞put请求
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param postParams
     * @param resultCallback
     */
    public static void putAsyn(Object tag, String url, Map<String, String> urlParams, JSONObject postParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.PUT, tag, url, urlParams, postParams, resultCallback);
    }

    /**
     * 非阻塞delete请求
     *
     * @param tag
     * @param url
     * @param urlParams
     * @param resultCallback
     */
    public static void deleteAsyn(Object tag, String url, Map<String, String> urlParams, ResultCallback resultCallback) {
        commonDelegate.commonAsyn(HttpMethod.DELETE, tag, url, urlParams, null, resultCallback);
    }

    public static void cancelTag(Object tag) {
        Log.i(TAG, "cancel tag is " + tag);
        Dispatcher dispatcher = mIsShortMode ? mOkHttpClient2.dispatcher() : mOkHttpClient.dispatcher();
        List<Call> queuedCalls = dispatcher.queuedCalls();
        List<Call> runningCalls = dispatcher.runningCalls();
        for (Call call : queuedCalls) {
            if (Util.equal(tag, call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : runningCalls) {
            if (Util.equal(tag, call.request().tag())) {
                call.cancel();
            }
        }
    }

    public static Map<String, String> getParamsMap(String... params) {
        Map<String, String> result = null;
        if (params != null) {
            if (params.length % 2 == 0) {
                result = new HashMap<>();
                for (int i = 0; i < params.length; i += 2) {
                    result.put(params[i], params[i + 1]);
                }
            }
        }
        return result;
    }

    public static String getParamsedUrl(String originUrl, Map<String, String> ectraParams) {
        return generatorUrlParams(originUrl, ectraParams);
    }

    public static JSONObject getParamJSONObject(String... params) {
        JSONObject result = null;
        if (params != null) {
            if (params.length % 2 == 0) {
                result = new JSONObject();
                try {
                    for (int i = 0; i < params.length; i += 2) {
                        result.put(params[i], params[i + 1]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static Map<String, String> getCommonUrlParams(Map<String, String> urlParams) {
        if (urlParams == null) {
            urlParams = new HashMap<>();
        }
        urlParams.put("reg_proxy", "aoa_helper");
        return urlParams;
    }

    private void deliveryResult(ResultCallback callback, final Request request) {
        if (callback == null) callback = DEFAULT_RESULT_CALLBACK;
        final ResultCallback resCallBack = callback;
        deliveryResultCore(request, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(request.tag().toString(), "request fail " + e);
                sendFailedStringCallback(request, e, resCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    MissFreshResponse mryxResponse = new MissFreshResponse();
                    mryxResponse.code = response.code();
                    mryxResponse.content = response.body().string();
                    mryxResponse.isSuccessful = response.isSuccessful();
                    mryxResponse.accessToken = response.header("x-access-token");
                    sendSuccessResultCallback(mryxResponse, resCallBack);
                } catch (Exception e) {
                    sendFailedStringCallback(response.request(), e, resCallBack);
                }
            }
        });
    }

    private void deliveryResult(Callback callback, final Request request) {
        deliveryResultCore(request, callback);
    }

    private void deliveryResultCore(final Request request, Callback callback) {
        if (mIsShortMode) {
            mOkHttpClient2.newCall(request).enqueue(callback);
        } else {
            mOkHttpClient.newCall(request).enqueue(callback);
        }
    }

    private void sendFailedStringCallback(final Request request, final Exception e, final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                callback.onNetError(request, e);
            }
        });
    }

    private void sendSuccessResultCallback(final MissFreshResponse missFreshResponse, final ResultCallback callback) {
        if (missFreshResponse.isSuccessful) {

            try {
                // filter err
                ErrorMsg msg = RequestCallBack.getErrorMsg(missFreshResponse.content);
                switch (msg.code) {
                    case ErrorMsg.IErrCode.token_expired: {
                        onTokenExpired(callback);
                        return;
                    }

                }

            } catch (Exception e) {
                if(e.getMessage() == null){
                    Log.e(TAG,"null");
                }else {
                    Log.e(TAG, e.getMessage());
                }
            }

            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(missFreshResponse.content);
                }
            });

        } else if (401 == missFreshResponse.code || 403 == missFreshResponse.code) {
            //the code is HTTP STATUS
            onTokenExpired(callback);
        } else {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCodeError(missFreshResponse.code);
                }
            });
        }
    }

    private void onTokenExpired(final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {

                callback.onCodeLoginError();
            }
        });
    }


    private String buildUrl(String url, Map<String, String> parameters) {
        if (TextUtils.isEmpty(url) || parameters == null || parameters.isEmpty()) {
            return url;
        }
        return generatorUrlParams(url, parameters);
    }

    private static String buildUrlWithDefaultParams(String url, Map<String, String> parameters) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        parameters = getCommonUrlParams(parameters);
        return generatorUrlParams(url, parameters);
    }

    private static String generatorUrlParams(String url, Map<String, String> parameters) {
        StringBuilder result = new StringBuilder().append(url).append("?");
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                String key = param.getKey();
                String value = param.getValue();
                result.append(key).append("=").append(value).append("&");
            }
        }
        return result.substring(0, result.length() - 1);
    }

    private void buildHeader(Request.Builder builder) {
        builder.addHeader(X_VERSION, X_VERSION_CODE);
    }

    private String buildChromeHeadValue(String addressCode, String stationCode) {
        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("address_code", addressCode);
            obj.put("station_code", stationCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj == null ? "" : obj.toString();
    }

    public interface HttpMethod {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
    }

    public static abstract class ResultCallback {
        //返回错误码401或者403
        public abstract void onCodeLoginError();

        //返回码错误

        /**
         * {@link ErrorMsg.IErrCode}
         *
         * @param code
         */
        public abstract void onCodeError(int code);

        //未知异常
        public abstract void onNetError(Request request, Exception e);

        //业务成功
        public abstract void onSuccess(String response);

        public abstract void onNotBindPhoneErr();
    }

    private class CommonDelegate {

        public void commonAsyn(int httpMethod, Object tag, String url, Map<String, String> urlParams, JSONObject postParams, ResultCallback resultCallback) {
            commonAsyn(httpMethod, tag, url, urlParams, postParams, true, resultCallback);
        }

        public void commonAsyn(int httpMethod, Object tag, String url, Map<String, String> urlParams, JSONObject postParams, Callback callback) {
            commonAsyn(httpMethod, tag, url, urlParams, postParams, true, callback);
        }

        public void commonAsyn(int httpMethod, Object tag, String url, Map<String, String> urlParams, JSONObject postParams, boolean isUseDefaultParams, ResultCallback resultCallback) {
            deliveryResult(resultCallback, constructorCommonDelegate(httpMethod, tag, url, urlParams,false, postParams, isUseDefaultParams, resultCallback));
        }

        public void commonAsyn(int httpMethod, Object tag, String url, Map<String, String> urlParams, JSONObject postParams, boolean isUseDefaultParams, Callback callback) {
            deliveryResult(callback, constructorCommonDelegate(httpMethod, tag, url, urlParams, false, postParams, isUseDefaultParams, null));
        }




        private Request constructorCommonDelegate(int httpMethod, Object tag, String url, Map<String, String> urlParams, boolean jsonSecurity, JSONObject postParams, boolean isUseDefaultParams, ResultCallback resultCallback) {
            Request.Builder builder = new Request.Builder();
            tag = tag == null ? TAG : tag;
            builder.tag(tag);
            String finalUrl = isUseDefaultParams ? buildUrlWithDefaultParams(url, urlParams) : buildUrl(url, urlParams);
            builder.url(finalUrl);
            buildHeader(builder);
            RequestBody requestBody;
            switch (httpMethod) {
                case HttpMethod.GET:
                    builder.get();
                    break;
                case HttpMethod.POST:
                    String jsonStr = "";
                    if (postParams == null) {
                        postParams = new JSONObject();
                    }
                    jsonStr = postParams.toJSONString();
                    if(jsonSecurity){
                        jsonStr = plant2Security(jsonStr);
                    }
                    postBody(builder,jsonStr);
                    break;
                case HttpMethod.PUT:
                    if (postParams == null) {
                        postParams = new JSONObject();
                    }
                    requestBody = RequestBody.create(MEDIA_TYPE_JSON, postParams.toString());
                    builder.put(requestBody);
                    break;
                case HttpMethod.DELETE:
                    builder.delete();
                    break;
            }

//            LogUtil.i(tag.toString(), "request is httpMethod=" + httpMethod + "&finalUrl=" + finalUrl + "&urlParams=" + urlParams + "&postParams=" + postParams);
            Request request = builder.build();
            return request;
        }
    }


    private String plant2Security(String origin ){

//        StringBuilder sb = new StringBuilder();
//        sb.append("==").append(origin).append("==");
//        String flaged = sb.toString();
//        ContextUtils.decryptBase64()



        return null;
    }



    private void postBody(Request.Builder builder , String toPostStr){
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, toPostStr);
        builder.post(requestBody);
    }





    public static byte[] genKey() {
        String key = "zhe87651234";
        byte[] bytes = new byte[16];
        try {
            byte[] tmp = key.getBytes("utf-8");
            if (tmp.length > bytes.length) {
                System.arraycopy(tmp, 0, bytes, 0, bytes.length);
            } else {
                System.arraycopy(tmp, 0, bytes, 0, tmp.length);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

















}

