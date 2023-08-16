package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.LendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private IntegralGradeMapper integralGradeMapper;

    @Autowired
    private BorrowerMapper borrowerMapper;

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private DictService dictService;

    @Autowired
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        // 获取用户积分
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral();

        //根据积分查询额度
        QueryWrapper<IntegralGrade> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .le("integral_start", integral)
                .ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(queryWrapper);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }

        return integralGrade.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        // 获取用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 判断用户绑定状态
        Assert.isTrue(userInfo.getStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(), ResponseEnum.USER_NO_BIND_ERROR);
        // 判断借款人额度申请状态
        Assert.isTrue(userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(), ResponseEnum.USER_NO_AMOUNT_ERROR);
        // 判断借款人额度是否充足
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(), ResponseEnum.USER_AMOUNT_LESS_ERROR);
        // 存储BorrowInfo数据
        borrowInfo.setUserId(userId);
        // 百分比转小数
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        // 设置借款申请状态
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());

        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(queryWrapper);
        if (objects.size() == 0) {
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        return (Integer) objects.get(0);
    }

    @Override
    public IPage<BorrowInfo> listPage(Page<BorrowInfo> pageParam) {

        Page<BorrowInfo> borrowInfoPage = baseMapper.selectBorrowInfoList(pageParam);
        borrowInfoPage.getRecords().forEach(borrowInfo -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());

            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);
        });

        return borrowInfoPage;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        // 查询借款对象：BorrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());

        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        // 查询借款人对象：Borrower（BorrowerDetailVO）
        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(queryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        // 组装集合结果
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        // 修改借款审核的状态 borrow_info
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);

        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);

        // 如果审核通过，则产生新的标的记录 lend
        if(borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus()){
            // 创建新的标的
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);

        }


    }
}
