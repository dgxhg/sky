package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 每分钟执行：处理超时未支付订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);

        List<Orders> list = orderMapper.getByStatusAndOutTimeLT(Orders.PENDING_PAYMENT, localDateTime);

        if (list != null && !list.isEmpty()) {
            for (Orders order : list) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 每天凌晨1点执行：把派送中订单改为已完成
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliverOrder() {
        log.info("定时处理【派送中】的订单，自动改为已完成：{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(60);
        // 查询所有 派送中 的订单
        List<Orders> list = orderMapper.getByStatusAndOutTimeLT(Orders.DELIVERY_IN_PROGRESS, localDateTime);

        if (list != null && !list.isEmpty()) {
            for (Orders order : list) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}