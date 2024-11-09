package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.entity.SetmealDish;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品Id来查询套餐Id
     */


    List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);


    /**
     * 给套餐添加它所对应的所有菜品
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);


    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getSetmealDishesBySetmealId(Long setmealId);

    void deleteBatchs(List<Long> setmealIds);
}
