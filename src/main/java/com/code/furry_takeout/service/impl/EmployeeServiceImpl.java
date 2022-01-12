package com.code.furry_takeout.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.entity.Employee;
import com.code.furry_takeout.mapper.EmployeeMapper;
import com.code.furry_takeout.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public Object list(LambdaQueryWrapper<Employee> wrapper) {
        return null;
    }
}
