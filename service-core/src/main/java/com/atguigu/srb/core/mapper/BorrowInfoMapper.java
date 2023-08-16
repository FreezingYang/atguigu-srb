package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

    Page<BorrowInfo> selectBorrowInfoList(Page<BorrowInfo> page);

}
