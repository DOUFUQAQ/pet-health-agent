package com.pethealth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.dto.CareLogDTO;
import com.pethealth.entity.CareLog;

/**
 * 养护日志服务接口
 */
public interface ICareLogService extends IService<CareLog> {

    /**
     * 添加养护日志
     */
    CareLog addCareLog(Long userId, CareLogDTO careLogDTO);

    /**
     * 更新养护日志
     */
    CareLog updateCareLog(Long userId, CareLogDTO careLogDTO);

    /**
     * 删除养护日志
     */
    void deleteCareLog(Long userId, Long logId);

    /**
     * 分页查询宠物的养护日志
     */
    IPage<CareLog> listPetCareLogs(Long userId, Long petId, String logType, Integer pageNum, Integer pageSize);
}
