package com.zhe.req.reghelper.manager;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhe on 18-8-17.
 */

public class DataManager {
    public static final String TAG = "zhe";

    private DataManager(){}
    private static DataManager instance = new DataManager();
    public static DataManager getInstance(){
        return instance;
    }


    public static class Packet{
        MsgInfo mMsg;
        private Packet(){}

        public Packet(byte[] msgDataWithCheckSum){
            byte[] withOutCheckSum = new byte[msgDataWithCheckSum.length -1];
            System.arraycopy(msgDataWithCheckSum,0,withOutCheckSum,0,withOutCheckSum.length);
            mMsg = new MsgInfo(withOutCheckSum);
        }

        public Packet(MsgInfo msgInfo){
            mMsg = msgInfo;
        }

        public MsgInfo getMsg(){
            return mMsg;
        }

        public byte[] getPacketBytes(){
            return pack();
        }

        private byte[] pack(){
            byte[] tmpHead = new byte[9];
            tmpHead[0] = (byte) 0xff;
            tmpHead[1] = (byte) 0x1a;

            // len
            byte[] msgData = mMsg.getBytes();
            byte[] res = new byte[msgData.length + 10];

            byte[] tmpLen = genBytes((short)(res.length));
            tmpHead[2] = tmpLen[0];
            tmpHead[3] = tmpLen[1];
            tmpHead[8] = checkSum(tmpHead,0,8);

            System.arraycopy(tmpHead,0,res,0,tmpHead.length);
            System.arraycopy(msgData,0,res,tmpHead.length,msgData.length);
            res[res.length - 1] = checkSum(res,tmpHead.length,msgData.length);
            return res;
        }

        public static short getPackLen(byte[] packHead){
            return genShort(packHead[2],packHead[3]);
        }

        public static boolean checkIfHead(byte[] head){
            boolean res = false;
            if(head != null && head.length == 9 &&
                    head[0] == (byte)0xFF && head[1] == 0x1a ){
                byte checkSum = checkSum(head,0,8);
                if(checkSum == head[8]){
                    res = true;
                }
            }
            if(!res){
                Log.i(TAG,"ch rs:"+res+", data:"+convertHexStr(head));
            }
            return res;
        }

        public static boolean checkMsgBody(byte[] packBody){
            boolean rs = false;
            if(packBody.length >6 &&
                    packBody[0]==(byte)0x10 &&
                    packBody[1]==(byte)0x10 ){
                short msgLen = genShort(packBody[2],packBody[3]);
                if(msgLen + 1 == packBody.length){ // check length
                    byte checkSum = checkSum(packBody,0,msgLen);
                    if(checkSum == packBody[packBody.length - 1]){
                        rs = true;
                    }
                }

            }
            Log.i(TAG,"cb rs:"+rs+", data:"+convertHexStr(packBody));
            return rs;
        }

    }




    public static class MsgInfo{

        public short mMsgId;
        public List<ParaItem> mParaItems = null;
        private HashMap<Short,ParaItem> paramIdRef = new HashMap<>();

        private MsgInfo(){
        }

        public MsgInfo(byte[] data){
            Log.i(TAG," MsgInfo, data:"+convertHexStr(data));
            if(data == null || data.length < 6){
                return;
            }
            mMsgId = genShort(data[4],data[5]);
            if(data.length > 6){
                byte[] tmp = new byte[data.length - 6];
                System.arraycopy(data,6,tmp,0,tmp.length);
                mParaItems = genParamItems(tmp);
                initPidRef();
            }
        }

        public MsgInfo(short msgId, List<ParaItem> paraItems){
            mMsgId = msgId;
            mParaItems = paraItems;
            if(mParaItems != null && mParaItems.size() > 0){
                initPidRef();
            }
        }

        public ParaItem getParam(int pid){

            return paramIdRef.get((short)pid);
        }

        public boolean hasParam(int pid){
            return paramIdRef.containsKey((short) pid);
        }

        private void initPidRef(){
            for (ParaItem tmpP: mParaItems) {
                paramIdRef.put(tmpP.id,tmpP);
            }
        }


        public byte[] getBytes(){
            byte[] msgHead = new byte[6];
            msgHead[0] = (byte) 0x10;
            msgHead[1] = 0x10;
            byte[] tmpIdArr = DataManager.genBytes(mMsgId);
            msgHead[4] = tmpIdArr[0];
            msgHead[5] = tmpIdArr[1];
//            short msgParamsLen = 0;
//            byte[] paramsBytes = null;
            byte[] res = null;
            if(mParaItems == null || mParaItems.size() == 0){
                res = msgHead;
            }else {
                byte[] paramsBytes = genBytesByParams(mParaItems);
                res = new byte[paramsBytes.length + 6];
                System.arraycopy(msgHead,0,res,0,msgHead.length);
                System.arraycopy(paramsBytes,0,res,msgHead.length,paramsBytes.length);
            }
            // len
            byte[] msgLenArr = genBytes((short) (res.length));
            res[2] = msgLenArr[0];
            res[3] = msgLenArr[1];

            return res;
        }


