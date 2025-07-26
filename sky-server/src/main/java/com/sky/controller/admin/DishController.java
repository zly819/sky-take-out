package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 张立业
 * @version 1.0
 */
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@RestController
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    public Result addDish(@RequestBody DishDTO dto) {
        log.info("新增菜品:{}", dto);
        dishService.addDish(dto);
        return Result.success();
    }

    /**
     * 分页查询菜品列表
     * @param dto
     * @return
     */
    @ApiOperation("分页查询菜品列表")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dto){
        log.info("分页查询菜品列表:{}",dto);
        PageResult pageResult = dishService.page(dto);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品：{}",ids);
        dishService.delete(ids);
        return Result.success();
    }
}
