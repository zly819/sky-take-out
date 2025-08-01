package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;

public interface OrderService {

    /**
     * 条件搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
}
