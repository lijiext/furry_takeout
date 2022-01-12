package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.code.furry_takeout.entity.DishFlavor;
import com.code.furry_takeout.mapper.DishFlavorMapper;
import com.code.furry_takeout.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
