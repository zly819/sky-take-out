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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZLY
 * @version 1.0
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param dto
     */
    @Override
    public void addCart(ShoppingCartDTO dto) {
        //创建ShoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        //拷贝属性
        BeanUtils.copyProperties(dto, shoppingCart);

        //1.判断该商品是否已存在购物车-- 条件：dishId\dishFlavor
        //只查当前用户自己的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart cart= shoppingCartMapper.selectBy(shoppingCart);
        if (cart == null) {//代表购物车没有该商品数据
            //2.补充缺失的属性值
            //判断是新增套餐还是新增菜品
            if (dto.getDishId() != null) {   //代表新增的是菜品
                //根据菜品的id查询菜品表，获取菜品相关信息
                Dish dish = dishMapper.selectById(dto.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else {         //代表新增的是套餐
                Setmeal setmeal = setmealMapper.getById(dto.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            //3.将商品数据存入到shopping_cart表中
            shoppingCartMapper.insert(shoppingCart);
        }else {//代表购物车有该商品数据

            //4.将原来的购物数量+1，调用mapper更新方法
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        }

        //最终目的：将用户添加的商品，存入到购物车表中shopping_cart表
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        return shoppingCartMapper.list(BaseContext.getCurrentId());
    }
}
