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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author ZLY
 * @version 1.0
 */
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@RestController
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    public Result addDish(@RequestBody DishDTO dto) {
        log.info("新增菜品:{}", dto);
        dishService.addDish(dto);

        //缓存优化，清理缓存
        redisTemplate.delete("dish_" + dto.getCategoryId());

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

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 回显菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("{id}")
    public Result getById(@PathVariable Long id) {
        log.info("回显菜品：{}",id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dto
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dto) {
        log.info("修改菜品:{}",dto);
        dishService.update(dto);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }


    /**
     * 启用或停用菜品
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用或停用菜品")
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或停用菜品: status={}, id={}",status, id);
        dishService.startOrStop(status, id);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
}
