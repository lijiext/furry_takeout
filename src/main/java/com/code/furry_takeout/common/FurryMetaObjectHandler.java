package com.code.furry_takeout.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 2. 配置 Component 注解
@Slf4j
@Component
// 1. implements MetaObjectHandler
public class FurryMetaObjectHandler implements MetaObjectHandler {

    // 重写 insert 和 update 方法
    @Override
    public void insertFill(MetaObject metaObject) {

        log.info("公共字段填充 Insert ");
//        log.info("Insert 线程ID：" + Thread.currentThread().getId());
        // 3. 设置公共字段值
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        /* 4. 需要记录操作人 使用 ThreadLocal 解决
        解决方式：
        1. 在 DoFilter 时设置 ThreadLocal 局部变量值
        2. 在同一个线程中需要的地方获取变量值
         */
        metaObject.setValue("createUser", BaseContext.getCurrentEmployeeId());
        metaObject.setValue("updateUser", BaseContext.getCurrentEmployeeId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
//        log.info("公共字段填充 Update ");
        log.info("Update 线程ID：" + Thread.currentThread().getId());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentEmployeeId());
    }
}
