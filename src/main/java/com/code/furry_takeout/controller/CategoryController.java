package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.entity.Category;
import com.code.furry_takeout.service.CategoryService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("category")
@RestController
public class CategoryController {
    // 1. Autowire Service
    @Autowired
    CategoryService categoryService;

    // 2. 设置接口路径
    @GetMapping("page")
    public R<Page> PaginationCategory(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Category 分页查询 页" + page + " 页长" + pageSize);
        Page<Category> categoryPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        categoryService.page(categoryPage, wrapper);
        return R.success(categoryPage);
    }

    @PostMapping
    public R<String> AddCategory(@RequestBody Category category) {
        categoryService.save(category);
        log.info("员工 " + BaseContext.getCurrentEmployeeId() + " 添加了分类 " + category);
        return R.success("添加分类成功");
    }

    @PutMapping
    public R<String> ModifyCategory(@RequestBody Category category) {
        categoryService.updateById(category);
        log.info("员工 " + BaseContext.getCurrentEmployeeId() + " 修改了分类 " + category);
        return R.success("修改分类信息成功");
    }

    @DeleteMapping
    public R<String> DeleteCategory(@RequestParam(required = true) Long id) {
        log.info("员工 " + BaseContext.getCurrentEmployeeId() + "  删除分类 " + id);
        if (categoryService.remove(id)) {
            log.info("删除分类");
        }
        return R.success("删除分类成功");
    }

    @GetMapping("list")
    public R<List<Category>> ListCategory(Category category) {
        log.info("菜品分类查询 type={}", category.getType());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getName() != null, Category::getType, category.getType());
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(wrapper);
        return R.success(list);

    }
}
