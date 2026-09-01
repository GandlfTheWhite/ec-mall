package com.zyd.ecmall.mapper;

import com.zyd.ecmall.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO ec_mall.orders (order_no, member_id, order_date, total_amount, status, shipping_address, receiver_name, receiver_phone) " +
            "VALUES (#{orderNo}, #{memberId}, #{orderDate}, #{totalAmount}, #{status}, #{shippingAddress}, #{receiverName}, #{receiverPhone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM ec_mall.orders WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    /**
     * 注文ステータスを更新する / 更新订单状态
     */
    @Update("UPDATE ec_mall.orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 会員IDで全注文を取得（作成日降順） / 根据会员ID获取所有订单
     */
    @Select("SELECT * FROM ec_mall.orders WHERE member_id = #{memberId} ORDER BY created_at DESC")
    List<Order> selectByMemberId(@Param("memberId") Long memberId);

    @Select("SELECT * FROM ec_mall.orders ORDER BY created_at DESC")
    List<Order> selectAll();

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
