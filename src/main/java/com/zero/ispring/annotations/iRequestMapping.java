package com.zero.ispring.annotations;

import java.lang.annotation.*;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/7/30 2:08 下午
 * @desc 自定义RequestMapping
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface iRequestMapping {
    String value() default "";
}
