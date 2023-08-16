package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-07-19
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {

        List<Dict> dictList = baseMapper.selectList(null);
        System.out.println(dictList);
//        创建ExcelDictDTO列表，将Dict列表转换成ExcelDictDTO列表
        List<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {

            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });


        return excelDictDTOList;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {

        try {
            //查询redis中是否存在数据列表
            log.info("redis数据查询");
            List<Dict> dictList = (List<Dict>)redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);
            if (dictList != null){
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis查询数据异常" + ExceptionUtils.getStackTrace(e));
        }
        // redis中没有数据列表，再查询mysql
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        List<Dict> dictList = baseMapper.selectList(queryWrapper);
        //填充hashChildren字段
        dictList.forEach(dict -> {
            //判断当前节点是否有子节点，找到当前的dict下级有没有子节点
            dict.setHasChildren(hasChildren(dict.getId()));
        });

        //将数据存入redis中
        try {
            log.info("redis数据存储");
            redisTemplate.opsForValue().set("srb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis存储数据异常" + ExceptionUtils.getStackTrace(e));
        }

        return dictList;
    }

    /**
     * 通过dictCode获取父节点数据
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);
        // 通过父节点id查询子节点后，返回dictList
        return this.listByParentId(dict.getId());
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        Dict parentDict = baseMapper.selectOne(queryWrapper);

        if (parentDict == null){
            return "";
        }

        queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("parent_id", parentDict.getId())
                .eq("value", value);
        Dict dict = baseMapper.selectOne(queryWrapper);

        if (dict == null){
            return "";
        }

        return dict.getName();
    }

    /**
     * 判断当前id所在的节点下是否有子节点
     * @param id
     * @return
     */
    private boolean hasChildren(Long id){
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

}
