package com.zhe.req.reghelper.application.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhengxiaoguo on 15/12/29.
 */
public class ContextUtils {
    private static final String TAG = "ContextUtils";

    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
    }

    public static String getSdCardPath() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cachePath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        return cachePath;
    }


    public static void showToast(Context context, Object msg) {
        if (msg != null && context != null) {
            Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private static final int toastStepMs = 5 * 1000;

    private static final Map<String, Long> toastLastTimeMP = new HashMap<>();

    public static void showToastByKeyLimit(Context context, String key, String msg) {
        long currTimeMs = System.currentTimeMillis();
        if (toastLastTimeMP.containsKey(key)) {
            Long timeMs = toastLastTimeMP.get(key);
            if (currTimeMs - timeMs.longValue() < toastStepMs) {
                return;
            }
        }

        toastLastTimeMP.put(key, Long.valueOf(currTimeMs));
        showToast(context, msg);

    }


    public static void showToastSuc(Context context) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View view = inflater.inflate(R.layout.layout_img_suc_toast, null);
//        Toast toast = new Toast(context);
//        toast.setView(view);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
    }


    public static boolean isConnectedInternet(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * @return 获取随机数字
     */
    public static int getRandomNum() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    public static String getUUID(Context deviceInfo) {
        String uuid = null;
        try {
            final TelephonyManager tm = (TelephonyManager) deviceInfo.getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;

            tmDevice = "" + tm.getDeviceId();

            tmSerial = "" + tm.getSimSerialNumber();

            androidId = "" + android.provider.Settings.Secure.getString(deviceInfo.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(),
                    ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

            String uniqueId = deviceUuid.toString();

            uuid = MD5(uniqueId);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return uuid;
    }

    public static String MD5(String inStr) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }


    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return

    public static String encryptBase64(String content, byte[] enCodeFormat) {
        try {
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            String base64Str = Base64.encodeToString(bytes, Base64.DEFAULT);
            return base64Str; // 加密

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
     */

    /**
     * 解密
     *
     * @param content 待解密内容
     * @return
     */
    public static String decryptBase64(byte[] content, byte[] enCodeFormat) {
        try {
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            String rs = new String(result, "utf-8");
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }




//                Log.i("MainActivity","base64:"+base64Str);
//
//    byte[] decodeBytes = Base64.decode(base64Str, Base64.DEFAULT);
//



    public static String openImageCapture(Activity activity, int requestCode) {
        String imagePath = null;
        try {
            File storagePath = new File(getTakePhotoPath());
            if (!storagePath.exists()) {
                storagePath = new File(getSdCardPath());
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String fileName = MD5(dateFormat.format(new Date())) + ".jpg";

            File imageFile = new File(storagePath, fileName);
            imagePath = imageFile.getAbsolutePath();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    public static void openPhotoLibrary(Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int[] getLocalImageSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            int[] size = new int[2];
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeFile(path, options);
            size[0] = options.outWidth;
            size[1] = options.outHeight;

            Log.d(TAG, "本地图片宽高 ：" + size[0] + "*" + size[1]);
            return size;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTakePhotoPath() {
        if (isSdCardAvailable()) {
            return "/sdcard/";
        }
        return null;
    }

    public static int getNetType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        int mNetWorkType = -1;

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();

            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = NetType.net_t_wifi;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();

                if (proxyHost == null || proxyHost.equals("")) {
                    mNetWorkType = NetType.net_t_2g;
                } else {
                    if (isFastMobileNetwork(telephonyManager)) {
                        mNetWorkType = NetType.net_t_3g;
                    } else {
                        mNetWorkType = NetType.net_t_2g;
                    }
                }
            }
        } else {
            mNetWorkType = NetType.net_none;
        }

        Log.i(TAG, "[getNetType]...end.rs:" + mNetWorkType);
        return mNetWorkType;
    }

    private static boolean isFastMobileNetwork(TelephonyManager telephonyManager) {

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    public static void toast(Context context, String msg, int shortType) {
        Toast toast = Toast.makeText(context, msg, shortType);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void toast(Context context, String msg) {
        toast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void toastBottom(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,40);
        toast.show();
    }


    public static String getPhoneIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        } else {
            return "";
        }
    }

    public interface NetType {
        int net_none = 1001;
        int net_t_2g = 2001;
        int net_t_3g = 3001;
        int net_t_wifi = 4001;
    }

    public static boolean isWeixinAvilible(Context context) {

        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAppOnForeground(Context ctx) {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = ctx.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    public static String saveImageToLocal(Context ctx, Bitmap bitmap) {

        File imageFile = null;
        String path = "";
        try {
            if (isSdCardAvailable()) {
                File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
                String fileName = "mryx_" + timeStamp + "";
                imageFile = new File(pic, fileName + ".jpg");
            } else {
                File cacheDir = ctx.getCacheDir();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
                String fileName = "mryx_" + timeStamp + "";
                imageFile = new File(cacheDir, fileName + ".jpg");
            }

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            insertImageToLocal(ctx, imageFile);
            path = imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return path;
    }


    private static void insertImageToLocal(Context ctx, File tmpFile) {
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "");
        values.put(MediaStore.Images.Media.DATE_TAKEN, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, tmpFile.getPath());
        values.put(MediaStore.Images.Media.SIZE, tmpFile.length());
        ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    public static void setTextFlag(TextView tvPrice, boolean deleteLine) {
        if (deleteLine) {
            tvPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        } else {
            tvPrice.getPaint().setFlags(0);
        }
    }


    public static byte[] getAssetFile(Context context, String path) {
        byte[] bytes = null;
        try {
            InputStream is = context.getResources().getAssets().open(path);
            int size = is.available();
            bytes = new byte[size];
            is.read(bytes);
            is.close();
        } catch (Exception e) {
        }
        return bytes;
    }


    /**
     * 将 str 中的xx元 xx折 标识color颜色
     *
     * @param textView
     * @param str
     * @param color
     */
    public static void setTextviewSpecialStrColor(TextView textView, String str, int color) {
        if (textView == null || TextUtils.isEmpty(str)) {
            return;
        }
        // 000元 9999折
        String match = "\\d+\\.\\d+[折元]|\\d+[折元]";
        Pattern pattern = Pattern.compile(match);
        Matcher matcher = pattern.matcher(str);

        SpannableString spannableString = new SpannableString(str);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannableString);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void systemExit(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> list = activityManager.getAppTasks();
        if (list.size() == 0) {
            return;
        }
        for (ActivityManager.AppTask appTask : list) {
            appTask.finishAndRemoveTask();
        }
    }

    public static String converImgUrl2Webp(String url) {
        return converImgUrl2WebpAndFitWidth(url, 0);
    }


    public static String converImgUrl2WebpAndFitWidth(String url, int width) {
//        LogUtil.i(TAG,"converImgUrl2Webp...url:"+url+"，width:"+width);
        if (TextUtils.isEmpty(url) || (!url.startsWith("http:") && !url.startsWith("https"))) {
            return url;
        }
//        LogUtil.i(TAG,"converImgUrl2Webp...url:"+url);
        // step 1
        String appendStr = "iopcmd=convert&dst=webp";

        if (url.contains(appendStr)) {
            return url; // 已经包含转换参数
        }
        if (width > 0) {
            appendStr = appendStr + "|iopcmd=thumbnail&type=4&width=" + width;
        }

        //step 2, 截取未带参数部分的url 判断url类型尾缀
        String tmpLowerCaseUrl = url.toLowerCase();
        if (tmpLowerCaseUrl.contains("?")) {
            tmpLowerCaseUrl = tmpLowerCaseUrl.substring(0, tmpLowerCaseUrl.indexOf("?"));
        }
        if (!tmpLowerCaseUrl.endsWith(".jpg")
                && !tmpLowerCaseUrl.endsWith(".jpeg")
                && !tmpLowerCaseUrl.endsWith(".png")) {
            return url;//url后缀不是 jpg jpeg png 结尾
        }


        String headModel1 = "image.missfresh.cn";
        String headModel2 = "ufile.ucloud.cn";
        String headModel3 = "ufile.ucloud.com.cn";

        String match = "^(http://|https://)[^\\/]+\\/";
        Pattern pattern = Pattern.compile(match);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String urlHeader = matcher.group().toLowerCase();
            urlHeader = urlHeader.substring(0, urlHeader.length() - 1);
//            LogUtil.i(TAG,"urlHeader:"+urlHeader);

            if (!urlHeader.contains(headModel1)
                    && !urlHeader.contains(headModel2)
                    && !urlHeader.contains(headModel3)) {
                return url; //不是指定的host
            }

            if (!url.contains("?")) { //无参数
                url = url.concat("?").concat(appendStr);
            } else {
                if (!url.endsWith("&")) {
                    url = url.concat("&");
                }
                url = url.concat(appendStr);
            }

        }

//        LogUtil.i(TAG,"converImgUrl2Webp....end url:"+url);

        return url;
    }


    public static void openScheme(Activity activity, String schemeUrl) {
        if (!TextUtils.isEmpty(schemeUrl)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeUrl));
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    public static String getMacAddr(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getMacAddress();
    }


    public static boolean ifLangZh(){
        boolean rs = true;
        String currL = Locale.getDefault().getLanguage();
        if (TextUtils.isEmpty(currL)
                || currL.equalsIgnoreCase(Locale.CHINA.getLanguage())
                || currL.equalsIgnoreCase(Locale.CHINESE.getLanguage())
                || currL.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getLanguage())
                || currL.equalsIgnoreCase(Locale.TRADITIONAL_CHINESE.getLanguage())) {
            rs = true;
        } else {
            rs = false;
        }
        return rs;
    }

    public static String getLangType(){
        String rs = "zh";
        String currL = Locale.getDefault().getLanguage();
        if(TextUtils.isEmpty(currL)){
            return rs;
        }
        currL = currL.toUpperCase();

        if (currL.equalsIgnoreCase(Locale.CHINA.getLanguage())
                || currL.equalsIgnoreCase(Locale.CHINESE.getLanguage())
                || currL.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getLanguage())
                ) {
            rs = "zh";
        } else if(currL.equalsIgnoreCase(Locale.TRADITIONAL_CHINESE.getLanguage())){
            rs = "zh-tra";
        }else {
            rs = "en";
        }
        return rs;
    }

    /**
     * 改变音频数据音量
     * @param oriData
     * @param destV
     * @param bits
     * @return
     */
    public static byte[] reSetV(byte[] oriData, float destV, int bits){
        if(bits == 8){
            int i = 0;
            short tmp;
            while (i < oriData.length){
                tmp = (short) (oriData[i] * destV);
                if( tmp > 127){
                    oriData[i] = 127;
                }else if(tmp < -128){
                    oriData[i] = -128;
                }else {
                    oriData[i] = (byte) tmp;
                }
                i++;
            }
        }else if( bits == 16 && oriData.length % 2 ==0){
            int i = 0;
            int tmp = 0;
            while (i < oriData.length){
                tmp = (int) ((short)(((oriData[i+1] & 0xFF) << 8) | (0xFF & oriData[i]))*destV);
                i+=2;
                if(tmp > 32767){
                    tmp = 32767;
                }else if(tmp < -32768){
                    tmp = -32768;
                }
                oriData[i-1] = (byte) (tmp >> 8 & 0xFF);
                oriData[i-2] = (byte) (tmp & 0xFF);
            }
        }
        return oriData;
    }


    public static String intent2Str(Intent intent){

        String tmpLog = " action: "+intent.getAction()+", ";
        if(intent != null && intent.getExtras() != null && intent.getExtras().keySet() != null){
            Set<String> set = intent.getExtras().keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()){
                String key = iterator.next();
                Object value = intent.getExtras().get(key);
                tmpLog += (" "+key+" ='"+value+"', ");
            }
        }
        return tmpLog;
    }

}






























