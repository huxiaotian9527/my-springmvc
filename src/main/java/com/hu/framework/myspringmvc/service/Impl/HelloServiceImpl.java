package com.hu.framework.myspringmvc.service.Impl;

import com.hu.framework.myspringmvc.annotation.MyService;
import com.hu.framework.myspringmvc.service.HelloService;

/**
 * hello实现类
 * @author hutiantian
 * @date: 2018/10/20 13:22
 * @since 1.0.0
 */
@MyService
public class HelloServiceImpl implements HelloService {
    public String sayHello(String name){
        return "I am "+name;
    }
}
