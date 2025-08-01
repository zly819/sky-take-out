package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ZLY
 * @version 1.0
 */
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单--最终目的：将订单数据存入表中（orders、order_detail）
     * @param dto
     * @return
     */
    @Transactional   //开启事务
    public OrderSubmitVO submit(OrdersSubmitDTO dto) {

        //查询地址表，获取收货人信息
        AddressBook addressBook = addressBookMapper.getById(dto.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询用户表，获取用户表信息
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }

        //查询购物车列表数据---只查询自己名下的购物车数据
        List<ShoppingCart> cartList = shoppingCartMapper.list(userId);
        if (cartList == null || cartList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.构造订单数据，存入orders表中
        Orders orders = new Orders();
        //拷贝属性值
        BeanUtils.copyProperties(dto, orders);
        //补充缺失属性
        orders.setNumber(System.currentTimeMillis() + "");//订单编号
        orders.setStatus(Orders.PENDING_PAYMENT);//订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setPayStatus(Orders.UN_PAID);//支付状态 0未支付 1已支付 2退款
        orders.setPhone(addressBook.getPhone());//获取收获人电话
        orders.setAddress(addressBook.getDetail());//获取详细地址
        orders.setUserName(user.getName());//下单人
        //新增订单数据
        ordersMapper.insert(orders);
        log.info("订单id:{}", orders.getId());

        //2.构造订单明细数据，存入order_detail表中
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //循环遍历购物车列表数据，构造订单明细
        cartList.forEach(cart ->{
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail,"id");
            //关联订单id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        });

        //批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetailList);

        //3.清空购物车--删除自己名下的购物车列表
        shoppingCartMapper.delete(userId);

        //4.构造OrderSubmitVO对象，并返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.selectById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

//        return vo;


        //-----------------------------------------------------------
        //模拟支付成功---修改订单状态
        //业务处理，修改订单状态--来单提醒
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);

        // 通过websocket通知商家
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1:来单通知
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号: " + outTradeNo);

        String msg = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(msg);
    }

    /**
     * 用户端订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryByUser(Integer page, Integer pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> ordersPage= ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        //查询出订单明细，并封装到OrderVO进行响应
        if (ordersPage != null && ordersPage.getTotal() > 0) {
            for (Orders orders : ordersPage) {
                Long ordersId = orders.getId();//订单id

                //查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
            return new PageResult(ordersPage.getTotal(), list);
        }
        return null;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        // 根据id查询订单
        Orders orders = ordersMapper.getById(id);

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        Orders ordersDB = ordersMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            //当订单状态大于2时，不可取消订单
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            //  weChatPayUtil.refund(
            //          ordersDB.getNumber(), //商户订单号
            //          ordersDB.getNumber(), //商户退款单号
            //          new BigDecimal(0.01),//退款金额，单位 元
            //          new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    @Override
    public void reminder(Long id) {
        // 催单----》用户点击催单按钮，需要服务端给客户端推送消息
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type", 2);         //2-代表催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
