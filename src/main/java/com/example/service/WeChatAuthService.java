package com.example.service;

import com.alibaba.fastjson.JSONObject;

public interface WeChatAuthService extends ThirdAuthService {
    public JSONObject getUserInfo(String accessToken, String openId);
}
