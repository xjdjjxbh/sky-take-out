package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.controller.admin.DishController;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /*
    菜品分页查询
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        //开启分页查询，开启滞后它可以动态的修改sql为分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        //把查询出来的结果赋值给pageResult对象，这是固定的写法，与前端达成的协议，方便前端查看数据
        return new PageResult( page.getTotal(), page.getResult());
    }

    @Override
    /**
     * 批量删除菜品
     */
    @Transactional     //里面涉及到了对多张表的操作，所以这里要加事务注解
    public void deleteBatch(List<Long> dishIds) {
        //判断当前菜品是否能够删除--是否存在启售中的菜品
        for (Long id : dishIds) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                //当前菜品处于启售中
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否被套餐关联，关联了则不能删除
        List<Long> setmealids = setmealDishMapper.getSetmealDishIdsByDishIds(dishIds);
        if (setmealids != null && setmealids.size() > 0) {
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //删除菜品表中的菜品数据
//        for (Long id : dishIds) {
//            dishMapper.deleteById(id);
//            //删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //根据菜品id集合，批量删除菜品数据
        dishMapper.deleteByIds(dishIds);

        //根据菜品id集合，批量删除关联的口味数据
        dishFlavorMapper.deleteByDishIds(dishIds);
    }

    @Override
    /**
     * 根据菜品id查询菜品信息和对应的口味数据
     */
    public DishVO getByIdWithFlavor(Long id) {

        //查询菜品表，获取菜品的基本信息
        Dish dish = dishMapper.selectById(id);

        //查询菜品的口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        //将查询到的数据进行封装
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品表基本信息   因为dishDTO里面还包含了口味数据，而我们只需要dish的基本信息，所以使用dishDTO不太合理
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //先删除原来的口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());

        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            /*
             当前的口味有可能是新增出来的(例如菜品A之前只有辣味，现在又出了甜味的，所以我们要给这个甜味指定它所对应的菜是什么)
             所以在插入口味数据之前，还要设置这个口味对应的菜品的id
             */
            flavors.forEach(flavor -> {flavor.setDishId(dishDTO.getId());});

            //向口味表插入n条数据,实现批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status).build();
        dishMapper.update(dish);
    }

    /**
     * 根据种类id获取菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
