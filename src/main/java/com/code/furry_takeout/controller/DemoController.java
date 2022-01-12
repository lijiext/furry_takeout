package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.code.furry_takeout.entity.Employee;
import com.code.furry_takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/getInfos")
    public Object getInfos() {
        log.info("开始请求!");
        LambdaQueryWrapper<Employee> wrapper = Wrappers.<Employee>lambdaQuery().orderByDesc(Employee::getCreateTime);
        return employeeService.list(wrapper);
    }
}
