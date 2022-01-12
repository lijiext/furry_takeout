package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.common.CustomException;
import com.code.furry_takeout.dto.DishDto;
import com.code.furry_takeout.entity.Dish;
import com.code.furry_takeout.entity.DishFlavor;
import com.code.furry_takeout.mapper.DishMapper;
import com.code.furry_takeout.service.DishFlavorService;
import com.code.furry_takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().peek(item -> item.setDishId(dishDto.getId())).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 1. 根据 ID 获取菜品
        Dish dish = this.getById(id);

        // 2. 复制到新的容器中以备进一步查询填充
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 3. 构造查询条件
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dish.getId());

        // 4. 封装查询结果
        List<DishFlavor> list = dishFlavorService.list(wrapper);

        // 5. 设置对象属性
        dishDto.setFlavors(list);

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 1. 修改菜品信息
        this.updateById(dishDto);

        // 2. 查询菜品口味表，并更新
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dishDto.getId());

        //  3. 删除口味信息？？？
        dishFlavorService.remove(wrapper);

        List<DishFlavor> list = dishDto.getFlavors();
        list = list.stream().peek(item -> item.setDishId(dishDto.getId())).collect(Collectors.toList());
        // 4. 保存菜品信息
        dishFlavorService.saveBatch(list);
    }

    // 根据菜品 ID 列表批量删除已经停售的菜品
    @Override
    public void removeWithFlavor(List<Long> ids) {

        log.error("准备移除菜品，列表为 {}", ids);
        // 1. 首先需要判断菜品是否已经停售
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        // 查询所有未停售的菜品
        wrapper.eq(Dish::getStatus, 1);
        // 查询 ids 是否在未停售菜品中
        wrapper.in(Dish::getId, ids);

        log.error("查询，结果为 {}", this.list(wrapper));
        if (this.count(wrapper) > 0) {
            throw new CustomException("批处理列表中含有未停售的菜品");
        }
        // 2. 移除菜品
        this.removeByIds(ids);
        // 3. 移除菜品口味
        LambdaQueryWrapper<DishFlavor> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(wrapper1);
    }




}
