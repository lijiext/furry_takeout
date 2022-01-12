package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.entity.OrderDetail;
import com.code.furry_takeout.mapper.OrderDetailMapper;
import com.code.furry_takeout.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
