package com.zhe.req.reghelper.network;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * Created by g_chen on 15/9/6.
 * 网络请求的响应
 */
public class MissFreshResponse implements Serializable {
    public String content;
    public int code;
    public boolean isSuccessful;
    public String accessToken;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
