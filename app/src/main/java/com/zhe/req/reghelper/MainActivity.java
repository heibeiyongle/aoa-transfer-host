package com.zhe.req.reghelper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhe.reg.R;
import com.zhe.req.reghelper.application.utils.ContextUtils;
import com.zhe.req.reghelper.bean.NetMsg;
import com.zhe.req.reghelper.manager.DataDealer;
import com.zhe.req.reghelper.manager.DataManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zhe.req.reghelper.bean.NetMsg.MSG_ID_PING;
import static com.zhe.req.reghelper.bean.NetMsg.MSG_ID_RES_NET;
import static com.zhe.req.reghelper.bean.NetMsg.MSG_ID_RES_REG_RES;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "zhe";
    public static final String ACTION_USB_PERMISSION =
            "com.zhe.AOA.MainActivity.action.USB_PERMISSION";

    private static final String ACC_MANUF = "zhe";	// Expected manufacturer name
    private static final String ACC_MODEL = "regHelper";	// Expected model name

    TextView tvNetState;
    TextView tvHUstate;
    TextView tvTransState;
    View vInputCodeBox;
    EditText etInputCode;
    View btnToReg;


    private UsbManager mUSBManager;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileOutputStream mOutputStream;
    private FileInputStream mInputStream;
    Receiver mReceiver;

    private boolean mfPermissionRequested, mfConnectionOpen;
    DataDealer mDataD;
    Handler mh;
    Toast mt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mh = new Handler();
        setContentView(R.layout.activity_main);
        mUSBManager = (UsbManager) getSystemService (Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        registerReceiver(receiver, filter);
        tvNetState = findViewById(R.id.tv_net_status);
        tvHUstate = findViewById(R.id.tv_hu_state);
        tvTransState = findViewById(R.id.tv_info);

        vInputCodeBox = findViewById(R.id.vbox_input_code);
        etInputCode = findViewById(R.id.et_reg_code);
        btnToReg = findViewById(R.id.v_to_reg);


        mDataD = new DataDealer(dataDealerCb);
        mt = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        mt.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

        mh.postDelayed(checkNetR,1000);

        btnToReg.setOnClickListener(mClk);
    }

    Runnable checkNetR = new Runnable() {
        @Override
        public void run() {
            boolean connected = ContextUtils.isConnectedInternet(MainActivity.this);
            if(connected){
                tvNetState.setText("网络正常");
                tvNetState.setBackgroundColor(Color.GREEN);
            }else {
                tvNetState.setText("请链接网络");
                tvNetState.setBackgroundColor(Color.RED);
                showT("手机未联网网络");
                Log.i(TAG, " net conn? :"+connected);
            }

            UsbAccessory[] accList = mUSBManager.getAccessoryList();
            if(accList != null && accList.length > 0){
                String msg = "已连接设备";
                tvHUstate.setBackgroundColor(Color.GREEN);
                onFindHU(msg);

            }else {
                onFindHU("未发现车机");
                tvHUstate.setBackgroundColor(Color.RED);
            }

            mh.postDelayed(this,3000);
        }
    };


    private void showT(String str){
        mt.setText(str);
        mt.show();
    }


    private View.OnClickListener mClk = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.v_to_reg:{
                    if(mCurrentDevInfo == null)
                    {
                        showToastFromThread(" dev-info null-Err !!");
                        return;
                    }

                    String regCode = etInputCode.getText().toString();
                    if(TextUtils.isEmpty(regCode)){
                        showToastFromThread(" regCode null-Err !!");
                        return;
                    }

                    mToServerOpt.toRegServerByCode(mCurrentDevInfo.uuid,regCode,
                            mCurrentDevInfo.productId,"L",mCurrentDevInfo.channel,
                            "",mCurrentDevInfo.isMFIFake);
                    break;
                }
            }
        }
    };


    /*
       1. 添加监听 插入
       2. 打开扫描
       3. 监听请求权限结果
       4. 打开设备
       5. 开始读取数据
       6. 处理数据
     */

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent: ");
        ops = open();
        if(ops == OpenStatus.CONNECTED){
            ptlog("检测到车机\n");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        ops = open();
    }
    OpenStatus ops;
    // Listens for permission and accessory detached messages (registered in onCreate)
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action: "+action);
            // Check the reason the receiver was called
            if (ACTION_USB_PERMISSION.equals(action))
            {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                {
                    Log.i(TAG, "Permission Granted");
                    ptlog("通信已允许\n");
                    OpenStatus status = open(accessory);
                    if (status == OpenStatus.CONNECTED){
                        onFindHU("车机已连接");
                    }else{
                        showToastFromThread("Error: " + status);
                    }
                }
                else
                {
                    Log.i(TAG, "Permission NOT Granted");
                }
            }
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
                ptlog("车机已断开\n");
                onHUDetach();
                close();
            }else if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){
                Log.i(TAG, "onReceive: ACTION_USB_ACCESSORY_ATTACHED");
                ops = open();
                if(ops == OpenStatus.CONNECTED){
                    onFindHU("车机已连接 attach");
                    ptlog("车机已连接(attach)\n");
                }
            }
        }
    };

    private void onFindHU(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHUstate.setText(str);
            }
        });
    }

    private void onHUDetach(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHUstate.setText("---------");
                tvHUstate.setBackgroundColor(Color.RED);
            }
        });
        hideInputArea();
    }

    int i = 0;
    private void ptlog(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(i > 20){
//                    tvTransState.setText("");
//                }
                tvTransState.append(++i+","+msg);
            }
        });
    }

    private void clLog(){
        i = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTransState.setText("");
            }
        });
    }

    private void onTransferConnected(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTransState.setBackgroundColor(Color.GREEN);
            }
        });
    }


    private void onTransferBreak(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTransState.setBackgroundColor(Color.RED);
            }
        });
    }

    public enum OpenStatus
    {
        CONNECTED, REQUESTING_PERMISSION, UNKNOWN_ACCESSORY, NO_ACCESSORY, NO_PARCEL
    }



    // Requests permissions to access the accessory
    public OpenStatus open()
    {
        Log.i(TAG, "open: 111");
        if (mfConnectionOpen)
            return OpenStatus.CONNECTED;

        UsbAccessory[] accList = mUSBManager.getAccessoryList();	// The accessory list only returns 1 entry
        if (accList != null && accList.length > 0)
        {
            // If permission has been granted, try to establish the connection
            if (mUSBManager.hasPermission(accList[0])){
                Log.i(TAG, "open: 2");
                return open(accList[0]);
            }

            // If not, request permission
            if (!mfPermissionRequested)
            {
                Log.i(TAG, "Requesting USB permission");
                PendingIntent permissionIntent = PendingIntent.
                        getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                mUSBManager.requestPermission(accList[0], permissionIntent);
                mfPermissionRequested = true;
                return OpenStatus.REQUESTING_PERMISSION;
            }
        }

        return OpenStatus.NO_ACCESSORY;
    }

    // Try to establish a connection to the accessory
    public OpenStatus open(UsbAccessory accessory)
    {
        Log.i(TAG, "open: 3");
        if (mfConnectionOpen)
            return OpenStatus.CONNECTED;

        // Check if the accessory is supported by this app
        if (!ACC_MANUF.equals(accessory.getManufacturer()) || !ACC_MODEL.equals(accessory.getModel()))
        {
            Log.i(TAG, "Unknown accessory: " + accessory.getManufacturer() + ", " + accessory.getModel());
            return OpenStatus.UNKNOWN_ACCESSORY;
        }

        // Open read/write streams for the accessory
        mParcelFileDescriptor = mUSBManager.openAccessory(accessory);

        if (mParcelFileDescriptor != null)
        {
            Log.i(TAG, "open: 4");
            FileDescriptor fd = mParcelFileDescriptor.getFileDescriptor();
            mOutputStream = new FileOutputStream(fd);
            mInputStream = new FileInputStream(fd);
            mfConnectionOpen = true;
            mReceiver = new Receiver(mDataD);
            new Thread(mReceiver).start();			// Run the receiver as a separate thread
            return OpenStatus.CONNECTED;
        }

        Log.i(TAG, "Couldn't get any ParcelDescriptor");
        return OpenStatus.NO_PARCEL;
    }

    public void showToastFromThread(final String sToast)
    {
        Log.i(TAG, sToast);
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                showT(sToast);
            }
        });
    }


    // Stop and clean up the connection to the accessory
    public void close()
    {
        ops = OpenStatus.NO_ACCESSORY;
        if (!mfConnectionOpen)
            return;

        mfPermissionRequested = false;
        mfConnectionOpen = false;

        // End the receiver thread
        mReceiver.close();
        Log.i(TAG, "Receiver Thread closed");

        // Close the data streams
        try
        {
            mInputStream.close();
            Log.i(TAG, "Input Stream closed");
        }
        catch (IOException e)
        {
            Log.w(TAG, "Exception when closing Input Stream", e);
        }

        try
        {
            mOutputStream.close();
            Log.i(TAG, "Output Stream closed");
        }
        catch (IOException e)
        {
            Log.w(TAG, "Exception when closing Output Stream", e);
        }

        try
        {
            mParcelFileDescriptor.close();
            Log.i(TAG, "File Descriptor closed");
        }
        catch (IOException e)
        {
            Log.w(TAG, "Exception when closing File Descriptor", e);
        }
    }


    // A new thread that receives messages from the accessory
    DataDealer mToServerOpt;
    NetMsg.ReqDevInfoMsg mCurrentDevInfo;

    private class Receiver implements Runnable
    {
        private AtomicBoolean fRunning = new AtomicBoolean(true);
        // Constructor
        Receiver(DataDealer cb ) {
            mToServerOpt = cb;
        }

        public void run()
        {
            Log.i(TAG, "Receiver thread started");
            clLog();
            ptlog("通信准备\n");
            try {
                byte[] dataBlock = new byte[16384];//max
                NetMsg.BaseMsg baseMsg = null;
                while (fRunning.get()) {
                    int headSize = mInputStream.read(dataBlock);
                    int printLen = headSize;
                    if(printLen > 100){
                        printLen = 100;
                    }
                    ptlog("--> data-len "+printLen+"\n");
                    ptlog( " data-head:"+DataManager.convertHexStr(Arrays.copyOfRange(dataBlock,0,printLen)));

                    Log.i(TAG," get aoa data: len:"+printLen+
                            ", data-head:"+DataManager.convertHexStr(Arrays.copyOfRange(dataBlock,0,printLen)));
//
                    if (headSize > 0) {
                        baseMsg = convertHead9ToMsg(dataBlock);
                    }else {
                        Log.i(TAG, " aoa size:"+headSize);
                    }

                    if (!fRunning.get()) {
                        Log.i(TAG, " exit ,by mExit true.");
                        break;
                    } else if (headSize == -1) {
                        Log.i(TAG, " exit ,by headSize -1.");
                        break;
                    } else if (headSize == -2) {
                        Log.i(TAG, " exit ,by headSize -2.");
                        break;
                    }
                    if (baseMsg == null) {
                        continue;
                    }
                    Log.i(TAG, "baseMsg:" + baseMsg.toString());
                    switch (baseMsg.msgId) {
                        case NetMsg.MSG_ID_REQ_NET: {
                            ptlog("请求网络\n");
                            mToServerOpt.onGetReqMsg((NetMsg.NetReqMsg) baseMsg);
                            break;
                        }
                        case NetMsg.MSG_ID_PING:{
                            ptlog("通信开始\n");
                            senPingRes();
                            onTransferConnected();
                            break;
                        }
                        case NetMsg.MSG_ID_REQ_DEV_INFO:{
                            ptlog("得到设备信息\n");
                            onGetDevInfo((NetMsg.ReqDevInfoMsg) baseMsg);
                            mCurrentDevInfo = (NetMsg.ReqDevInfoMsg) baseMsg;
                            break;
                        }
                    }
                }
            }catch (Exception e) {
                Log.w(TAG, "Exception reading input stream", e);
            }
            ptlog("读数据结束\n");
            onTransferBreak();
            close();
            Log.i(TAG, "Receiver thread ended");
        }



        //
        private void onGetDevInfo(NetMsg.ReqDevInfoMsg msg){
            // to check server
            mToServerOpt.toCheckServer(msg.uuid,msg.productId,"",msg.channel,"",msg.isMFIFake );
        }


        private NetMsg.BaseMsg convertHead9ToMsg(byte[] body){
            NetMsg.BaseMsg res = null;
            short packLen = DataManager.Packet.getPackLen(body);
            Log.i(TAG, "convertHead9ToMsg: packLen:"+packLen);
//            ptlog("包长度:"+packLen+"\n");
//            if(body.length != packLen){
//                return null;
//            }
            byte[] packBody = new byte[packLen - 9];
            System.arraycopy(body,9,packBody,0,packBody.length);
            if(!DataManager.Packet.checkMsgBody(packBody)){
                Log.i(TAG," convertHead9ToMsg, msgBody data Err.");
                return null;
            }
            DataManager.Packet tmpPackInfo = new DataManager.Packet(packBody);
            DataManager.MsgInfo msgInfo = tmpPackInfo.getMsg();

            switch (msgInfo.mMsgId){
                case NetMsg.MSG_ID_REQ_NET:{
                    res = NetMsg.NetReqMsg.genObj(msgInfo);
                    break;
                }
                case NetMsg.MSG_ID_PING:{
                    res = NetMsg.CommonMsg.genObj(msgInfo);
                    break;
                }
                case NetMsg.MSG_ID_REQ_DEV_INFO:{
                    res = NetMsg.ReqDevInfoMsg.genObj(msgInfo);
                    break;
                }
            }
            return res;
        }

        public void close()
        {
            fRunning.set(false);
            ops = OpenStatus.NO_ACCESSORY;
        }

    }



    DataDealer.IDealCb dataDealerCb = new DataDealer.IDealCb() {
        @Override
        public void onGetRes(NetMsg.NetResMsg res) {
            sendRes(res.httpCode,res.body,res.reqId);
        }

        @Override
        public void onGetRegCheckRes(NetMsg.RegResMsg res) {
            if(res.reged){
                // reged ,send msg to dev
                sendRegRes(true,res.regMode,res.regCode);
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showT("设备未激活，请输入激活码，完成激活");
                        vInputCodeBox.setVisibility(View.VISIBLE);
                    }
                });
                // not-reged, notice user input code
            }
        }

        @Override
        public void onGetRegRes(NetMsg.RegResMsg res) {
            sendRegRes(res.reged,res.regMode,res.regCode);
            if(res.reged){
                hideInputArea();
            }
        }
    };

    public void sendRes(int code, String body, String reqId ){
        ptlog("\t\t<-- http:"+code+"\n");
        Log.i(TAG, "sendRes: code:"+code+", body:"+body);
        ArrayList<DataManager.ParaItem> paraItems = new ArrayList<>();
        DataManager.ParaItem reqIdP = new DataManager.ParaItem(0,reqId);
        DataManager.ParaItem codeP = new DataManager.ParaItem(1,code);
        paraItems.add(reqIdP);
        paraItems.add(codeP);

        if(!TextUtils.isEmpty(body)){
            DataManager.ParaItem bodyP = new DataManager.ParaItem(2,body);
            paraItems.add(bodyP);
        }
        sendMsg(MSG_ID_RES_NET,paraItems);
    }

    private void senPingRes(){
        sendMsg(MSG_ID_PING,null);
    }

    private void sendRegRes(boolean reged, String regMode, String regCode){
//        // byte-res , linkMode, code
//        public byte linkMode;
//        public String regCode;
        ptlog("\t\t<-- 激活成功:"+reged+"\n");
        ArrayList<DataManager.ParaItem> paraItems = new ArrayList<>();
        DataManager.ParaItem regSucP = new DataManager.ParaItem(0,(byte)(reged?1:0));
        paraItems.add(regSucP);
        if(reged){
            DataManager.ParaItem linkModeP = new DataManager.ParaItem(1,NetMsg.regMode2Byte(regMode));
            paraItems.add(linkModeP);
            DataManager.ParaItem regCodeP = new DataManager.ParaItem(2,regCode);
            paraItems.add(regCodeP);
            ptlog("\t\t<-- linkMode:"+NetMsg.regMode2Byte(regMode)+"("+regMode+")"+", code:"+regCode+"\n");

        }
        sendMsg(MSG_ID_RES_REG_RES,paraItems);
    }

    private void sendMsg(int msgId, List<DataManager.ParaItem> paraItems){
        DataManager.MsgInfo tmpMsg = new DataManager.MsgInfo(
                (short) msgId,paraItems);
        DataManager.Packet packet = new DataManager.Packet(tmpMsg);
        byte[] toWrite = packet.getPacketBytes();
        Log.i(TAG," sendMsg, -> data:"+DataManager.convertHexStr(toWrite));
        try {
            mOutputStream.write(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void hideInputArea(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vInputCodeBox.setVisibility(View.GONE);
            }
        });
    }


}
