package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 删除菜品
     * @param ids
     */
    @Transactional   //0.开启事务
    public void delete(List<Long> ids) {
        //1.删除菜品之前，需要判断菜品是否起售，起售中不允许删除
        ids.forEach(id ->{
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //2.需要判断菜品是否被套餐管关联，关联了也不允许删除
        Integer count  = setmealDishMapper.countByDishId(ids);
        if (count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品基本信息 dish表
        dishMapper.deleteBatch(ids);
        //4.删除菜品口味列表信息 dish_flavor表
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {

        DishVO dishVO = new DishVO();

        //1.根据菜品id查询菜品基本信息
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);

        //2.根据id查询口味列表信息
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        dishVO.setFlavors(flavors);

        //3.返回DishVO对象
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dto
     */
    @Transactional         //开启事务
    public void update(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);

        //1.修改菜品的基本信息
        dishMapper.update(dish);

        //2.修改口味列表信息
        //由于口味数据可能增加，删除，修改，涉及到增删改操作，所以先全部删除旧数据，再添加新的
        dishFlavorMapper.deleteByDishId(dto.getId());

        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && flavors.size() > 0){
            //关联菜品id
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }



    }
}
