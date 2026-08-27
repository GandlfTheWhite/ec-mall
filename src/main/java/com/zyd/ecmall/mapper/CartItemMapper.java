package com.zyd.ecmall.mapper;

import com.zyd.ecmall.dto.CartResponse.CartItemDetail;
import com.zyd.ecmall.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {

    /**
     * カート内の全明細＋商品情報を一括取得（JOINクエリ）
     */
    @Select("""
        SELECT
            ci.product_id AS productId,
            p.name AS productName,
            p.image_url AS imageUrl,
            ci.quantity,
            ci.price_at_add AS priceAtAdd,
            p.price AS currentPrice
        FROM ec_mall.cart_item ci
        INNER JOIN ec_mall.products p ON ci.product_id = p.id
        WHERE ci.cart_id = #{cartId}
        ORDER BY ci.created_at DESC
        """)
    List<CartItemDetail> selectCartDetailsByCartId(@Param("cartId") Long cartId);

    /**
     * 特定の商品がカートに存在するか確認
     */
    @Select("SELECT id, cart_id, product_id, quantity, price_at_add FROM ec_mall.cart_item WHERE cart_id = #{cartId} AND product_id = #{productId}")
    CartItem selectByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * 明細追加
     */
    @Insert("INSERT INTO ec_mall.cart_item (cart_id, product_id, quantity, price_at_add) VALUES (#{cartId}, #{productId}, #{quantity}, #{priceAtAdd})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem item);

    /**
     * 明細の数量更新
     */
    @Update("UPDATE ec_mall.cart_item SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 明細削除（特定商品をカートから外す）
     */
    @Delete("DELETE FROM ec_mall.cart_item WHERE cart_id = #{cartId} AND product_id = #{productId}")
    int deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * カートを空にする（全明細削除）
     */
    @Delete("DELETE FROM ec_mall.cart_item WHERE cart_id = #{cartId}")
    int deleteAllByCartId(@Param("cartId") Long cartId);
}