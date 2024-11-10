package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping()
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);

        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据,从redis里面删除对应种类下面的数据
//        String key = "dish_" + dishDTO.getCategoryId();
//        redisTemplate.delete(key);

        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    /*
     *  分页查询的时候固定返回PageResult类型的结果，里面包含总共的记录条数和查询出来的结果列表.这是固定的写法
     */
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     *
     */
    @DeleteMapping
    @ApiOperation("菜品的批量删除")
    //添加了RequestParam 注解之后，springMVC框架会自动地解析ids里面的1,2,3字符串，然后把他们分别添加到List集合
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的批量删除:{}",ids);
        dishService.deleteBatch(ids);

//        将所有的菜品缓存数据都清理掉.即删除所有以dish_开头的key
//        Set keys = redisTemplate.keys("dish_*");    //匹配到所有的指定类型的数据
//        redisTemplate.delete(keys);      //直接一次性删除所有的key
        cleanCache("dish_*");
        return Result.success();
    }

    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据菜品id查询菜品信息和对应的口味数据");
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping()
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //如果这里修改的是分类信息（也就是说本来是A类，然后改成了B类，那么会影响到两个分类下面的数据），如果修改的是其他的描述信息，比如价格，图片这些，那么只会影响一个分类下面的数据
        //也就是说这里有可能会影响一个分类下面的数据，也有可能影响两个分类下面的数据，为了简单起见，这里直接删除所有的缓存数据，因为修改菜品信息操作其实并不多，清空缓存影响不会太大
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品的起售，停售状态")
    public Result updateStatus(@PathVariable Integer status,Long id){
        log.info("更改菜品的起售停售状态");
        dishService.updateStatus(status,id);

//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        cleanCache("dish_*");

        return Result.success();
    }

    @ApiOperation("根据种类id获取菜品")
    @GetMapping("list")
    public Result<List<Dish>> getDishByCategoryId(Long categoryId){
        log.info("根据种类id获取菜品");
        List<Dish> dishes = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishes);
    }


    /*
    清理redis里面的缓存数据
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
