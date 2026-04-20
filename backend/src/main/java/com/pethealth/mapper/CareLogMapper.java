package com.pethealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethealth.entity.CareLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 养护日志Mapper
 */
@Mapper
public interface CareLogMapper extends BaseMapper<CareLog> {
}
