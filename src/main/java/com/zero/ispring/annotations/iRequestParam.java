package com.zero.ispring.annotations;

import java.lang.annotation.*;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/8/3 5:21 下午
 * @desc
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface iRequestParam {
    String value() default "";
}
