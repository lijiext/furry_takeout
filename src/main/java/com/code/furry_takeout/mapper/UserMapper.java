package com.code.furry_takeout.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.code.furry_takeout.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}