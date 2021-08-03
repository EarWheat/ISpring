package com.zero.ispring.demo;

import com.zero.ispring.annotations.iAutowired;
import com.zero.ispring.annotations.iController;
import com.zero.ispring.annotations.iRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/7/30 3:16 下午
 * @desc
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@iController
@iRequestMapping("/demo")
public class Demo {

    @iAutowired
    private DemoService demoService;

    @iRequestMapping("/hello")
    public String helloWorld(HttpServletRequest request, HttpServletResponse response){
        String name = demoService.getName();
        return "Hello World!".concat(name);
    }
}
