package com.pethealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethealth.entity.Alert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警记录Mapper
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {
}
