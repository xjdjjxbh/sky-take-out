package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveSetmealWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        /*
        插入套餐的基本信息
         */
        setmealMapper.insert(setmeal);

        //获取刚才插入的套餐的id
        Long setmealId = setmeal.getId();

        //获取套餐里面所包含的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        if (setmealDishes != null) {
            setmealDishes.forEach(setmealDish -> {setmealDish.setSetmealId(setmealId);});
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public SetmealVO getByIdWithDish(Long setmealId) {
        //根据套餐的id获取套餐的基本信息
        Setmeal setmeal = setmealMapper.getById(setmealId);

        //根据套餐的id获取套餐里面所有的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishesBySetmealId(setmealId);

        //把套餐的基本信息和所有菜品的信息传递给要返回的视图对象
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
//        BeanUtils.copyProperties(setmealDishes, setmealVO);     这里的拷贝有错误，因为是应该把setmealDishes这个整体拷贝给setmeal里面的一个属性
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            //对所有套餐的id进行遍历，只要里面有套餐是起售状态，就不能进行删除操作
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //删除套餐表中的数据
        setmealMapper.deleteBatchs(ids);

        //删除套餐菜品关系表中的数据
        setmealDishMapper.deleteBatchs(ids);
    }

    /**
     * 起售或停售套餐
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                                .status(status)
                                .id(id)
                                .build();
        setmealMapper.updateSetmeal(setmeal);
    }

    @Transactional
    @Override
    public void updateSetmealWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //先修改套餐的基本信息
        setmealMapper.updateSetmeal(setmeal);


        //先删除当前套餐下面所有的菜品
        ArrayList<Long> list = new ArrayList<>(Collections.singletonList(setmeal.getId()));
        setmealDishMapper.deleteBatchs(list);

        //再来修改套餐里面的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            //因为菜品有可能是新添加的，还没有指定新添加的菜品是属于哪个套餐，所以要先指定一下所有的菜品属于哪个套餐
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
