package com.zhe.req.reghelper.network;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;

public class RequestCallBack extends HttpManagerNew.ResultCallback {
    /**
     * 如果不需要登陆 那么重写一个空的方法即可
     */
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

    /**
     * 解析错误信息
     *
     * @param response
     * @return
     */
    public static ErrorMsg getErrorMsg(String response) {
        ErrorMsg errorMsg = new ErrorMsg();
        try {
            Log.i("RequestCallBack","res:"+response);
            if("success".equals(response) ){
                errorMsg.code = 0;
                errorMsg.msg = "success";
            }else if(response!=null && response.startsWith("[")){
                errorMsg.code = ErrorMsg.IErrCode.res_code_unpack_err;
            } else {
                JSONObject jsonObject = JSONObject.parseObject(response);
                if (jsonObject != null) {
                    errorMsg.code = jsonObject.getIntValue("code");
                    errorMsg.msg = jsonObject.getString("msg");
                    if (errorMsg.code != 0) {
                        errorMsg.msg = TextUtils.isEmpty(errorMsg.msg) ? "操作失败,请稍后再试" : errorMsg.msg;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("RequestCallBack", e.getMessage());
            errorMsg.code = ErrorMsg.IErrCode.res_code_unpack_err;
        }
        return errorMsg;
    }
}
