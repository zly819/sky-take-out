package com.sky.mapper;

import com.sky.entity.DishFlavor;

import java.util.List;

//注意：启动类上加了@Mapper注解，该接口的@Mapper注解可以省略
public interface DishFlavorMapper {


    /**
     * 批量插入口味列表数据
     * @param dishFlavorList
     */
    void insertBatch(List<DishFlavor> dishFlavorList);

    void deleteBatch(List<Long> dishIds);
}
