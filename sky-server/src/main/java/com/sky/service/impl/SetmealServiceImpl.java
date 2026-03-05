package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
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
}
