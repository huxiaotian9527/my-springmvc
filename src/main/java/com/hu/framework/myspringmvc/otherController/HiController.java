package com.hu.framework.myspringmvc.otherController;

import com.hu.framework.myspringmvc.annotation.MyAutoWired;
import com.hu.framework.myspringmvc.annotation.MyController;
import com.hu.framework.myspringmvc.annotation.MyRequestMapping;
import com.hu.framework.myspringmvc.service.HelloService;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hutiantian
 * @date: 2018/10/20 14:30
 * @since 1.0.0
 */
@MyController
@MyRequestMapping(value="/myMvc")
public class HiController {

    @MyAutoWired
    private HelloService helloService;

    //注意：这里不是自定义注解，所以匹配不到
    @RequestMapping("/testHi")
    public String test(String name){
        return "I am Hi,"+helloService.sayHello(name);
    }
}
