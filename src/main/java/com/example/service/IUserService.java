package com.example.service;


import com.example.model.User;

import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */
public interface IUserService {

    int addUser(User user);

    List<User> findAllUser();//int pageNum, int pageSize
    User selectByPrimaryKey(Integer userId);
}