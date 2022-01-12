package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.entity.ShoppingCart;
import com.code.furry_takeout.mapper.ShoppingCartMapper;
import com.code.furry_takeout.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
