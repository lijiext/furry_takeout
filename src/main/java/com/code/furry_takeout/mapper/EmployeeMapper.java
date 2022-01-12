package com.code.furry_takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.code.furry_takeout.entity.Employee;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
