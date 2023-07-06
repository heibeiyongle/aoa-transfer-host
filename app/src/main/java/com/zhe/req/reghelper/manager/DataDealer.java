package com.zhe.req.reghelper.manager;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.zhe.req.reghelper.MainActivity;
import com.zhe.req.reghelper.bean.NetMsg;
import com.zhe.req.reghelper.network.HttpManagerNew;
import com.zhe.req.reghelper.network.RequestCallBack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by zhe on 19-5-19.
 */

public class DataDealer {
    public static final String TAG = "zhe";

    private static final String serverUrl = "https://que.zhe.com/";
    public static final String protocolVer = "3";
    public interface Urls {
        // new with unbind action
        String authCheck = serverUrl + "check-regV4";
        String auth2 = serverUrl + "regV4";
//        String unbindTdKey = serverUrl + "unbindTdKeyV4";
    }


    IDealCb mCb ;

    public DataDealer(IDealCb cb){
        mCb = cb;
    }

    // 完整的http 请求转发
    public void onGetReqMsg(NetMsg.NetReqMsg msg){
        toCheckServer(msg.url,msg.body,msg.reqId);
    }

    // just check
    public void toCheckServer(String devId, String productId, final String regMode,
                              String channelId, String subChannelId, int isMfiFake){

        Log.i(TAG, "toCheckServer...start");
        RequestCallBack requestCallBack = new RequestCallBack() {
            @Override
            public void onNetError(Request request, Exception e) {
                super.onNetError(request, e);
                Log.i(TAG, "toCheckServer...onNetError.");

            }

            @Override
            public void onCodeError(int code) {
                super.onCodeError(code);

            }

            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "toCheckServer...res:" + response);
//                {"code":0,"msg":"","regDispOpt":"","td_key":"QC437B2","reg_mode":""}
                JSONObject jsonObject = JSONObject.parseObject(response);
                String code = jsonObject.getString("code");
                String msg = jsonObject.getString("msg");
                String td_key = jsonObject.getString("td_key");
                String reg_mode = jsonObject.getString("reg_mode");

                NetMsg.RegResMsg res = new NetMsg.RegResMsg();
                res.reged = "0".equals(code);
                res.resMsg = msg;
                res.regCode = td_key;
                res.regMode = reg_mode;
                mCb.onGetRegCheckRes(res);
            }
        };

        JSONObject bodyJson = genParamObj(1,devId, "",productId,"",
                channelId,subChannelId,isMfiFake);

        HttpManagerNew.postAsyn(MainActivity.class.getSimpleName(),
                Urls.authCheck, null, bodyJson, requestCallBack);
        Log.i(TAG, "toCheckServer...end");

    }

    // reg code
    public void toRegServerByCode(String devId, final String regCode, String productId, final String regMode,
                                  String channelId, String subChannelId, int isMfiFake){

        Log.i(TAG, "toRegServerByCode...start");
        RequestCallBack requestCallBack = new RequestCallBack() {
            @Override
            public void onNetError(Request request, Exception e) {
                super.onNetError(request, e);
                Log.i(TAG, "toCheckServer...onNetError.");

            }

            @Override
            public void onCodeError(int code) {
                super.onCodeError(code);

            }

            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "toCheckServer...res:" + response);
//                {"code":0,"msg":"","regDispOpt":"","td_key":"QC437B2","reg_mode":""}
                JSONObject jsonObject = JSONObject.parseObject(response);
                String code = jsonObject.getString("code");
                String msg = jsonObject.getString("msg");
//                String td_key = jsonObject.getString("td_key");
//                String reg_mode = jsonObject.getString("reg_mode");

                NetMsg.RegResMsg res = new NetMsg.RegResMsg();
                res.reged = "0".equals(code);
                res.resMsg = msg;
                if(res.reged){
                    res.regCode = regCode;
                    res.regMode = regMode;
                }
                mCb.onGetRegRes(res);
            }
        };

        JSONObject bodyJson = genParamObj(0,devId, regCode,productId,regMode,
                channelId,subChannelId,isMfiFake);
        HttpManagerNew.postAsyn(MainActivity.class.getSimpleName(),
                Urls.auth2, null, bodyJson, requestCallBack);
        Log.i(TAG, "toRegServerByCode...end");
    }






    public void onNetErr(String url, String reqId){
        NetMsg.NetResMsg resMsg = new NetMsg.NetResMsg();
        resMsg.httpCode = 404;
        resMsg.reqId = reqId;
        mCb.onGetRes(resMsg);
    }

    public void onSuc(String body, String reqId){
        NetMsg.NetResMsg resMsg = new NetMsg.NetResMsg();
        resMsg.httpCode = 200;
        resMsg.reqId = reqId;
        resMsg.body = body;
        mCb.onGetRes(resMsg);
    }

    private void toCheckServer(final String url, String body , final String reqId) {
        Log.i(TAG, "toCheckServer...start");
        RequestCallBack requestCallBack = new RequestCallBack() {
            @Override
            public void onNetError(Request request, Exception e) {
                super.onNetError(request, e);
                Log.i(TAG, "toCheckServer...onNetError.");
                onNetErr(url,reqId);
            }

            @Override
            public void onCodeError(int code) {
                super.onCodeError(code);
                onNetErr(url,reqId);
            }

            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "toCheckServer...res:" + response);
                onSuc(response,reqId);
            }
        };
        Log.i(TAG, " toCheckServer url:" + url+"\nbody:"+body);

        JSONObject bodyJson = null;
        if(!TextUtils.isEmpty(body)){
            bodyJson = JSONObject.parseObject(body);
        }
        HttpManagerNew.postAsyn(MainActivity.class.getSimpleName(),
                url, null, bodyJson, requestCallBack);
        Log.i(TAG, "toCheckServer...end");
    }


    public interface IDealCb{
        void onGetRes(NetMsg.NetResMsg res);
        void onGetRegCheckRes(NetMsg.RegResMsg res);
        void onGetRegRes(NetMsg.RegResMsg res);

    }

    private JSONObject genParamObj(int justCheck,String devId, String regCode,String productId,String regMode,
                                   String channelId,String subChannelId,int isMfiFake){
        //post json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("d_id", devId);
        jsonObject.put("z_just_check", justCheck); //设置不自动激活
        // new add
        jsonObject.put("z_p_ver",protocolVer);
        jsonObject.put("td_key", regCode);
        jsonObject.put("z_reg_mode",regMode);
        jsonObject.put("z_c", channelId);// channel
        jsonObject.put("z_pm", "PRODUCT"); // debug/product
        jsonObject.put("z_pro_did", productId); // 商品的id
        jsonObject.put("z_pro_sid", subChannelId);// 产品渠道商id
        jsonObject.put("os_sdk", android.os.Build.VERSION.SDK_INT+"");
        jsonObject.put("os_mf", android.os.Build.MANUFACTURER);
        jsonObject.put("os_fp", android.os.Build.FINGERPRINT);
        jsonObject.put("os_lan", "");
        jsonObject.put("hu_time", getCurrTimeStr());
        jsonObject.put("hu_mfi_fake", isMfiFake+"");
        jsonObject.put("reg_source", "reg_helper");

        return jsonObject;
    }

    public static String getCurrTimeStr(){
        DateFormat wholeFormat = new SimpleDateFormat("yy-MM-dd-HH_mm_ss", Locale.CHINA);
        String time = wholeFormat.format(new Date());
        return time;
    }






}
