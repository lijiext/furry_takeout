package com.code.furry_takeout.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

// 1. 设置需要接管的类型
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {
    // 2. 设置全局处理何种信息
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> handleSqlException(Exception e) {
        e.printStackTrace();
        if (e.getMessage().contains("Duplicate entry")) {
            String[] msg = e.getMessage().split(" ");
            return R.error("账号" + msg[1] + "已存在");
        } else
            return R.error("数据库操作异常");
    }

    @ExceptionHandler(NullPointerException.class)
    public R<String> handleNullPointerException(Exception e) {
        log.error("空指针异常");
        e.printStackTrace();
        return R.error("空指针异常");
    }

    @ExceptionHandler(IOException.class)
    public R<String> handleIOException(Exception e) {
        log.error("文件IO异常");
        e.printStackTrace();
        return R.error("文件输入输出异常");
    }

    @ExceptionHandler(NoSuchMethodException.class)
    public R<String> handleNoSuchMethodException(Exception e) {
        log.error("没有本方法");
        e.printStackTrace();
        return R.error("语法错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> handleCustomException(Exception e) {
        e.printStackTrace();
        log.error("自定义异常");
        return R.error(e.getMessage());
    }


}
