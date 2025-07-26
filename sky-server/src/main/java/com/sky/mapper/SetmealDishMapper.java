package com.sky.mapper;

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
}
