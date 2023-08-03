package com.atguigu.srb.core.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Api(tags = "网站积分等级接口")
@RestController
@RequestMapping("/api/core/integralGrade")
public class IntegralGradeController {

    @ApiOperation("测试接口")
    @GetMapping("/test")
    public void listAll(){
        System.out.println(111);
    }

}

