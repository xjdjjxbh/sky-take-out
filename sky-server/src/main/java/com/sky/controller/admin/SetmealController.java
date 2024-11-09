package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "套餐相关接口")
@RestController()
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    @ApiOperation("新增套餐")
    @PostMapping()
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐:{}", setmealDTO);

        setmealService.saveSetmealWithDishes(setmealDTO);

        return Result.success();
    }

    @ApiOperation("分页查询套餐")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐信息:{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("根据id查询套餐信息")
    @GetMapping("/{id}")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id){
        log.info("根据id获取套餐信息");
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    //前端发过来的请求：http://localhost/api/setmeal?ids=10,9
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    public Result deleteSetmealByIds(@RequestParam List<Long> ids){
        log.info("批量删除套餐");
        setmealService.deleteBatch(ids);
        return Result.success();
    }


    @ApiOperation("起售或停售套餐")
    @PostMapping("/status/{status}")
    public Result startOrStopSetmeal(@PathVariable Integer status, Long id){
        log.info("起售或停售套餐");
        setmealService.startOrStop(status,id);
        return Result.success();
    }

    @ApiOperation("修改套餐")
    @PutMapping
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐");
        setmealService.updateSetmealWithDishes(setmealDTO);
        return Result.success();
    }


}
