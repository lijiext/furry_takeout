package com.code.furry_takeout.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.code.furry_takeout.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {


    public boolean remove(Long id);
}
