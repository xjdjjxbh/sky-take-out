package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional    //事务注解，因为涉及到了多张表的操作，要保证这些操作组成的操作是原子性的(在启动类上面已经标注了开启注解方式的事务管理，这里添加这个注解之后，事务就会生效)
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();    //因为DishDTO里面还包含了口味数据，这在插入菜品数据的时候是不需要的，所以我们创建一个菜品对象就够了
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表添加一条数据
        dishMapper.insert(dish);

        //获取到刚才生成的主键值
        Long dishId = dish.getId();

        //向口味表添加n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            /*
            因为每次要新增菜品口味的时候，要把口味对应的菜品id和相应的菜品id对上
            而只有每次插入菜品之后才知道菜品的id，所以要进行上面的获取菜品id那一步
             */
            flavors.forEach(flavor -> {flavor.setDishId(dishId);});
            //向口味表插入n条数据,实现批量插入
            dishFlavorMapper.insertBatch(flavors);

        }


    }
}
