package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.result.R;
import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.pojo.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Slf4j
@Api(tags = "会员接口")
@RestController
@RequestMapping("/api/core/userBind")
public class UserBindController {

    @Autowired
    private UserBindService userBindService;

    @ApiOperation("账号绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(@RequestBody UserBindVO userBindVO, HttpServletRequest request){
        // 从header中获取token，并对token进行校验，确保用户已经登录，并从token中提取userId
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        // 根据userId做账户绑定，生成一个动态表单的字符串
        String formStr = userBindService.commitBindUser(userBindVO, userId);

        return R.success().put("formStr", formStr);
    }

    @ApiOperation("账号绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        // 汇付宝向尚融宝发起回调请求时携带的参数
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info(""+JSON.toJSONString(paramMap));

        // 校验签名
        if(!RequestHelper.isSignEquals(paramMap)){
            log.error("用户账号绑定异步回调签名验证失败");
            return "fail";
        }

        log.info("签名校验成功");
        try {
            userBindService.updateUserBindAndInfo(paramMap);
        } catch (Exception e) {
            log.error("用户账号绑定异常");
            return "fail";
        }

        return "success";
    }
}

