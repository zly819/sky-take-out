package com.sky.mapper;

import com.sky.entity.OrderDetail;

import java.util.List;

public interface OrderDetailMapper {

    //基于xml---foreach标签
    void insertBatch(List<OrderDetail> orderDetailList);
}
