package com.code.furry_takeout.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.common.CustomException;
import com.code.furry_takeout.entity.Category;
import com.code.furry_takeout.entity.Dish;
import com.code.furry_takeout.entity.Setmeal;
import com.code.furry_takeout.mapper.CategoryMapper;
import com.code.furry_takeout.service.CategoryService;
import com.code.furry_takeout.service.DishService;
import com.code.furry_takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;




    @Override
    public boolean remove(Long id) {
        // 1. 查询分类下的商品
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId, id);
        long dishCount = dishService.count(wrapper);
        if (dishCount > 0) {
            throw new CustomException("当前分类下还有菜品");
        }
        // 2. 查询分类下的套餐
        LambdaQueryWrapper<Setmeal> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Setmeal::getCategoryId, id);
        long mealSetCount = setmealService.count(wrapper1);
        if (mealSetCount > 0) {
            throw new CustomException("当前分类下还有套餐");
        }
        return super.removeById(id);
    }
}
