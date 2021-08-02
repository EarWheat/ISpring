package com.zero.ispring.annotations;

import java.lang.annotation.*;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/7/30 2:11 下午
 * @desc 自定义service
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface iService {
    String value() default "";
}
