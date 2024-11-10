package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造redis里面的key  dish_分类id
        String key = "dish_" + categoryId;

        //查询redis里面是否有返回数据（redis里面存储的是出于起售状态的菜品）
        /*
        这里放进去的时候是什么数据类型，那么取出来的时候就是什么数据类型，放进去的时候是一个种类下面的所有菜品，那么取出来的时候
        就是一个list，这个里面包含一个种类下面的所有菜品
         */
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);


        //如果存在，则不需要访问数据库，直接返回redis里面的缓存数据
        if (list != null && !list.isEmpty()) {
            return Result.success(list);
        }


        //如果不存在，访问数据库，并且把查询到的数据放入到redis里面去
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品


        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);     //把这个菜品列表作为字符串传到redis里面去


        return Result.success(list);
    }

}
