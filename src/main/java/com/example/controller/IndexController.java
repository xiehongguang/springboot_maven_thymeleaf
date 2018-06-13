package com.example.controller;

import com.example.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = {"/index"})
public class IndexController {
    @Autowired
    private IUserService userService;
    /**
     * 首页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"/index"})
    public String index(Model model) {
        model.addAttribute("name", "xhg");
        return "index";
    }

    /**
     * restAPI 返回json
     *
     * @return
     */
    @RequestMapping("/json")
    @ResponseBody
    public Map<String, Object> json() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "xhg");
        map.put("age", "18");
        map.put("sex", "man");
        return map;
    }

    /**
     * 返回所有用户
     * @return
     */
    @RequestMapping(value = {"/findAll"}, produces = {"application/json;charset=UTF-8"}, method = RequestMethod.GET)
    @ResponseBody
    public List<User> getAllUsers() {
        List<User> userInfo = userService.findAllUser();
        return userInfo;
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", produces = {"application/json;charset=UTF-8"})
    public int addUser(User user) {
        return userService.addUser(user);
    }

    /**
     * 指定用户
     * @return
     */
    @ResponseBody// @RequestMapping(value = "/all/{pageNum}/{pageSize}", produces = {"application/json;charset=UTF-8"})
    @RequestMapping(value = "/findUser")
    public User selectByPrimaryKey() {//@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize
        return userService.selectByPrimaryKey(1);//pageNum,pageSize
    }
}