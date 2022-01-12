package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.dto.SetmealDto;
import com.code.furry_takeout.entity.Category;
import com.code.furry_takeout.entity.Setmeal;
import com.code.furry_takeout.entity.SetmealDish;
import com.code.furry_takeout.service.CategoryService;
import com.code.furry_takeout.service.SetmealDishService;
import com.code.furry_takeout.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("setmeal")
public class SetMealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    // 套餐分页查询
    @GetMapping("page")
    public R<Page> PaginationSetMeal(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     String name) {
        log.info("员工 {} 分页查询套餐，第 {} 页，页长 {}, 检索词 {}", BaseContext.getCurrentEmployeeId(), page, pageSize, name);
        // 1. 设置分页对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        // 2. 设置查询条件
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(name), Setmeal::getName, name)
                .orderByDesc(Setmeal::getCreateTime);
        // 3. 第一次查询
        setmealService.page(setmealPage, wrapper);
        // 4. 设置分类名分页对象
        Page<SetmealDto> resPage = new Page<>(page, pageSize);
        // 5. 复制属性
        BeanUtils.copyProperties(setmealPage, resPage, "records");
        // 6. 属性填充到新 Page 中
        resPage.setRecords(setmealPage.getRecords().stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList()));
        return R.success(resPage);
    }


    // 新增套餐
    @PostMapping
    public R<String> AddMealSet(@RequestBody SetmealDto setmealDto) {
        setmealService.addDishAndFlavor(setmealDto);
        log.info("用户 {} 新增套餐 {}", BaseContext.getCurrentEmployeeId(), setmealDto);
        return R.success("添加套餐成功");
    }

    // 批量删除套餐
    @DeleteMapping
    public R<String> BatchDeleteMealSet(@RequestParam("ids") List<Long> ids) {
        log.info("用户 {} 删除套餐 {}", BaseContext.getCurrentEmployeeId(), ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    // 批量修改套餐状态
    @PostMapping("status/{code}")
    public R<String> BatchModifyMealSet(@PathVariable("code") Integer code, @RequestParam("ids") List<Long> ids) {
        log.info("用户 {} 修改套餐 {} 状态为 {}", BaseContext.getCurrentEmployeeId(), ids, code);
        // 1. 设置更新条件
        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Setmeal::getStatus, code);
        wrapper.in(Setmeal::getId, ids);
        // 2. 更新
        setmealService.update(wrapper);
        return R.success("更新套餐状态成功");
    }

    // 查询套餐数据
    @GetMapping("list")
    public R<List<Setmeal>> ListSetMeal(Setmeal setmeal) {
        log.info("用户 {} 查询套餐列表 {}", BaseContext.getCurrentEmployeeId(), setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        return R.success(setmealService.list(queryWrapper));
    }

    // 获取套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> GetMealSetById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getSetmealWithDish(id);
        return R.success(setmealDto);
    }

    // 保存套餐
    @PutMapping()
    public R<String> SaveMealSet(@RequestBody SetmealDto setmealDto) {
        log.info("管理端用户 {} 修改套餐 {}", BaseContext.getCurrentEmployeeId(), setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("保存套餐成功");
    }
}
