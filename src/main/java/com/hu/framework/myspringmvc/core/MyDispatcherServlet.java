package com.hu.framework.myspringmvc.core;

import com.hu.framework.myspringmvc.annotation.MyAutoWired;
import com.hu.framework.myspringmvc.annotation.MyController;
import com.hu.framework.myspringmvc.annotation.MyRequestMapping;
import com.hu.framework.myspringmvc.annotation.MyService;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义拦截器类，替代springMVC的dispatcherServlet
 *
 * @author hutiantian
 * @date: 2018/10/20 13:51
 * @since 1.0.0
 */
//使用SpringBoot的注解使servlet生效
//自定义Servlet拦截路径为"/*"，默认服务启动时加载
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)          //这里用"/"匹配不到？
public class MyDispatcherServlet extends HttpServlet {

    private List<String> classList = new ArrayList<>();                 //指定路径下的所有类的全额类名

    private Map<String, Object> ioc = new HashMap<>();                  //模拟ioc容器，key-

    private Map<String,Method> handlerMapping = new HashMap<>();        //保存


    //通过注解获取application.yml中的扫描包路径
    @Value("${project.scan}")
    private String scanPath;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            doDispatcher(request,response);     //调用doPost方法，发射调用，将结果返回至浏览器
        }catch (Exception e){
            e.printStackTrace();
            response.getWriter().write("500 Exception happened in this server!");
        }
    }

    /**
     * 自定义初始化方法
     */
    @Override
    public void init(){
        try{
            //1.扫描指定包下文件，通过反射解析对应的注解
            doScanner(scanPath);

            //2.初始化相关类，并保存在IOC容器之中
            doInstance();

            //3.完成自动化注入
            doAutoWired();

            //4.创建HandlerMapping,将url和method建立关系
            doHandlerMapping();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);         //异常退出
        }

        System.out.println("MySpringMVC init success !!!!! ------hahaha---------");

    }

    /**
     * 扫描指定包下的所有的类，将其路径加载至list中
     */
    public void doScanner(String path) {
        //classLoader拿到的是包路径，所以要将scanPath的点转化成斜杠
        //classLoader读取路径不能加"/"
        URL url = this.getClass().getClassLoader().getResource(path.replace(".", "/"));
        //根据url读取文件
        File file = new File(url.getFile());
        for (File f : file.listFiles()) {
            String name = f.getName();
            if (f.isDirectory()) {
                doScanner(path + "." + name);                   //递归读取文件夹下的所有class文件
            } else {
                if (name.endsWith(".class")) {
                    classList.add(path + "." + name.substring(0, name.length() - 6));
                }
            }
        }
    }

    /**
     * 实例化带有注解的class
     */
    public void doInstance() {
        if (classList.isEmpty()) {
            System.out.println("----------未扫描到class文件，请检查配置文件路径！");
            return;
        }
        for (String name : classList) {
            try {
                Class clazz = Class.forName(name);
                //实例化有注解的class
                if (clazz.isAnnotationPresent(MyController.class)) {
                    //将@MyController的类放入ioc容器之中
                    ioc.put(toUpperCase(clazz.getSimpleName()), clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    //判断class是否给注解赋值
                    MyService myService = (MyService) clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    if ("".equals(beanName)) {
                        beanName = toUpperCase(clazz.getSimpleName());          //没有赋值，用类名首字母小写
                    }
                    ioc.put(beanName, clazz.newInstance());
                    //同时将service实现的接口放入ioc中
                    Class[] classes = clazz.getInterfaces();
                    for (Class c : classes) {
                        //TODO 多个class实现同一个接口
                        if (ioc.containsKey(c.getName() )){
                            throw new Exception("----error!-----this interface already has instanced");
                        }
                        ioc.put(c.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 自动注入DI
     */
    public void doAutoWired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (String clazz : ioc.keySet()) {
            Field[] fields = ioc.get(clazz).getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MyAutoWired.class)) {
                    //如果MyAutoWired没有赋值，则使用field的className来匹配ioc中的实例
                    MyAutoWired myAutoWired = field.getAnnotation(MyAutoWired.class);
                    String beanName = myAutoWired.value();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    field.setAccessible(true);
                    try {
                        //第一个参数代表当前需要给属性赋值的对象，
                        //第二个对象代表具体的赋值（就是field的type的实例化对象）
                        field.set(ioc.get(clazz), ioc.get(beanName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    continue;
                }
            }
        }
    }

    /**
     * 建立url和method对应
     */
    public void doHandlerMapping() throws Exception{
        if (ioc.isEmpty()) {
            return;
        }
        for (String key : ioc.keySet()) {
            Class clazz = ioc.get(key).getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }
            //获取Controller上的RequestMapping
            String baseUrl = ((MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class)).value();
            Method[] methods = clazz.getMethods();
            //获取方法上的RequestMapping
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                //拼接baseUrl，同时用正则表达式将多个"///"替换成一个"/"
                String url = (baseUrl+"/" + method.getAnnotation(MyRequestMapping.class).value() ).replaceAll("/+", "/");
                if(handlerMapping.containsKey(url)){
                    throw new Exception("this key already exit! "+ url);
                }
                handlerMapping.put(url,method);
            }
        }
    }

    /**
     * 具体业务逻辑处理，若无法匹配到url，返回404
     */
    private void doDispatcher(HttpServletRequest request,HttpServletResponse response) throws Exception{
        if(handlerMapping.isEmpty()){
            return;
        }
        String requestUrl = request.getRequestURI();            //这里是绝对路径
        requestUrl.replace(request.getContextPath(),"").replaceAll("/+","/");
        if(!handlerMapping.containsKey(requestUrl)){
            response.getWriter().write("404 not found!");
            return;
        }
        //获取到url对应的method
        Method method = handlerMapping.get(requestUrl);
        Map<String,String[]> paramMap = request.getParameterMap();
        //通过method找到method对应的class的name
        String beanName = toUpperCase(method.getDeclaringClass().getSimpleName());
        //通过className找到ioc中已经初始化好的实例
        String name = "";
        if(paramMap.get("name")!=null){
            name = paramMap.get("name")[0];
        }
        method.invoke(ioc.get(beanName),new Object[]{request,response,name});
        System.out.println("Mapped Success :"+requestUrl+" method: "+method.getName());
    }

    /**
     * 将字符串首字母小写
     */
    private String toUpperCase(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

}
