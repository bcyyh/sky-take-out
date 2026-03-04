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
import com.sky.vo.DishVO;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 菜品分页查询
     * @param dishPageQueryDto
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDto) {
        PageHelper.startPage(dishPageQueryDto.getPage(),dishPageQueryDto.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDto); //调用DTO返回VO
        return new PageResult(page.getTotal(),page.getResult());
    }

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
    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能删除 -- 是否存在启售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);

            if(dish.getStatus()== StatusConstant.ENABLE){
                //当前菜品处于启售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //当前菜品是否被套餐关联了-- 存在关联的套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds!=null && setmealIds.size()>0){
            //即判断有没有袋子 袋子里有没有装东西
            //菜品被套餐关联了 不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品数据
//        for (Long id : ids) {
//            dishMapper.deleteByID(id);
//            //删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //delete from dish where id in(?,?,?) 批量删除 根据菜品id集合批量删除菜品数据
        dishMapper.deleteByIds(ids);
        //根据菜品id集合关联删除的口味数据 delete from dish_flavor where dish_id in(?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //分两步查 1.查询菜品数据 2.查询口味数据 将查询的数据封装到VO中
        Dish dish = dishMapper.getById(id);

        List<DishFlavor> dishFlavors= dishFlavorMapper.getByDishId(id);

        //将查询的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        //此时categoryName不是必须 拷贝不过来也没关系
        dishVO.setFlavors(dishFlavors); //dishFlavors（存储口味列表的内存地址）赋值给 dishVO 的属性。
                                        //Jackson 的工具，执行 “序列化 (Serialization)” 操作。
        return dishVO;
    }

    /**
     * 根据id来修改菜品和口味数据
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表的基本信息
        dishMapper.update(dish);
        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            //此处口味需要与菜品进行关联 所以需要设置DishId
            //这批新创建的口味对象，必须全部效忠于当前的这一个菜品 ID
            dishFlavorMapper.insertBatch(flavors);
        };
    }
}
