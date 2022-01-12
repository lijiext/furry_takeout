package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.entity.ShoppingCart;
import com.code.furry_takeout.service.ShoppingCartService;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("shoppingCart")
@RestController
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    // 购物车列表展示
    @GetMapping("list")
    public R<List<ShoppingCart>> ListShoppingCart(ShoppingCart shoppingCart) {
        shoppingCart.setUserId(BaseContext.getCurrentEmployeeId());
        log.info("用户 {} 查询购物车列表 {}", BaseContext.getCurrentEmployeeId(), shoppingCart);
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId,
                shoppingCart.getUserId());
        wrapper.orderByDesc(ShoppingCart::getCreateTime);
        return R.success(shoppingCartService.list(wrapper));
    }

    // 购物车添加菜品
    @PostMapping("add")
    public R<ShoppingCart> AddShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        log.info("用户 {} 购物车列表 {}", BaseContext.getCurrentEmployeeId(), shoppingCart);
        // 1. 设置当前用户
        shoppingCart.setUserId(BaseContext.getCurrentEmployeeId());
        // 2. 设置查询条件
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());
        if (shoppingCart.getDishId() != null) {
            // 3. 如果传递过来的是菜品，则查询菜品
            wrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 4. 如果传递过来的是套餐，则查询套餐
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartDb = shoppingCartService.getOne(wrapper);
        if (shoppingCartDb == null) {
            // 5. 如果购物车为空，设置数量 1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            // 6. 保存购物车信息
            shoppingCartService.save(shoppingCart);
            shoppingCartDb = shoppingCart;
        } else {
            // 7. 如果已经有了这个 item 则数量加 1
            shoppingCartDb.setNumber(shoppingCartDb.getNumber() + 1);
            // 8. 保存购物车
            shoppingCartService.updateById(shoppingCartDb);
        }
        return R.success(shoppingCartDb);
    }

    //  购物车减少菜品数量
    @PostMapping("sub")
    public R<ShoppingCart> MinusShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        log.info("用户 {} 给购物车减少菜品 {}", BaseContext.getCurrentEmployeeId(), shoppingCart);
        // 1. 设置当前用户
        shoppingCart.setUserId(BaseContext.getCurrentEmployeeId());
        // 2. 设置查询条件
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());
        if (shoppingCart.getDishId() != null) {
            // 3. 如果传递过来的是菜品，则查询菜品
            wrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 4. 如果传递过来的是套餐，则查询套餐
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartDb = shoppingCartService.getOne(wrapper);
        if (shoppingCartDb == null) {
            // 5. 如果购物车中的数量为 0， 则返回异常
            log.info("购物车为空，无法减少");
            return R.error("购物车为空， 不能减少");
        } else {
            // 6. 如果购物车中数量为1， 继续减少则清空购物车
            if (shoppingCartDb.getNumber() == 1) {
                shoppingCartService.remove(wrapper);
                return R.success(shoppingCart);
            }
            // 7. 如果已经有了这个 item 则数量减 1
            shoppingCartDb.setNumber(shoppingCartDb.getNumber() - 1);
            // 7. 保存购物车
            shoppingCartService.updateById(shoppingCartDb);
            log.info("购物车减少 {}", shoppingCartDb);
        }
        return R.success(shoppingCartDb);
    }

    //  清空购物车
    @DeleteMapping("clean")
    public R<String> CleanShoppingCart() {
        log.info("用户 {} 清除购物车", BaseContext.getCurrentEmployeeId());
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentEmployeeId());
        shoppingCartService.remove(wrapper);
        return R.success("购物车清空成功");
    }
}
