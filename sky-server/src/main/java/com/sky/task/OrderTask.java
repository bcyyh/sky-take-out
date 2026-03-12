package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类 定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    /**
     * 处理超时订单
     */
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 0/1 * * * ?")
//@Scheduled(cron = "1/5 * * * * ? ")

    public void processTimeoutOrders(){
        log.info("定时处理超时订单{}", LocalDateTime.now());

        // select * from orders where status =? and order_time < ?(当前时间-15)
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (orderList != null && orderList.size() > 0){
            for (Orders orders : orderList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }
    /**
     * 处理一直处在派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ? ")

    public void processDeliveryOrders(){
        log.info("定时处理处于派送中的订单{}", LocalDateTime.now());
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(60));
        if (orderList != null && orderList.size() > 0){
            for (Orders orders : orderList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
