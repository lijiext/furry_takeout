package com.code.furry_takeout.dto;


import com.code.furry_takeout.entity.Setmeal;
import com.code.furry_takeout.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
