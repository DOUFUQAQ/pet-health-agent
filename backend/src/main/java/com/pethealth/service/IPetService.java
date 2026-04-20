package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.dto.PetDTO;
import com.pethealth.entity.Pet;
import com.pethealth.vo.PetVO;

import java.util.List;

/**
 * 宠物服务接口
 */
public interface IPetService extends IService<Pet> {

    /**
     * 添加宠物
     */
    PetVO addPet(Long userId, PetDTO petDTO);

    /**
     * 更新宠物信息
     */
    PetVO updatePet(Long userId, PetDTO petDTO);

    /**
     * 删除宠物
     */
    void deletePet(Long userId, Long petId);

    /**
     * 获取用户的所有宠物
     */
    List<PetVO> listUserPets(Long userId);

    /**
     * 获取宠物详情
     */
    PetVO getPetDetail(Long userId, Long petId);
}
