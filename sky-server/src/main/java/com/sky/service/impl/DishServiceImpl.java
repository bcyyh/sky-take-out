package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
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
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();

        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表插入1条数据

        dishMapper.insert(dish);
        //关键点：此时数据库会自动为这道菜生成一个唯一的 id、
        // 此时数据库中有值了 但是dish中没有值 所以要在Mapper.xml中主键回填
        //获取Insert语句生成的语句值
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //在同一次新增菜品的动作里，dishId 是唯一的，所有口味共用它。
            //在不同次新增菜品的动作里，dishId 是变化的
            //每一次都给实体类赋值
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
            };


    }
}
