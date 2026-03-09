package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入购物车的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //此时返回结果只有一条 只是为了复用
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果已经存在 只需要将数量++
        if(list != null && list.size() > 0){
            ShoppingCart cart= list.get(0);
            cart.setNumber(cart.getNumber()+1); //数量++
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //如果不存在 需要插入一条购物车数据
            Long dishId = shoppingCartDTO.getDishId();

            //先判断本次添加到购物车的时菜品还是套餐
            if(dishId != null){
                //添加的为菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());


            }else {
                //否则本次添加的数据为套餐
               Long setmealId = shoppingCartDTO.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
               shoppingCart.setName(setmeal.getName());
               shoppingCart.setImage(setmeal.getImage());
               shoppingCart.setAmount(setmeal.getPrice());


            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }



    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取到当前用户的id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
         return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 减
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //，DTO（Data Transfer Object） 是用来“传话”的，而 Entity（实体类） 是用来“干活”的。
        // 1. 必须先判断 list 是否为空，确保能拿到数据
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0); // 拿到当前购物车里的这行记录

            // 2. 将后续逻辑移入 if 块内部，或者提前声明变量
            if (cart.getNumber() == 1) {
                // 如果数量只有 1 个，直接删掉这条记录
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                // 如果数量大于 1，减 1 并更新数据库
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }
}
