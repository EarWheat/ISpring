package com.zero.ispring.demo;

import com.zero.ispring.annotations.iService;

/**
 * @author liuzhaoluliuzhaolu
 * @date 2021/8/2 8:05 下午
 * @desc
 * @prd
 * @Modification History:
 * Date         Author          Description
 * ------------------------------------------ *
 */
@iService
public class DemoServiceImpl implements DemoService {

    @Override
    public String getName(){
        return "zero";
    }
}
