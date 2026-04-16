package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @AutoFill(OperationType.INSERT)
    void insertBatch(List<DishFlavor> flavors);

    void deleteByDishId(List<Long> ids);
}
