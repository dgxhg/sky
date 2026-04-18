package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    SetmealMapper setmealMapper;
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
                Dish dish = dishMapper.getById(id);
                if(dish.getStatus() == StatusConstant.ENABLE) throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            List<Long> setmealIdByDishIds = setmealDishMapper.getSetmealIdByDishIds(ids);
            if(setmealIdByDishIds.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            for (Long id : ids) {
                Dish dish = dishMapper.getById(id);
                String key = "dish_"+dish.getCategoryId();
                redisTemplate.delete(key);
            }
            dishMapper.deleteBatch(ids);
            dishFlavorMapper.deleteByDishIds(ids);
        }
    }

    @Override
    public DishDTO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        DishDTO dishDTO = new DishDTO();
        BeanUtils.copyProperties(dish, dishDTO);
        List<DishFlavor> flavor = dishFlavorMapper.getByDishId(id);
        dishDTO.setFlavors(flavor);
        return dishDTO;
    }
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        Long dishId = dishDTO.getId();
        dishFlavorMapper.deleteByDishId(dishId);
        List<DishFlavor>flavors = dishDTO.getFlavors();

        if(flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {flavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        // 【修正1】只有停售菜品时，才需要校验是否关联起售套餐
        if (status == StatusConstant.DISABLE) {
            // 查询所有包含这个菜品的套餐
            List<Setmeal> setmealList = setmealMapper.getByDishId(id);
            // 【修正2】如果存在起售的套餐，就不能停售菜品
            if (setmealList != null && !setmealList.isEmpty()) {
                for (Setmeal setmeal : setmealList) {
                    if (setmeal.getStatus() == StatusConstant.ENABLE) {
                        throw new DeletionNotAllowedException(
                                "该菜品已关联起售套餐，无法停售！");
                    }
                }
            }
        }

        // 更新菜品状态
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

}