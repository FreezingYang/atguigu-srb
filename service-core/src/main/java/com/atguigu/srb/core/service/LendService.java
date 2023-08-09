package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
public interface LendService extends IService<Lend> {

    void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);

    Page<Lend> listPage(Page<Lend> pageParam);

    Map<String, Object> getLendDetail(Long id);
}
