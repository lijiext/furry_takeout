package com.code.furry_takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.furry_takeout.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    Object list(LambdaQueryWrapper<Employee> wrapper);
}
