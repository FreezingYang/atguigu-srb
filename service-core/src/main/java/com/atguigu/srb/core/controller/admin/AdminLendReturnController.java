package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.service.LendReturnService;
import com.atguigu.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Api(tags = "还款记录")
@RestController
@RequestMapping("/admin/core/lendReturn")
public class AdminLendReturnController {

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendReturn> list = lendReturnService.selectByLendId(lendId);

        return R.success().put("list", list);
    }
}
