package com.zero.ispring.servlet;

import com.zero.ispring.annotations.iRequestParam;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/8/4 8:00 下午
 * @desc
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@Data
public class Handler {
    private Object controller;
    private Method method;
    private String url;
    private LinkedList<String> paramList;

    public Handler(Object controller, Method method, String url) {
        this.controller = controller;
        this.method = method;
        this.url = url;
        paramList = new LinkedList<>();
        putParamsIndex(method);
    }

    private void putParamsIndex(Method method){
        // 提取加了iRequestParam的参数
        Annotation[][] annotations = method.getParameterAnnotations();
        Arrays.stream(annotations).forEach(annotations1 -> {
            Arrays.stream(annotations1).forEach(annotation -> {
                if(annotation instanceof iRequestParam){
                    String name = ((iRequestParam) annotation).value();
                    if(!"".equals(name.trim())){
                        paramList.add(name);
                    }
                }
            });
        });

        // 提取req & rsp
        Class<?>[] paramTypes = method.getParameterTypes();
        Arrays.stream(paramTypes).forEach(type -> {
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramList.add(type.getName());
            }
        });

    }

    /**
     * 根据requestUrl定位Handler
     * @param url
     * @return
     */
    public Boolean match(String url){
//        if(this.pattern.matcher(url)){
//
//        }
        return true;
    }
}
