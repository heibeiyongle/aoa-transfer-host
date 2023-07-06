package com.zhe.req.reghelper.bean;

import android.text.TextUtils;
import android.util.Log;

import com.zhe.req.reghelper.manager.DataManager;

/**
 * Created by zhe on 19-5-19.
 */

public class NetMsg {

    public static final int  MSG_ID_REQ_NET = 0X01;
    public static final int  MSG_ID_RES_NET = 0X02;
    public static final int  MSG_ID_PING    = 0X03;
//    public static final int  MSG_ID_PING_RES    = 0X04;
    public static final int  MSG_ID_REQ_DEV_INFO    = 0X05;
    public static final int  MSG_ID_RES_REG_RES    = 0X06;


    public static class BaseMsg{
        public int msgId;
    }


    public static class NetReqMsg extends BaseMsg {

        public String reqId;
        public String url;
        public String body;

        public static NetReqMsg genObj(DataManager.MsgInfo msgInfo){
            NetReqMsg res = new NetReqMsg();
            res.msgId = msgInfo.mMsgId;
            if(msgInfo.hasParam(0)){
                res.reqId = msgInfo.getParam(0).convert2Str();
            }
            if(msgInfo.hasParam(1)){
                res.url = msgInfo.getParam(1).convert2Str();
            }
            if(msgInfo.hasParam(2)){
                res.body = msgInfo.getParam(2).convert2Str();
            }
            Log.i("zhe", "genObj: "+res.toString());
            return res;
        }


        @Override
        public String toString() {
            return "NetReqMsg{" +
                    "msgId=" + msgId +
                    ", reqId='" + reqId + '\'' +
                    ", url='" + url + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    public static class NetResMsg extends BaseMsg {

        public int httpCode;
        public String body;
        public String reqId;

        @Override
        public String toString() {
            return "NetResMsg{" +
                    "msgId=" + msgId +
                    ", httpCode=" + httpCode +
                    ", body='" + body + '\'' +
                    '}';
        }
    }



    public static class RegResMsg extends BaseMsg {

        public int httpCode;
        public String reqId;
        public boolean reged;
        public String resMsg;
        public String regMode;
        public String regCode;

        @Override
        public String toString() {
            return "RegCheckResMsg{" +
                    "msgId=" + msgId +
                    ", httpCode=" + httpCode +
                    ", reqId='" + reqId + '\'' +
                    ", reged=" + reged +
                    ", resMsg='" + resMsg + '\'' +
                    ", regMode='" + regMode + '\'' +
                    ", regCode='" + regCode + '\'' +
                    '}';
        }
    }




    public static class ReqDevInfoMsg extends BaseMsg {

        public String reqId;
        public String uuid;
        public String channel;
//        public String subChannel;
        public String productId;
        public int isMFIFake;
        public String regMode;
        public String regCode;

        public static ReqDevInfoMsg genObj(DataManager.MsgInfo msgInfo){
            ReqDevInfoMsg res = new ReqDevInfoMsg();
            res.msgId = msgInfo.mMsgId;
            if(msgInfo.hasParam(0)){
                res.reqId = msgInfo.getParam(0).convert2Str();
            }
            if(msgInfo.hasParam(1)){
                res.uuid = msgInfo.getParam(1).convert2Str();
            }
            if(msgInfo.hasParam(2)){
                res.channel = msgInfo.getParam(2).convert2Str();
            }
            if(msgInfo.hasParam(3)){
                res.productId = msgInfo.getParam(3).convert2Str();
            }
            if(msgInfo.hasParam(4)){
                res.isMFIFake = msgInfo.getParam(4).convert2Byte();
            }
            if(msgInfo.hasParam(5)){
                res.regMode =  regLinkMode2Str( msgInfo.getParam(5).convert2Byte());
            }
            if(msgInfo.hasParam(6)){
                res.regCode = msgInfo.getParam(6).convert2Str();
            }
            Log.i("zhe", "genObj: "+res.toString());
            return res;
        }

        @Override
        public String toString() {
            return "ReqDevInfoMsg{" +
                    "msgId=" + msgId +
                    ", reqId='" + reqId + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", channel='" + channel + '\'' +
                    ", productId='" + productId + '\'' +
                    ", isMFIFake=" + isMFIFake +
                    ", regMode='" + regMode + '\'' +
                    ", regCode='" + regCode + '\'' +
                    '}';
        }
    }

//    public static class RegResMsg extends BaseMsg {
//
//        public byte suc;
//        // res , linkMode, code
//        public byte linkMode;
//        public String regCode;
//
//
//    }


    public static class CommonMsg extends BaseMsg{
        public static CommonMsg genObj(DataManager.MsgInfo msgInfo){
            CommonMsg res = new CommonMsg();
            res.msgId = msgInfo.mMsgId;
            return res;
        }
    }

    public static int INIT_CARPLAY_WIRED    = 0x1;
    public static int INIT_CARPLAY_WIRELESS = 0x2;
    public static int INIT_ANDROID_CARLIFE  = 0x4;
    public static int INIT_ANDROID_AUTO     = 0x8;
    public static int INIT_ANDROID_MIRROR   = 0x10;

    public static byte regMode2Byte(String regMode){
        byte res = 0;
        if(TextUtils.isEmpty(regMode)){
            return res;
        }
        regMode = regMode.toLowerCase();
        if(regMode.contains("a")){
            res |= INIT_ANDROID_AUTO;
        }
        if(regMode.contains("l")){
            res |= INIT_CARPLAY_WIRED;
        }

        if(regMode.contains("w")){
            res |= INIT_CARPLAY_WIRELESS;
        }
        return res;
    }

    public static String regLinkMode2Str(byte regMode){
        String res = "";
        if(regMode == 0){
            return res;
        }
        if((regMode & INIT_CARPLAY_WIRELESS) == INIT_CARPLAY_WIRELESS){
            res +="w";
        }
        if((regMode & INIT_CARPLAY_WIRED) == INIT_CARPLAY_WIRED){
            res +="l";
        }
        if((regMode & INIT_ANDROID_AUTO) == INIT_ANDROID_AUTO){
            res +="a";
        }
        res = res.toUpperCase();
        return res;
    }



}
