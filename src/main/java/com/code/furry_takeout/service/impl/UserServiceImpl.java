package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.entity.User;
import com.code.furry_takeout.mapper.UserMapper;
import com.code.furry_takeout.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
