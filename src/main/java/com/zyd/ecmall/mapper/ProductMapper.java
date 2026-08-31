package com.zyd.ecmall.mapper;

import com.zyd.ecmall.dto.ProductSearchRequest;
import com.zyd.ecmall.entity.Product;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProductMapper {

    // 查询所有商品（按上架状态、分类等，先做简单的全查）
    @Select("SELECT id, name, description, price, stock, category, image_url, status, created_at, updated_at " +
            "FROM ec_mall.products ORDER BY id DESC")
    List<Product> selectAll();

    // 根据ID查询单个商品
    @Select("SELECT id, name, description, price, stock, category, image_url, status, created_at, updated_at " +
            "FROM ec_mall.products WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    // 新增商品（注意：created_at 和 updated_at 由数据库自动生成，不需要手动插入）
    @Insert("INSERT INTO ec_mall.products (name, description, price, stock, category, image_url, status) " +
            "VALUES (#{name}, #{description}, #{price}, #{stock}, #{category}, #{imageUrl}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    // 更新商品
    @Update("UPDATE ec_mall.products SET " +
            "name = #{name}, " +
            "description = #{description}, " +
            "price = #{price}, " +
            "stock = #{stock}, " +
            "category = #{category}, " +
            "image_url = #{imageUrl}, " +
            "status = #{status} " +
            "WHERE id = #{id}")
    int update(Product product);

    // 删除商品
    @Delete("DELETE FROM ec_mall.products WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 条件に一致する商品の総件数を取得 / 获取符合条件商品的总件数
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM ec_mall.products
        <where>
            <if test="keyword != null and keyword != ''">
                AND name LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test="category != null and category != ''">
                AND category = #{category}
            </if>
            <if test="minPrice != null">
                AND price &gt;= #{minPrice}
            </if>
            <if test="maxPrice != null">
                AND price &lt;= #{maxPrice}
            </if>
        </where>
        </script>
        """)
    long countBySearch(ProductSearchRequest request);

    /**
     * 条件に一致する商品リストをページングで取得 / 分页获取符合条件的商品列表
     */
    @Select("""
        <script>
        SELECT id, name, description, price, stock, category, image_url, status, created_at, updated_at
        FROM ec_mall.products
        <where>
            <if test="request.keyword != null and request.keyword != ''">
                AND name LIKE CONCAT('%', #{request.keyword}, '%')
            </if>
            <if test="request.category != null and request.category != ''">
                AND category = #{request.category}
            </if>
            <if test="request.minPrice != null">
                AND price &gt;= #{request.minPrice}
            </if>
            <if test="request.maxPrice != null">
                AND price &lt;= #{request.maxPrice}
            </if>
        </where>
        ORDER BY id DESC
        LIMIT #{offset}, #{request.size}
        </script>
        """)
    List<Product> searchProducts(@Param("request") ProductSearchRequest request,
                                 @Param("offset") int offset);

    /**
     * 在庫を減らす（楽観的ロック / 乐观锁防超卖）
     * 更新件数が0なら在庫不足 or 同時更新競合
     */
    @Update("""
        UPDATE ec_mall.products
        SET stock = stock - #{quantity}
        WHERE id = #{id} AND stock >= #{quantity}
    """)
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 在庫を増やす（キャンセル時に使用） / 增加库存（取消订单时使用）
     */
    @Update("UPDATE ec_mall.products SET stock = stock + #{quantity} WHERE id = #{id}")
    int addStock(@Param("id") Long id, @Param("quantity") Integer quantity);
        
}