        @Override
        public String toString() {
            return "MsgInfo{" +
                    "mMsgId=" + convertHexStr(genBytes(mMsgId)) +
                    ", mParaItems=" + mParaItems +
                    ", paramIdRef size=" + paramIdRef.size() +
                    '}';
        }
    }

    public static byte[] genBytesByParams(List<ParaItem> list){
        if(list == null || list.size() == 0){
            return null;
        }
        int destLen = 0;
        List<byte[]> tmpBytes = new ArrayList<>();

        // obj to bytes
        for(int i=0; i< list.size() ;i++ ){
            byte[] tmpB = list.get(i).toBytes();
            tmpBytes.add(tmpB);
            destLen += tmpB.length;
        }

        // combine bytes
        byte[] res = new byte[destLen];
        int offset = 0;
        for(int i=0; i<tmpBytes.size(); i++){
            int tmpLen = tmpBytes.get(i).length;
            System.arraycopy(tmpBytes.get(i),0,res,offset,tmpLen);
            offset += tmpLen;
        }

        return res;
    }


    public static List<ParaItem> genParamItems(byte[] src){
        List<ParaItem> ret = new ArrayList<>();
        int offset = 0;
        while (offset < src.length){
            short paramDataLen = genShort(src[offset],src[offset+1]);
            byte[] tmpData = new byte[paramDataLen];
            System.arraycopy(src,offset,tmpData,0,tmpData.length);
            ParaItem itemData = new ParaItem(tmpData);
            ret.add(itemData);
            offset += paramDataLen;
        }
        return ret;
    }


    public static class ParaItem{
        public short id;
        public byte[] data;

        private ParaItem(){}

        // FIXME: 18-9-7
        public ParaItem(byte[] rawData){
            id = genShort( rawData[2], rawData[3]);
            data = new byte[rawData.length - 4];
            System.arraycopy(rawData,4,data,0,data.length);
        }

        public ParaItem(int pId,byte oneByte){
            id = (short) pId;
            data = new byte[1];
            data[0] = oneByte;
        }
        public ParaItem(int pId,byte[] bytes){
            id = (short) pId;
            data = bytes;
        }
        public ParaItem(int pId,short oneShort){
            id = (short) pId;
            data = genBytes(oneShort);
        }
        public ParaItem(int pId,short[] sArr){
            id = (short) pId;
            data = genBytes(sArr);
        }
        public ParaItem(int pId,int src){
            id = (short) pId;
            data = genBytes(src);
        }
        public ParaItem(int pId,long src){
            id = (short) pId;
            data = genBytes(src);
        }
        public ParaItem(int pId,String src){
            id = (short) pId;
            data = genBytes(src);
        }

        public byte[] toBytes(){
            byte[] res = new byte[data.length + 4];
            byte[] lenArr = genBytes((short)( res.length));
            res[0] = lenArr[0];
            res[1] = lenArr[1];
            byte[] idArr = genBytes(id);
            res[2] = idArr[0];
            res[3] = idArr[1];
            System.arraycopy(data,0,res,4,data.length);
            return res;
        }

        public byte[] convert2Blob(){
            return data;
        }

        public byte convert2Byte(){
            byte ret = -1;
            if(data.length == 1){
                return data[0];
            }else if(data == null){
                Log.i(TAG," convert2short, data is null !");
            }else if(data.length != 1){
                Log.i(TAG," convert2short, data-len:"+data.length+", not 1 !");
            }
            return ret;
        }

        public short convert2short(){
            short ret = -1;
            if(data != null && data.length == 2){
                ret = genShort(data[0],data[1]);
            }else if(data == null){
                Log.i(TAG," convert2short, data is null !");
            }else if(data.length != 2){
                Log.i(TAG," convert2short, data-len:"+data.length+", not 2 !");
            }
            return ret;
        }

        public short[] convert2shortArr(){
            short[] ret = null;
            if(data != null && data.length%2 == 0){
                ret = genShortArr(data);
            }else if(data == null){
                Log.i(TAG," convert2short, data is null !");
            }else if(data.length%2 != 0){
                Log.i(TAG," convert2short, data-len:"+data.length+", not times 2 !");
            }
            return ret;
        }

