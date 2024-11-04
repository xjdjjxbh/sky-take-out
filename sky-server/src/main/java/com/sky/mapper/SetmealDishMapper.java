package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品Id来查询套餐Id
     */


    List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);
}
