package com.code.furry_takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.furry_takeout.dto.SetmealDto;
import com.code.furry_takeout.entity.Employee;
import com.code.furry_takeout.entity.Setmeal;
import com.code.furry_takeout.entity.SetmealDish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    Object list(LambdaQueryWrapper<Employee> wrapper);

    // 根据 ID 获得套餐和菜品信息
    SetmealDto getSetmealWithDish(Long id);

    // 新增菜品和口味
    void addDishAndFlavor(SetmealDto setmealDto);


    @Transactional
    void removeWithDish(List<Long> ids);

    @Transactional
    void updateWithDish(SetmealDto setmealDto);
}
