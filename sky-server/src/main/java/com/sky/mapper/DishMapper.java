package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /**
     * 插入菜品数据
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);


    /**
     * 菜品的分页查询
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);


    @Select("select * from dish where id = #{id}")
    Dish selectById(Long id);


    /**
     * 根据菜品id删除菜品
     *
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);


    /**
     * 根据菜品id集合，批量删除菜品数据
     *
     * @param dishIds
     */
    void deleteByIds(List<Long> dishIds);

    /**
     * 根据菜品id修改菜品的基本信息
     *
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    //修改的时候要更新修改时间和修改人，所以要加上自动填充注解
    void update(Dish dish);
}
