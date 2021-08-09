package com.zero.ispring.servlet;

import com.zero.ispring.annotations.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/7/30 2:13 下午
 * @desc
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
public class IDispatchServlet extends HttpServlet{
    private Properties properties = new Properties();
    /**
     * 享元模式，缓存
     */
    private List<String> classNames = new ArrayList<String>();
    /**
     * ioc容器简单实现
     */
    private Map<String, Object> ioc = new HashMap<>();

//    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    /**
     * Mapping执行Hander
     */
    private List<Handler> handlers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception , Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }


    /**
     * 自定义dispatch
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    protected void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+","/");
//        if(!this.handlers.contains(url)){
//            resp.getWriter().write("404 Not Found!");
//        }

        Map<String, String[]> params = req.getParameterMap();
        for(Handler handler : handlers){
            if(handler.match(url)){
                // 获取形参列表
                Method method = handler.getMethod();
                LinkedList<String> paramList = handler.getParamList();
                LinkedList<Object> paramValues = new LinkedList<>();
                paramList.forEach(param -> {
                    paramValues.add(params.get(param)[0]);
                });

                method.invoke(handler.getController(), paramValues.toArray());
            }
        }
        resp.getWriter().write("404 Not Found!");

//        Map<String, String[]> params = req.getParameterMap();
//        Method method = this.handlers.get(url);
//        // 获取形参列表
//        Class<?>[] parameterTypes = method.getParameterTypes();
//        Object[] paramValues = new Object[parameterTypes.length];
//        for (int i = 0; i < parameterTypes.length; i++) {
//            Class<?> parameterType = parameterTypes[i];
//            if (parameterType == HttpServletRequest.class) {
//                paramValues[i] = req;
//            } else if (parameterType == HttpServletResponse.class) {
//                paramValues[i] = resp;
//            } else if (parameterType == String.class) {
//                // 通过运行时的状态去拿到注解的值
//                Annotation[][] pa = method.getParameterAnnotations();
//                for (int j = 0; j < pa.length; j++) {
//                    for (Annotation a : pa[j]) {
//                        if (a instanceof iRequestParam) {
//                            String paramName = ((iRequestParam) a).value();
//                            if (!"".equals(paramName.trim())) {
//                                String value = Arrays.toString(params.get(paramName))
//                                        .replaceAll("\\[|\\]", "")
//                                        .replaceAll("\\s+", "");
//                                paramValues[i] = value;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName().trim());
//        method.invoke(ioc.get(beanName), new Object[]{req, resp});
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.加载配置文件  contextConfigLocation对应web.xml中的init-param
        loadConfig(config.getInitParameter("contextConfigLocation"));
        // 2.扫描相关的类
        scanner(properties.getProperty("scanPackage"));
        //=================IOC部分=============//
        // 3.初始化IOC容器，将扫描到的相关的类实例化，保存到IOC容器中
        instance();
        // AOP，DI之前，新生成的代理对象
        //================DI部分==============//
        // 4.完成依赖注入
        autoWired();
        // 5. url mapping
        loadMapping();

    }

    /**
     * 加载配置文件
     * @param contextConfigLocation
     */
    private void loadConfig(String contextConfigLocation){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 包扫描
     * @param scanPackage
     */
    private void scanner(String scanPackage){
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        if(Objects.nonNull(url)){
            File classPath = new File(url.getFile());
            // 当成是一个classPath文件夹
            for(File file : Objects.requireNonNull(classPath.listFiles())){
                if(file.isDirectory()){
                    // 递归遍历子文件夹
                    scanner(scanPackage + "." + file.getName());
                } else {
                    if(file.getName().endsWith(".class")) {
                        String className = file.getName().replace(".class","");
                        // 防止名字重复，这里使用包名加上类名
                        classNames.add(scanPackage.concat(".").concat(className));
                    }
                }
            }
        }
    }

    /**
     * IOC容器初始化
     */
    private void instance(){
        // 没有bean
        if(classNames.isEmpty()){
            return;
        }
        for(String className : classNames){
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(iController.class)){
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                } else if(clazz.isAnnotationPresent(iService.class)){
                    // 自定义名字
                    String beanName = clazz.getAnnotation(iService.class).value();
                    // 未命名
                    if("".equalsIgnoreCase(beanName.trim())){
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    // 2. 默认的类名首字母小写
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // TODO:接口注释测试
                    // 3.如果是接口
                    // 判断有多少个实现类，如果只有一个，默认选择这个实现类，
                    // 如果有多个，抛异常
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The " + i.getName() + " is exists !!! ");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * DI注入
     */
    private void autoWired(){
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()){
            for(Field field : entry.getValue().getClass().getDeclaredFields()){
                if(!field.isAnnotationPresent(iAutowired.class)){
                    return;
                }
                iAutowired autowired = field.getAnnotation(iAutowired.class);
                // 如果用户没有自定义的beanName，就默认根据类型注入
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    // 接口全名
                    beanName = field.getType().getName();
                }
                // 对于类中的private属性的成员进行暴力访问
                field.setAccessible(true);
                try {
                    // ioc.get(beanName) 相当于通过接口的全名从IOC中拿到接口的实现的实例
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 首字母小写
     *
     * @param simpleName
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * requestMapping
     */
    private void loadMapping(){
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(iController.class)){
                continue;
            }
            String startUrl = "";
            // controller上的requestMapping
            if(clazz.isAnnotationPresent(iRequestMapping.class)){
                String requestUrl = clazz.getAnnotation(iRequestMapping.class).value().trim();
                if(!"".equals(requestUrl) && !"/".equals(requestUrl)){
                    startUrl = requestUrl;
                }
            }
            for(Method method : entry.getValue().getClass().getMethods()){
                if(!method.isAnnotationPresent(iRequestMapping.class)){
                    return;
                }
                iRequestMapping requestMapping = method.getAnnotation(iRequestMapping.class);
                // 如果用户没有自定义的beanName，就默认根据类型注入
                String url = requestMapping.value().trim();
                String requestUrl = startUrl.concat(url);
                try {
                    // ioc.get(beanName) 相当于通过接口的全名从IOC中拿到接口的实现的实例
//                    handlerMapping.put(requestUrl,method);
                    handlers.add(new Handler(clazz, method, requestUrl));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
