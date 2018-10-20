package com.hu.framework.myspringmvc.controller;

import com.hu.framework.myspringmvc.annotation.MyAutoWired;
import com.hu.framework.myspringmvc.annotation.MyController;
import com.hu.framework.myspringmvc.annotation.MyRequestMapping;
import com.hu.framework.myspringmvc.service.HelloService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试用的Controller
 * @author hutiantian
 * @date: 2018/10/20 8:23
 * @since 1.0.0
 */
@MyController
@MyRequestMapping(value="/myMvc")
public class HelloController {

    @MyAutoWired
    private HelloService helloService;          //实现自动注入

    @MyRequestMapping("/test")
    public void test(HttpServletRequest req, HttpServletResponse resp , String name) throws Exception{
        resp.getWriter().write( "Hello,"+helloService.sayHello(name));
    }
}
