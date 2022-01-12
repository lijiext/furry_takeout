package com.code.furry_takeout.utils;

import com.code.furry_takeout.common.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 发送短信的工具类
 */
@Slf4j
@Component
public class SMSUtils {


    private static RestTemplate restTemplate;

    private static String account;

    private static String password;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        SMSUtils.restTemplate = restTemplate;
    }

    @Value("${furry_takeout.sms.account}")
    public void setAccount(String account) {
        SMSUtils.account = account;
    }

    @Value("${furry_takeout.sms.password}")
    public void setPassword(String password) {
        SMSUtils.password = password;
    }

    private static String Url = "http://106.ihuyi.com/webservice/sms.php?method=Submit";

    public static boolean sendMessage(String mobile, String validateCode) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //提交参数设置
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("account", account);
        map.add("password", password);
        map.add("mobile", mobile);
        map.add("content", "您的验证码是：" + validateCode + "。请不要把验证码泄露给其他人。");
        map.add("format", "json");

        // 组装请求体
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        // 发送post请求，并打印结果，以String类型接收响应结果JSON字符串
        Map<String, Object> result = null;
        try {
            result = restTemplate.postForObject(Url, request, Map.class);
            log.info(String.valueOf(result));
            if (result.getOrDefault("code", "0").toString().equals("2")) {
                return true;
            } else {
                throw new CustomException("验证码发送异常：" + result.getOrDefault("msg", "").toString());
            }
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }

    }

}