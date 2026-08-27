package com.zyd.ecmall.mapper;

import com.zyd.ecmall.entity.Cart;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CartMapper {

    /**
     * 会員IDからカートを取得（存在しなければ新規作成して返す）
     */
    @Select("SELECT id, member_id, created_at, updated_at FROM ec_mall.cart WHERE member_id = #{memberId}")
    Cart selectByMemberId(@Param("memberId") Long memberId);

    /**
     * カート新規作成
     */
    @Insert("INSERT INTO ec_mall.cart (member_id) VALUES (#{memberId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);
}