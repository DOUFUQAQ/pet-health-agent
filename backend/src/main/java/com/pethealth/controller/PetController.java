package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.dto.PetDTO;
import com.pethealth.service.IPetService;
import com.pethealth.utils.SecurityUtil;
import com.pethealth.vo.PetVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物管理控制器
 */
@RestController
@RequestMapping("/pet")
public class PetController {

    @Resource
    private IPetService petService;

    /**
     * 获取宠物列表
     */
    @GetMapping("/list")
    public Result<List<PetVO>> list() {
        return Result.success(petService.listUserPets(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetVO> detail(@PathVariable Long id) {
        return Result.success(petService.getPetDetail(SecurityUtil.getCurrentUserId(), id));
    }

    /**
     * 添加宠物
     */
    @PostMapping
    public Result<PetVO> add(@Valid @RequestBody PetDTO petDTO) {
        return Result.success(petService.addPet(SecurityUtil.getCurrentUserId(), petDTO));
    }

    /**
     * 更新宠物
     */
    @PutMapping
    public Result<PetVO> update(@Valid @RequestBody PetDTO petDTO) {
        return Result.success(petService.updatePet(SecurityUtil.getCurrentUserId(), petDTO));
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.deletePet(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }
}
