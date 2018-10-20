package com.hu.framework.myspringmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan       //加上这个@WebServlet才会生效
public class MySpringmvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringmvcApplication.class, args);
    }
}
