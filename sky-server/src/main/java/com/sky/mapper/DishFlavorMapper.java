package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper //它属于“自我声明”。每个接口自己声明自己是一个 Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除口味数据
     * @param
     * @return
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);


    /**
     * 根据菜品id集合批量删除口味数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds); //XML 里的 collection 名字和 Java 接口里的 形参名 对上。
}
