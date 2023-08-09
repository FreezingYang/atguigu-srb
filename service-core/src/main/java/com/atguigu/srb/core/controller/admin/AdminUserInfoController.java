package com.atguigu.srb.core.controller.admin;


import com.atguigu.common.result.R;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Slf4j

@Api(tags = "会员接口")
@RestController
@RequestMapping("/admin/core/userInfo")
public class AdminUserInfoController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true) @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true) @PathVariable Long limit,
            @ApiParam(value = "查询对象") UserInfoQuery userInfoQuery
    ){

        Page<UserInfo> userInfoPage = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.listPage(userInfoPage, userInfoQuery);

        return R.success().put("pageModel", pageModel);
    }

    @ApiOperation("锁定或解锁用户状态")
    @PutMapping("/lock/{id}/{status}")
    public R lock(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "状态（0：锁定 1：正常）", required = true)
            @PathVariable("status") Integer status
    ){

        LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfo::getId, id).set(UserInfo::getStatus, status);
        userInfoService.update(updateWrapper);
        return R.success(status==1 ? "解锁成功" : "锁定成功");
    }

}

