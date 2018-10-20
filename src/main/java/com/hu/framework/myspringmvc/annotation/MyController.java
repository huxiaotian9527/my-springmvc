package com.hu.framework.myspringmvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义的controller注解
 * @author hutiantian
 * @date: 2018/10/20 11:55
 * @since 1.0.0
 */
@Target({ElementType.TYPE})     //作用域 class interface
@Retention(RetentionPolicy.RUNTIME)
public @interface MyController {
}
