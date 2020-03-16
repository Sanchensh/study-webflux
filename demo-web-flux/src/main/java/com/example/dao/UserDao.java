package com.example.dao;

import com.example.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface UserDao {

    @Select("select * from t_user where id = #{id}")
    User find(Integer id);

    @Select("select * from t_user")
    List<User> findAll();

    @Insert("insert into t_user(username) values (#{userName})")
    int insert(User user);

    @Update("update t_user set username = #{userName} where id = #{id}")
    int update(User user);

    @Delete("delete from t_user where id = #{id}")
    int delete(Integer id);
}
