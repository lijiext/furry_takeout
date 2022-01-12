package com.code.furry_takeout.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomException extends RuntimeException {
    //构造方法
    public CustomException(String msg) {
        super(msg);
        log.info("捕获到自定义异常");
    }
}
