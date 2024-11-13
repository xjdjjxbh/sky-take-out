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

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入购物车中的物品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        //前端传过来了口味数据，在这里数据拷贝的时候就已经把口味数据传递给了购物车对象了
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        /*
        查询出来只可能有两种结果，一种是查不到数据，另一种是查到一条数据，因为我们确定了userId和dishId,这样就唯一确定了一条数据
         */
        // 存在的话，加一就可以了
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);

            //更新购物车里面商品的数量
            shoppingCartMapper.updateNumberById(cart);
        } else {
            //如果不存在，再执行插入

            //判断当前添加到购物车里面的到底是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //本次添加到购物车的是菜品
                //查询菜品表，得到当前添加菜品的基本信息
                Dish dish = dishMapper.selectById(dishId);
//                BeanUtils.copyProperties(dish, shoppingCart);
//                这里不要直接用属性拷贝，因为shoppingCart里面的id会被dish给强行赋值，
//                而shopping里面的id本应该是插入数据库之后让它自增的
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            } else {
                //本次添加到购物车里的是套餐
                //查询套餐表，得到当前添加套餐的基本信息
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
//                BeanUtils.copyProperties(setmeal, shoppingCart);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /**
     * 查看购物车数据
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());   //获取到当前用户的userId,然后作为查询条件传进去
        return shoppingCartMapper.list(shoppingCart);
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
     * 删除购物车中的一件商品
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //查询得到这件商品之前的数量，然后减一
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        Integer number = shoppingCarts.get(0).getNumber();

        //如果查询得到这件商品的数量只剩一件了，那么就把这件商品的信息直接从数据库里面删掉
        if (number==1){
            shoppingCartMapper.deleteById(shoppingCart);
            return;
        }

        shoppingCart.setNumber(number - 1);
        shoppingCartMapper.updateNumberById(shoppingCart);
    }
}
