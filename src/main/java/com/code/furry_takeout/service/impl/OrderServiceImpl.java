package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.CustomException;
import com.code.furry_takeout.dto.OrdersDto;
import com.code.furry_takeout.entity.*;
import com.code.furry_takeout.mapper.OrdersMapper;
import com.code.furry_takeout.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public void submit(Orders orders) {
        // 1. 记录用户ID
        Long userId = BaseContext.getCurrentEmployeeId();
        // 2. 查询用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(wrapper);
        // 3. 如果购物车为空则不能下单
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            log.info("用户 {} 购物车为空，无法下单", userId);
            throw new CustomException("购物车为空，无法下单");
        }
        // 4. 查询用户数据
        User user = userService.getById(userId);
        // 5. 查询用户地址信息
        long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            log.info("用户 {} 地址簿信息为空，无法下单", userId);
            throw new CustomException("用户地址簿为空，无法下单");
        }
        // 6. 生成订单号
        long orderId = IdWorker.getId();
        // 7. 订单金额初始化
        AtomicInteger amount = new AtomicInteger(0);
        // 8. 设置订单详情信息
        List<OrderDetail> orderDetails = shoppingCartList.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        // 9. 设置订单信息
        orders.setId(orderId);// 主键
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        //  1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setStatus(2);
        // 设置订单总金额
        orders.setAmount(new BigDecimal(amount.get()));
        // 订单号
        orders.setNumber(String.valueOf(orderId));
        // 订单详细地址
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 10. 保存订单信息
        this.save(orders);
        // 11. 设置订单详情
        orderDetailService.saveBatch(orderDetails);
        // 12. 清空购物车
        shoppingCartService.remove(wrapper);
    }

    // 获取订单详情
    @Override
    public Page<OrdersDto> dtoPage(Integer page, Integer pageSize) {
        // 1. 创建分页
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        // 2. 构造查询条件
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, BaseContext.getCurrentEmployeeId())
                .orderByDesc(Orders::getOrderTime);
        // 3. 查询基础条件
        this.page(ordersPage, wrapper);

        // 4. 新建分页，拷贝原有 page 移除 records 塞进去
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(ordersPage, ordersDtoPage, "records");
        // 5. 设置新的 records
        ordersDtoPage.setRecords(ordersPage.getRecords().stream().map(item -> {
            // 6. 新建要塞进去的对象，复制每一项
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            // 7. 新建查询条件
            LambdaQueryWrapper<OrderDetail> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(OrderDetail::getOrderId, item.getNumber()); // 根据订单号查询对应的订单详情
            // 8. 查询
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper1);
            if (orderDetails != null) {
                ordersDto.setOrderDetails(orderDetails);
            }
            return ordersDto;
        }).collect(Collectors.toList())); // 转列表返回
        return ordersDtoPage;
    }
}
