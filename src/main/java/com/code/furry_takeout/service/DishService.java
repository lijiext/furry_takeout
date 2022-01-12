package com.code.furry_takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.furry_takeout.dto.DishDto;
import com.code.furry_takeout.entity.Dish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DishService extends IService<Dish> {

    @Transactional
    void saveWithFlavor(DishDto dishDto);

    @Transactional
    DishDto getByIdWithFlavor(Long id);

    @Transactional
    void updateWithFlavor(DishDto dishDto);

    // 根据 菜品Id List 批量删除已经停售的菜品和口味信息
    @Transactional
    void removeWithFlavor(List<Long> ids);


}
