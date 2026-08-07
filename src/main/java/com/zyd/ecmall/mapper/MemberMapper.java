package com.zyd.ecmall.mapper;
import com.zyd.ecmall.entity.Member;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MemberMapper {

    @Select("""
        SELECT id, name, email, age, created_at
        FROM ec_mall.members
        ORDER BY id
        """)
    List<Member> selectAll();

    @Select("""
        SELECT id, name, email, age, created_at
        FROM ec_mall.members
        WHERE id = #{id}
        """)
//    Member selectById(Long id);
    Member selectById(@Param("id") Long id);

    @Select("""
    SELECT id, name, email, age, created_at, password_hash
    FROM ec_mall.members
    WHERE email = #{email}
    """)
    Member selectByEmail(@Param("email") String email);

    @Insert("""
    INSERT INTO ec_mall.members (name, email, age, password_hash)
    VALUES (#{name}, #{email}, #{age}, #{passwordHash})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);

    @Delete("""
    DELETE FROM ec_mall.members
    WHERE id = #{id}
    """)
    int deleteById(@Param("id") Long id);

    @Update("""
        UPDATE ec_mall.members
        SET name = #{name},
            email = #{email},
            age = #{age},
            password_hash = #{passwordHash}
        WHERE id = #{id}
        """)
    int update(Member member);

}


