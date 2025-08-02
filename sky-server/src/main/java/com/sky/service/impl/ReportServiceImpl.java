package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        // 1.准备日期列表数据 dateList    ---> 近7日：5-14，5-15...5-20
        List<LocalDate> dateList = getDateList(begin, end);

        // 2.准备营业额列表数据 turnoverList
        List<Double> turnoverList = new ArrayList<>();
        // 营业额=订单状态已完成的订单金额
        // 查询orders表，条件：状态-已完成，下单时间
        dateList.forEach(date -> {
            // select sum(amount) from orders where status = 5 and order_time between '2024-05-14 00:00:00' and 2024-05-14 '23:59:59:999999999'
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));    // 2024-05-14 00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));      // 2024-05-14 23:59:59.999999999
            Double turnover = ordersMapper.sumByMap(map);
            // 用来处理没有营业额的情况
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        });


        // 3.构造TurnoverReportVO对象并返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ",")) // [5-14，5-15...5-20] --> "5-14,5-15,....5-20"
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 1.构造dateList数据
        List<LocalDate> dateList = getDateList(begin, end);

        // 2.构造newUserList数据，新增用户列表
        List<Integer> newUserList = new ArrayList<>();
        // 3.totalUserList数据，总用户列表
        List<Integer> totalUserList = new ArrayList<>();

        // 循环遍历日期列表统计每日的新增用户数---user
        dateList.forEach(date -> {
            // select count(*) from user where create_time >= 当天开始时间 and create_time <= 当天结束时间
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));    // 2024-05-14 00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));      // 2024-05-14 23:59:59.999999999
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);

            // select count(*) from user where create_time <=  当天结束时间
            map.put("beginTime", null);    // 2024-05-14 00:00
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
        });

        // 4. 构造UserReportVO对象并返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        // 1.获取日期列表数据 dateList
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        // 循环遍历日期列表进行订单统计
        for (LocalDate date : dateList) {
            // 2.获取每日总订单列表数 orderCountList
            // 统计每日orders数量，条件：下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));    // 2024-05-14 00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));      // 2024-05-14 23:59:59.999999999
            Integer totalOrder = ordersMapper.countByMap(map);
            orderCountList.add(totalOrder);

            // 3.获取每日有效订单列表数 validOrderCountList
            // 统计每日有效orders数量，条件：状态=有效（已完成）、 下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间
            map.put("status", Orders.COMPLETED);
            Integer validOrder = ordersMapper.countByMap(map);
            validOrderCountList.add(validOrder);

            // 4.获取区间订单总数 totalOrderCount
            // select count(*) from orders where 下单时间 >= '2024-05-14 00:00' 下单时间 <= '2024-05-20 23:59:59.999999999'
            totalOrderCount += totalOrder;

            // 5.获取有效订单数 validOrderCount
            validOrderCount += validOrder;
        }

        // 4.获取区间订单总数 totalOrderCount----其他写法
        // for (Integer orderCount : orderCountList) {
        //     totalOrderCount += orderCount;
        // }

        // totalOrderCount = orderCountList.stream().reduce(new BinaryOperator<Integer>() {
        //     @Override
        //     public Integer apply(Integer num1, Integer num2) {   //累加器方法
        //         return num1 + num2;
        //     }
        // }).get();

        // totalOrderCount = orderCountList.stream().reduce((num1, num2) -> num1 + num2).get();

        // totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);

        // 6.计算完成率 orderCompletionRate
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (validOrderCount + 0.0) / totalOrderCount;
        }

        // 7.封装OrderReportVO对象并返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }



    /**
     * 获取日期列表数据
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<LocalDate>();
        // 循环插入日期数据
        while (!begin.isAfter(end)) {
            // 注意：小心死循环
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        log.info("dateList = {}", dateList);
        return dateList;
    }
}
