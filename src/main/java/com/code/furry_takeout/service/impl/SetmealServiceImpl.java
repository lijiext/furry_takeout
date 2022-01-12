package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.common.CustomException;
import com.code.furry_takeout.dto.SetmealDto;
import com.code.furry_takeout.entity.Employee;
import com.code.furry_takeout.entity.Setmeal;
import com.code.furry_takeout.entity.SetmealDish;
import com.code.furry_takeout.mapper.SetmealMapper;
import com.code.furry_takeout.service.SetmealDishService;
import com.code.furry_takeout.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public Object list(LambdaQueryWrapper<Employee> wrapper) {
        return null;
    }

    @Override
    public SetmealDto getSetmealWithDish(Long id) {
        // 1. 获取套餐信息
        Setmeal setmeal = this.getById(id);
        // 2. 封装表现层
        SetmealDto setmealDto = new SetmealDto();
        // 3. 复制原始结果到表现层结果再次封装
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 4. 创建查询条件, 查询套餐对应的菜品
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        // 5. 查询
        List<SetmealDish> list = setmealDishService.list(wrapper);
        // 6. 填充
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    public void addDishAndFlavor(SetmealDto setmealDto) {
        // 1. 保存基本套餐信息
        this.save(setmealDto);
        // 2. 关联套餐和菜品
        Long MealSetId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item -> {
            // 获取传递过来的每一项，设置其 MealSetID 为当前套餐 ID
            item.setSetmealId(MealSetId);
            return item;
        }).collect(Collectors.toList());
        // 3. 批量保存
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        // 批量删除套餐
        // 1. 判断套餐是否停售
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, 1);
        wrapper.in(Setmeal::getId, ids);
        if (this.count(wrapper) > 0) {
            log.info("列表中还有未停售的套餐");
            throw new CustomException("列表中还有未停售的套餐");
        }
        // 2. 如果已停售，则删除关联的菜品
        LambdaQueryWrapper<SetmealDish> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(SetmealDish::getSetmealId, ids);
//        log.info("删除套餐中的菜品 {}", setmealDishService.list(wrapper1));
        setmealDishService.remove(wrapper1);

        // 3. 删除套餐
        this.removeByIds(ids);
        log.info("删除套餐 {} 成功", ids);
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 1. 修改套餐信息
        this.updateById(setmealDto);
        // 2. 先删除套餐下的菜品信息
        Long setMealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmealDto);
        setmealDishService.remove(wrapper);
        // 3. 插入新的信息
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        list = list.stream().map(item -> {
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());
        // 4. 保存
        setmealDishService.saveBatch(list);
    }

}
