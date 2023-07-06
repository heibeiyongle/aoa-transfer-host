package com.zhe.req.reghelper.network;

/**
 * Created by zhengxiaoguo on 16/1/22.
 */
public class ErrorMsg {

    public interface IErrCode {
        int res_code_unpack_err = -1;
        int suc = 0;
        int token_expired = 1002;
        int param_err = 100; //"{code:101,msg:\"param err\"}"
        int key_used_err = 101;//"{code:101,msg:\"td_key already_used!\"}"
        int key_not_found_err = 102;//"{code:102,msg:\"td_key not_fond err\"}"
        int inner_err = 103;// "{code:103,msg:\"reg_err,just wait...\"}"
        int bind_twice_err = 104;// "{code:104,msg:\"same key and dev_id bind_twice! If you insistently , wait 12h.\"}"

    }


    public int code = IErrCode.suc;
    public String msg = "";
}
