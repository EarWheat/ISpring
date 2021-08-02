package com.zero.ispring.annotations;

import java.lang.annotation.*;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/7/30 2:07 下午
 * @desc 自定义Controller
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface iController {
    String value() default "";
}
