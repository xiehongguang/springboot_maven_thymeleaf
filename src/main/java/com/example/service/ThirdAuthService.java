package com.example.service;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;

public interface ThirdAuthService {
    public abstract String getAccessToken(String code);
    public abstract String getOpenId(String accessToken);
    public abstract String refreshToken(String code);
    public abstract String getAuthorizationUrl() throws UnsupportedEncodingException;
    public abstract JSONObject getUserInfo(String accessToken, String openId);
}
