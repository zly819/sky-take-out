package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 张立业
 * @version 1.0
 */
@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品
     * @param dto
     */
    @Transactional   //开启事务，涉及到多张表的增产改查需要开事务
    public void addDish(DishDTO dto) {
        //1.构造菜品基本信息数据，将其存入dish表中
        Dish dish = new Dish();
        //拷贝属性值
        BeanUtils.copyProperties(dto, dish);
        //调用mapper保存方法
        dishMapper.insert(dish);
        //todo
        log.info("dishId={}", dish.getId());

        //2.构造菜品口味列表数据，将其存入dish_flavor表中
        List<DishFlavor> dishFlavorList = dto.getFlavors();
        //2.1关联菜品id
        dishFlavorList.forEach(flavor -> {
            flavor.setDishId(dish.getId());
        });
        //2.2调用mapper保存方法，批量插入口味列表数据
        dishFlavorMapper.insertBatch(dishFlavorList);

    }

    /**
     * 分页查询菜品列表
     * @param dto
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dto) {
        //1.设置分页参数
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        //2.调用mapper的类表查询，强转为page
         Page<DishVO> page = dishMapper.list(dto);
        //3.封装PageResult对象并返回
        return new PageResult(page.getTotal(), page.getResult());
    }
}
