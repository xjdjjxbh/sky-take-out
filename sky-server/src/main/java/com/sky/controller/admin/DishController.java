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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping()
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

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
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品的起售，停售状态")
    public Result updateStatus(@PathVariable Integer status,Long id){
        log.info("更改菜品的起售停售状态");
        dishService.updateStatus(status,id);
        return Result.success();
    }

    @ApiOperation("根据种类id获取菜品")
    @GetMapping("list")
    public Result<List<Dish>> getDishByCategoryId(Long categoryId){
        log.info("根据种类id获取菜品");
        List<Dish> dishes = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishes);
    }
}
