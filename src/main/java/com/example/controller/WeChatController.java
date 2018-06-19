/**
 * https://blog.csdn.net/u010978008/article/details/73896306s
 */
package com.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.service.WeChatAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Controller
@RequestMapping("WeChatAuth")
public class WeChatController {
    @Autowired
    WeChatAuthService weChatAuthService;

    /**
     * 登陆
     *
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wxLoginPage", method = RequestMethod.GET)
    public void wxLoginPage(HttpServletResponse response) throws Exception {
        String uri = weChatAuthService.getAuthorizationUrl();
        try {
            response.sendRedirect(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return loginPage(uri);
    }

    @RequestMapping(value = "/callback")//wechat
    public void callback(String code, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accessToken = weChatAuthService.getAccessToken(code);
        JSONObject jsonObject = JSONObject.parseObject(accessToken);
        if (jsonObject.containsKey("access_token")) {

            String access_token = jsonObject.getString("access_token");
            String openId = jsonObject.getString("openId");
//        String refresh_token = jsonObject.getString("refresh_token");

            // 保存 access_token 到 cookie，两小时过期
            Cookie accessTokencookie = new Cookie("accessToken", access_token);
            accessTokencookie.setMaxAge(60 * 2);
            response.addCookie(accessTokencookie);

            Cookie openIdCookie = new Cookie("openId", openId);
            openIdCookie.setMaxAge(60 * 2);
            response.addCookie(openIdCookie);

            //根据openId判断用户是否已经登陆过
       /* KmsUser user = userService.getUserByCondition(openId);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/student/html/index.min.html#/bind?type="+ Constants.LOGIN_TYPE_WECHAT);
        } else {
            //如果用户已存在，则直接登录
            response.sendRedirect(request.getContextPath() + "/student/html/index.min.html#/app/home?open_id=" + openId);
        }*/
            // 获取用户信息
            JSONObject userJson=weChatAuthService.getUserInfo(accessToken,openId);
            System.out.println(userJson.toJSONString());
        } else {
            /* 获取access_token错误 */
        }
    }
}
