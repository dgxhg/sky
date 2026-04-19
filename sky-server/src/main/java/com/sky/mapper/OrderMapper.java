package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单状态和订单超时时间 查询订单
     * @param status 订单状态
     * @param dateTime 时间
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{dateTime}")
    List<Orders> getByStatusAndOutTimeLT(Integer status, LocalDateTime dateTime);
    void update(Orders order);
}