package com.zyd.ecmall.mapper;

import com.zyd.ecmall.entity.Order;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO ec_mall.orders (order_no, member_id, order_date, total_amount, status, shipping_address, receiver_name, receiver_phone) " +
            "VALUES (#{orderNo}, #{memberId}, #{orderDate}, #{totalAmount}, #{status}, #{shippingAddress}, #{receiverName}, #{receiverPhone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM ec_mall.orders WHERE id = #{id}")
    Order selectById(@Param("id") Long id);
}