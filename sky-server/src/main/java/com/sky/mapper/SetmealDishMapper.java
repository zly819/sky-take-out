package com.sky.mapper;

import java.util.List;

public interface SetmealDishMapper {
    /**
     * 根据id查询菜品
     * @param ids
     * @return
     */
    Integer countByDishId(List<Long> dishIds);
}
