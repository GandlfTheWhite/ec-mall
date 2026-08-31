package com.zyd.ecmall.mapper;


import com.zyd.ecmall.entity.User;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
        SELECT id, name, age, email, create_time
        FROM user
        ORDER BY id
        """)
    List<User> selectAll();

    @Select("""
        SELECT id, name, age, email, create_time
        FROM user
        WHERE id = #{id}
        """)
    User selectById(Long id);

    /**
     * 指定時間以上経過した未払い注文を取得する / 获取超时未支付的订单
     */
    @Select("""
        SELECT * FROM ec_mall.orders
        WHERE status = 0
          AND created_at < #{timeoutDateTime}
        """)
    List<Order> selectTimeoutOrders(@Param("timeoutDateTime") LocalDateTime timeoutDateTime);
    
    /**
     * 注文をキャンセル（ステータス=4）に更新する / 取消订单
     */
    @Update("UPDATE ec_mall.orders SET status = 4 WHERE id = #{id}")
    int cancelOrder(@Param("id") Long id);
    
}
