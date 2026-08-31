package com.zyd.ecmall.mapper;

import com.zyd.ecmall.entity.OrderItem;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderItemMapper {

    @Insert("INSERT INTO ec_mall.order_item (order_id, product_id, product_name, price, quantity) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{price}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);

    @Select("SELECT * FROM ec_mall.order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

}
