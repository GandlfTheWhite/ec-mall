package com.zyd.gulimail.mapper;


import com.zyd.gulimail.entity.User;

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
}