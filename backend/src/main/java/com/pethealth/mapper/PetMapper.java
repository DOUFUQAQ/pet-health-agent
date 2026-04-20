package com.pethealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethealth.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物Mapper
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
