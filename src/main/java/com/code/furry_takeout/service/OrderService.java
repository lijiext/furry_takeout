package com.code.furry_takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.furry_takeout.dto.OrdersDto;
import com.code.furry_takeout.entity.Orders;
import com.code.furry_takeout.mapper.OrdersMapper;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService extends IService<Orders> {
    // 订单提交接口
    @Transactional
    public void submit(Orders orders);

    // 订单查询接口
    public Page<OrdersDto> dtoPage(Integer page, Integer pageSize);
}
