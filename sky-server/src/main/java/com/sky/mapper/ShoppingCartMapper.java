package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);


    /**
     * 根据id更新商品数量购物车
     *
     * @param shoppingCart
     */
//    @Update("update shopping_cart set number = #{number} where user_id = #{userId} and ")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) VALUES" +
            "(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);


    /**
     * 清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 删除购物车中的一件商品
     * @param shoppingCart
     */
//    void subShoppingCart(ShoppingCart shoppingCart);

    /**
     * 清理商品件数为0的购物车项目
     * @param shoppingCart
     */
    void deleteById(ShoppingCart shoppingCart);
}
