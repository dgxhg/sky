package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    /**
     * 新增 菜品和口味一致性
     * 操作两张表开启事务保证数据一致性
     * @param dishDTO\
     */
    @Autowired
    DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        /**
         * 插入菜品
         */
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        /**
         * 插入口味道
         */
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {flavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult dishPageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<Dish> page = dishMapper.dishPageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        if(ids != null && ids.size() > 0) {
            for(Long id : ids) {
                Integer status = dishMapper.getById(id);
                if(status == StatusConstant.ENABLE) throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            List<Long> setmealIdByDishIds = setmealDishMapper.getSetmealIdByDishIds(ids);
            if(setmealIdByDishIds.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            dishMapper.deleteBatch(ids);
            dishFlavorMapper.deleteByDishId(ids);
        }
    }
}