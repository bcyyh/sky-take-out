package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
//实现类类注入@Service 才可以被自动装配 原理为反射
public class SetmealServiceImpl implements SetmealService {
    //此处可以自动装配 是因为在Mapper层加了@Mapper
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional  // 开启事务 新增操作需要原子性
    public void save(SetmealDTO setmealDTO) {
        //新建一个对象 然后用DTO进行赋值
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //此时实体类被前端DTO赋值了 又因为前端不给ID 故用插入后主键回填的方法获取ID
        //ID 是数据库自增生成
        log.info("新增套餐：{}", setmeal);
        setmealMapper.insert(setmeal); //套餐表进行插入
        Long setmealId = setmeal.getId();
        // 3. 处理关联菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //setmealDishes 这个套餐里包含的所有菜品的详细清单
        if (setmealDishes != null && setmealDishes.size() > 0) {
            // 关键一步：给每一个关联菜品设置它所属的套餐ID
            //此时套餐ID是固定的 遍历每一个商品捆绑套餐
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            //此处为清单里的所有菜进行同样的套餐ID赋值

            // 4. 批量插入到 setmeal_dish 关系表
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

     /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //用分页插件进行分页 仅需传入页码和每页数据条数
        //1. 设置分页口令（存入 ThreadLocal）这个参数只对紧随其后的第一条 MyBatis 查询语句有效
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        // 2. 紧接着的查询会被拦截，并从 ThreadLocal 取出口令
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
        //从 Page 对象这个“大包裹”里取出那行 private long total 的值。
        //把 Page 对象本身作为一个 List 传给 PageResult 的 records 字段。
    }

    /**
     * 批量删除套餐
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //判断当前套餐是否可以删除 是否有起售的套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //先删除关系表 再删除套餐 如果先删除主表 关系表就没有父亲了
        setmealDishMapper.deleteBySetmealIds(ids);  //套餐菜品关系表
        setmealMapper.deleteByIds(ids);   //套餐表

    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {

        //分两步走

        //此处getById(id)是将套餐表所有数据查询出来 赋值给套餐实体类
        Setmeal setmeal = setmealMapper.getById(id);
        //再把套餐关系表中的数据赋值给套餐菜品集合
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealIds(id);

        //此时创建VO对象 将获得到的两个对象拷贝给它
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;  //因为要返回给前端展示 所以用VO
    }

    /**
     * 进行套餐的修改
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //DTO 是给前端看的“门面”，而 Entity 是数据库认识的“身份证” 修改数据库故用实体类
        log.info("修改套餐：{}", setmeal);
        setmealMapper.update(setmeal);
        //覆盖操作 先删除套餐和菜品的关联数据 再重新进行套餐和菜品关联数据的赋值
        setmealDishMapper.deleteBySetmealIds(Arrays.asList(setmealDTO.getId()));
        //先删除原先的所有数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //从前端传来的“大包裹”（DTO）中，把用户最新选定的菜品数组提取出来。
        if (setmealDishes != null && setmealDishes.size() > 0) {
            // 关键一步：给每一个关联菜品设置它所属的套餐ID
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());  //将前端传来的套餐ID赋给菜品
            });
            // 4. 批量插入到 setmeal_dish 关系表
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐的起售和停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                for (Dish dish : dishList) {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
