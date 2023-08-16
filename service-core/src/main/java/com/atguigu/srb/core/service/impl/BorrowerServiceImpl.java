package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.IntegralEnum;
import com.atguigu.srb.core.mapper.BorrowerAttachMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.mapper.UserIntegralMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.pojo.entity.BorrowerAttach;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserIntegral;
import com.atguigu.srb.core.pojo.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerAttachVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerAttachService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private BorrowerAttachMapper borrowerAttachMapper;

    @Autowired
    private UserIntegralMapper userIntegralMapper;

    @Autowired
    private DictService dictService;

    @Autowired
    private BorrowerAttachService borrowerAttachService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {
        // 获取用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus()); //认证中
        baseMapper.insert(borrower);

        // 保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        // 更新userInfo中的借款人认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(queryWrapper);
        if (objects.size() == 0) {
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }

        return (Integer) objects.get(0);
    }

    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword) {
        if(StringUtils.isBlank(keyword)){
            return baseMapper.selectPage(pageParam, null);
        }

        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .like("name", keyword)
                .or().like("id_card", keyword)
                .or().like("mobile", keyword)
                .orderByDesc("id");

        return baseMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {
        // 获取借款人信息
        Borrower borrower = baseMapper.selectById(id);

        // 填充基本借款人信息
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower, borrowerDetailVO);

        // 婚否
        borrowerDetailVO.setMarry(borrower.getMarry() ? "是" : "否");
        // 性别
        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男" : "女");

        // 下拉列表
        borrowerDetailVO.setEducation(dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation()));
        borrowerDetailVO.setIndustry(dictService.getNameByParentDictCodeAndValue("industry", borrower.getIndustry()));
        borrowerDetailVO.setIncome(dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome()));
        borrowerDetailVO.setReturnSource(dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource()));
        borrowerDetailVO.setContactsRelation(dictService.getNameByParentDictCodeAndValue("contactsRelation", borrower.getContactsRelation()));

        // 审核状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        // 附件列表
        List<BorrowerAttachVO> borrowerAttachVOList = borrowerAttachService.selectBorrowerAttachVOList(borrower.getId());
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        // 获取借款额度申请ID
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        //获取借款额度申请对象
        Borrower borrower = baseMapper.selectById(borrowerId);

        // 设置审核状态
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);

        // 获取用户ID
        Long userId = borrower.getUserId();
        // 获取用户对象
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 获取原始积分
        Integer integral = userInfo.getIntegral();

        // 计算基本信息积分
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);
        int currentIntegral = integral + borrowerApprovalVO.getInfoIntegral();

        // 身份证积分
        if (borrowerApprovalVO.getIsIdCardOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
            currentIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
        }

        // 房产积分
        if (borrowerApprovalVO.getIsHouseOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
            currentIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
        }

        // 车辆积分
        if (borrowerApprovalVO.getIsCarOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
            currentIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
        }

        // 设置用户总积分
        userInfo.setIntegral(currentIntegral);

        // 修改审核状态
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        // 更新用户信息
        userInfoMapper.updateById(userInfo);

    }

}
