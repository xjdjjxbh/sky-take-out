package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveSetmealWithDishes(SetmealDTO setmealDTO);


    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO getByIdWithDish(Long setmealId);


    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 起售或停售套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void updateSetmealWithDishes(SetmealDTO setmealDTO);


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