        public int convert2int(){
            int ret = -1;
            if(data != null && data.length == 4){
                ret = genInt(data);
            }else if(data == null){
                Log.i(TAG," convert2int, data is null !");
            }else if(data.length != 4){
                Log.i(TAG," convert2int, data-len:"+data.length+", not 4 !");
            }
            return ret;
        }

        public long convert2long(){
            long ret = -1;
            if(data != null && data.length == 8){
                ret = genLong(data);
            }else if(data == null){
                Log.i(TAG," convert2int, data is null !");
            }else if(data.length != 8){
                Log.i(TAG," convert2int, data-len:"+data.length+", not 8 !");
            }
            return ret;
        }

        public String convert2Str(){
            try {
                if(data != null && data.length >0){
                    String ret = new String(data,0,data.length,"utf-8");
//                    Log.i(TAG," convert2Str, ret:"+ret+", src-data:"+ContextUtils.convertHexStr(data));
                    return ret;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG,e.getMessage());
            }
            return "";
        }

        public String debug2Hex(){
            return convertHexStr(data);
        }

        @Override
        public String toString() {
            return "ParaItem{" +
                    "id=" + id +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }




    private static short genShort(byte b0, byte b1){
        short ret = (short)(((b0 & 0xFF) << 8) | (0xFF & b1));
        return ret;
    }

    private static byte[] genBytes(short s){
        byte[] res = new byte[2];
        res[0] = (byte) (s >> 8 & 0xFF);
        res[1] = (byte) (s & 0xFF);
        return res;
    }

    private static short[] genShortArr(byte[] src){
        if(src == null || src.length%2 != 0){
            return null;
        }
        short[] ret = new short[src.length/2];
        for(int i = 0; i< src.length; i+=2){
            ret[i/2] = genShort(src[i],src[i+1]);
        }
        return ret;
    }

    private static byte[] genBytes(short[] sArr){
        byte[] res = new byte[sArr.length];
        for(int i=0; i<sArr.length; i++ ){
            byte[] tmpShort = genBytes(sArr[i]);
            res[i*2] = tmpShort[0];
            res[i*2 + 1] = tmpShort[1];
        }
        return res;
    }

    private static int genInt(byte[] src){
        int ret = 0;
//        Log.i(TAG," genInt, src:"+ContextUtils.convertHexStr(src));
        for (int ix = 0; ix < 4; ++ix) {
            ret <<= 8;
            ret |= (src[ix] & 0xff);
        }
//        Log.i(TAG," genInt, ret:"+ret);
        return ret;
    }

    private static byte[] genBytes(int src){
        byte[] res = new byte[4];
        for(int i=3; i>=0; i--){
            res[i] = (byte) (src & 0xFF);
            src >>= 8;
        }
        return res;
    }

    private static long genLong(byte[] src){
        long ret = 0;
        for (int ix = 0; ix < 8; ++ix) {
            ret <<= 8;
            ret |= (src[ix] & 0xff);
        }
//        Log.i(TAG," genLong, src:"+ContextUtils.convertHexStr(src) + ", ret:"+ret);
        return ret;
    }


    private static byte[] genBytes(long src){
        byte[] res = new byte[8];
        for(int i=7; i>=0; i--){
            res[i] = (byte) (src & 0xFF);
            src >>= 8;
        }
        return res;
    }

    private static byte[] genBytes(String src){
        if(TextUtils.isEmpty(src)){
            return null;
        }
        byte[] res = null;
        try {
            res = src.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,e.getMessage());
        }
        return res;
    }

    public static String convertHexStr(byte[] data){
        return convertHexStr(data,true);
    }

    public static String convertHexStr(byte[] data,boolean ifBlankGap){
        if(data == null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < data.length; i ++){
            String hex = Integer.toHexString( data[i] & 0x00ff);
            hex = hex.length() == 1 ? "0"+hex:hex;
            sb.append(hex);
            if(ifBlankGap){
                sb.append(" ");
            }
        }

        return sb.toString().toUpperCase();
    }


    public static byte[] convertToBytes(String hexStr){
        if (hexStr == null || hexStr.length()%2 != 0){
            return null;
        }
        byte[] rs = new byte[hexStr.length()/2];
        for(int i=0; i<hexStr.length(); i++){
            String tmp = hexStr.substring(i,i+2);
            rs[i/2] = (byte) (Integer.valueOf(tmp,16) & 0x00ff);
            i++;
        }
        return rs;
    }


    public static byte checkSum(byte[] src, int start, int len){
        int i;
        byte ret = 0;

        for(i=start; i<(start+len); i++ ){
            ret += src[i];
        }
        return (byte)(0x100-ret);
    }

}
