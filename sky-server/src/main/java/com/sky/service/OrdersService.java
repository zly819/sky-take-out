package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrdersService {

    /**
     * 用户下单
     * @param dto
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO dto);
}
