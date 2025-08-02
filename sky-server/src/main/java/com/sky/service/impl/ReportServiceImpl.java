package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
