package com.sky.mapper;

import com.sky.entity.SetmealDish;

import java.util.List;

public interface SetmealDishMapper {
    /**
     * 根据id查询菜品
     * @param dishIds
     * @return
     */
    Integer countByDishId(List<Long> dishIds);

    /**
     *启用或停用菜品
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
