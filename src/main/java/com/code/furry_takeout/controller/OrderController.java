package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.dto.OrdersDto;
import com.code.furry_takeout.entity.OrderDetail;
import com.code.furry_takeout.entity.Orders;
import com.code.furry_takeout.entity.ShoppingCart;
import com.code.furry_takeout.service.OrderDetailService;
import com.code.furry_takeout.service.OrderService;
import com.code.furry_takeout.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("submit")
    public R<String> SubmitOrder(@RequestBody Orders orders) {
        log.info("用户 {} 提交订单 {}", BaseContext.getCurrentEmployeeId(), orders);
        orderService.submit(orders);
        return R.success("订单提交成功");
    }

    // 订单查询分页
    @GetMapping("userPage")
    public R<Page<OrdersDto>> PaginationOrders(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "5") Integer pageSize) {
        log.info("用户 {} 分页查询订单详情，第 {} 页， 页长 {}", BaseContext.getCurrentEmployeeId(), page, pageSize);
        return R.success(orderService.dtoPage(page, pageSize));
    }

    // 管理端订单查询
    @GetMapping("page")
    public R<Page<Orders>> PaginationOrders(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "5") Integer pageSize,
                                            Long number,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        log.info("管理端用户 {} 分页查询订单详情，第 {} 页，页长 {}", BaseContext.getCurrentEmployeeId(), page, pageSize);
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(number != null, Orders::getNumber, number)
                .between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime)
                .orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage, wrapper);
        return R.success(ordersPage);
    }

    // 修改订单状态
    @PutMapping
    public R<String> ModifyOrderStatus(@RequestBody Orders orders) {
        log.info("管理端用户 {} 修改订单状态 {}", BaseContext.getCurrentEmployeeId(), orders.getStatus());
        orderService.updateById(orders);
        return R.success("修改订单状态成功");
    }

    //  再来一单
    @PostMapping("again")
    public R<String> PurchaseAgain(@RequestBody Orders orders) {
        log.info("用户 {} 再次订购 {} 成功", BaseContext.getCurrentEmployeeId(), orders);
        // 1. 查询订单表，获取到订单号 number
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getId, orders.getId());
        Orders oldOrder = orderService.getOne(wrapper);
        String oldOrderNumber = oldOrder.getNumber();
        log.info("原有订单号 {}", oldOrderNumber);
        // 2. 查询订单详情表，获取菜品信息
        LambdaQueryWrapper<OrderDetail> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(OrderDetail::getOrderId, oldOrderNumber);
        List<OrderDetail> orderDetailList = orderDetailService.list(wrapper1);
        log.info("原有订单详情 {}", orderDetailList);
        //  3. 获取菜品信息添加到购物车中
        // 查出来的 list 里面的每一项如果有dishid则获取口味、金额、image、菜名称
        // 如果有setmealid 则获取金额、图片、套餐名字
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail item : orderDetailList
        ) {
            // ！！！ 请注意本处 引用数据类型 传递的是地址而不是值
            ShoppingCart newShoppingCart = new ShoppingCart();
            newShoppingCart.setUserId(BaseContext.getCurrentEmployeeId());
            if (item.getDishId() != null) {
                // TODO 本处需要增加菜品被删除或者套餐停售的逻辑处理
                log.info("再来一单，添加菜品 {}", item);
                // 1. item 是菜品，将这个菜品添加到购物车中
                newShoppingCart.setNumber(item.getNumber());
                newShoppingCart.setAmount(item.getAmount());
                newShoppingCart.setDishFlavor(item.getDishFlavor());
                newShoppingCart.setDishId(item.getDishId());
                newShoppingCart.setImage(item.getImage());
                newShoppingCart.setName(item.getName());
            } else if (item.getSetmealId() != null) {
                log.info("再来一单，添加套餐 {}", item);
                // 2. item 是套餐，将这个套餐添加到购物中
                newShoppingCart.setNumber(item.getNumber());
                newShoppingCart.setImage(item.getImage());
                newShoppingCart.setSetmealId(item.getSetmealId());
                newShoppingCart.setAmount(item.getAmount());
                newShoppingCart.setNumber(item.getNumber());
                newShoppingCart.setName(item.getName());
            }
            newShoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(newShoppingCart);
        }
        log.info("再来一单，操作完成后的购物车为 {}", shoppingCartList);
        shoppingCartService.saveBatch(shoppingCartList);
        return R.success("再次订购成功");
    }
}
