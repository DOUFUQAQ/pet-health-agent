package com.pethealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.common.ResultCode;
import com.pethealth.dto.PetDTO;
import com.pethealth.entity.Pet;
import com.pethealth.exception.BusinessException;
import com.pethealth.mapper.PetMapper;
import com.pethealth.service.IPetService;
import com.pethealth.utils.DateUtil;
import com.pethealth.vo.PetVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物服务实现
 */
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements IPetService {

    @Override
    public PetVO addPet(Long userId, PetDTO petDTO) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);
        pet.setUserId(userId);
        pet.setStatus(0);
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        pet.setDeleted(0);
        baseMapper.insert(pet);
        return toVO(pet);
    }

    @Override
    public PetVO updatePet(Long userId, PetDTO petDTO) {
        if (petDTO.getId() == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        Pet pet = baseMapper.selectById(petDTO.getId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }
        BeanUtils.copyProperties(petDTO, pet);
        pet.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(pet);
        return toVO(pet);
    }

    @Override
    public void deletePet(Long userId, Long petId) {
        Pet pet = baseMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }
        baseMapper.deleteById(petId);
    }

    @Override
    public List<PetVO> listUserPets(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId)
               .orderByDesc(Pet::getCreateTime);
        return baseMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PetVO getPetDetail(Long userId, Long petId) {
        Pet pet = baseMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }
        return toVO(pet);
    }

    private PetVO toVO(Pet pet) {
        PetVO vo = new PetVO();
        BeanUtils.copyProperties(pet, vo);
        if (pet.getBirthday() != null) {
            vo.setAge(DateUtil.calculateAge(pet.getBirthday()));
        }
        return vo;
    }
}
