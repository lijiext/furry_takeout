package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.dto.DishDto;
import com.code.furry_takeout.entity.Category;
import com.code.furry_takeout.entity.Dish;
import com.code.furry_takeout.entity.DishFlavor;
import com.code.furry_takeout.service.CategoryService;
import com.code.furry_takeout.service.DishFlavorService;
import com.code.furry_takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    // 菜品分页查询
    @GetMapping("page")
    public R<Page> PaginationDish(int page, int pageSize, String name) {
        log.info("Dish 分页查询 页" + page + " 页长" + pageSize + " 搜索词" + name);
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(name), Dish::getName, name)
                .orderByDesc(Dish::getCreateTime);
        dishService.page(dishPage, wrapper);
        Page<DishDto> resPage = new Page<>(page, pageSize);
        // 由于需要两次查询，将原来的 page 拷贝到新的 page 再次处理
        BeanUtils.copyProperties(dishPage, resPage, "records");
        resPage.setRecords(dishPage.getRecords().stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList()));

        return R.success(resPage);
    }

    // 添加菜品
    @PostMapping
    public R<String> AddDish(@RequestBody DishDto dish) {
        log.info("员工 {} 添加菜品 {}", BaseContext.getCurrentEmployeeId(), dish);
//        dishService.save(dish);
        dishService.saveWithFlavor(dish);
        return R.success("添加菜品成功");
    }

    // 根据 ID 查询菜品
    @GetMapping("/{id}")
    public R<DishDto> QueryDishById(@PathVariable("id") Long id) {
        // DTO 是扩展集，用来对原始数据封装处理
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        if (dishDto != null) {
            log.info("员工 {} 查询菜品 {}", BaseContext.getCurrentEmployeeId(), dishDto);
            return R.success(dishDto);
        } else {
            return R.error("没有找到该菜品");
        }
    }

    // 修改菜品信息
    @PutMapping
    public R<String> ModifyDish(@RequestBody DishDto dish) {
        log.info("员工 {} 修改菜品信息 {}", BaseContext.getCurrentEmployeeId(), dish);
        dishService.updateWithFlavor(dish);
        return R.success("修改菜品信息成功");
    }


    // 批量删除菜品
    @DeleteMapping
    public R<String> BatchDeleteDish(@RequestParam List<Long> ids) {
        log.info("员工 {} 批量删除菜品，菜品编号列表为{}", BaseContext.getCurrentEmployeeId(), ids);
        dishService.removeWithFlavor(ids);
        return R.success("批量删除菜品成功");
    }


    // 批量启售 or 停售菜品
    @PostMapping("status/{code}")
    public R<String> BatchEnableDish(@PathVariable("code") Integer status, @RequestParam("ids") List<Long> ids) {
        log.info("员工 {} 批量修改菜品 {} 售卖状态为 {} ", BaseContext.getCurrentEmployeeId(), ids, status);
        // 1. 构造更新器
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();

        // 2. ids 在记录中
        wrapper.in(Dish::getId, ids);
        // 3. 设置 status
        wrapper.set(Dish::getStatus, status);
        // 4. 更新
        dishService.update(wrapper);
        return R.success("修改成功");
    }

    // 查询菜品列表
    @GetMapping("list")
    public R<List<DishDto>> GetDishListWithFlavor(Dish dish) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime)
                .eq(Dish::getStatus, 1)
                .like(dish.getName() != null, Dish::getName, dish.getName());
        List<Dish> list = dishService.list(wrapper);
        List<Dish> dishes = dishService.list(wrapper);
        // 适配移动端，增加查询口味功能
        List<DishDto> dishDtos = dishes.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            LambdaQueryWrapper<DishFlavor> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(DishFlavor::getDishId, item.getId());
            dishDto.setFlavors(dishFlavorService.list(wrapper1));
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }
}
