package com.example.mapper;

import com.example.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper //Spring Boot在初始化mybatis时会自动加载该mapper类。
public interface UserMapper {
    int deleteByPrimaryKey(Integer userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    //这个方式我自己加的
    List<User> selectAllUser();
}