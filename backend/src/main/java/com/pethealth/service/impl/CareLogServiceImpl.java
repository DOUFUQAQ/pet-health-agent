package com.pethealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.common.ResultCode;
import com.pethealth.dto.CareLogDTO;
import com.pethealth.entity.CareLog;
import com.pethealth.entity.Pet;
import com.pethealth.exception.BusinessException;
import com.pethealth.mapper.CareLogMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.service.ICareLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 养护日志服务实现
 */
@Service
public class CareLogServiceImpl extends ServiceImpl<CareLogMapper, CareLog> implements ICareLogService {

    @Resource
    private PetMapper petMapper;

    @Override
    public CareLog addCareLog(Long userId, CareLogDTO dto) {
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }
        CareLog log = new CareLog();
        BeanUtils.copyProperties(dto, log);
        log.setUserId(userId);
        if (log.getLogTime() == null) log.setLogTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        baseMapper.insert(log);
        return log;
    }

    @Override
    public CareLog updateCareLog(Long userId, CareLogDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("日志ID不能为空");
        }
        CareLog log = baseMapper.selectById(dto.getId());
        if (log == null || !log.getUserId().equals(userId)) {
            throw new BusinessException("日志不存在");
        }
        BeanUtils.copyProperties(dto, log);
        baseMapper.updateById(log);
        return log;
    }

    @Override
    public void deleteCareLog(Long userId, Long logId) {
        CareLog log = baseMapper.selectById(logId);
        if (log == null || !log.getUserId().equals(userId)) {
            throw new BusinessException("日志不存在");
        }
        baseMapper.deleteById(logId);
    }

    @Override
    public IPage<CareLog> listPetCareLogs(Long userId, Long petId, String logType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CareLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareLog::getUserId, userId)
               .eq(CareLog::getPetId, petId)
               .eq(logType != null && !logType.isEmpty(), CareLog::getLogType, logType)
               .orderByDesc(CareLog::getLogTime);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
