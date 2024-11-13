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
   * @param shoppingCartDTO
   */
  public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
	// 判断 当前加入购物车商品是否存在
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    shoppingCart.setUserId(BaseContext.getCurrentId());

    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
    if(list != null && !list.isEmpty()) {
      ShoppingCart cart = list.get(0);
      cart.setNumber(cart.getNumber() + 1);
      // 存在 数量 + 1 update
      shoppingCartMapper.updateShoppingCart(cart);
    } else {
      // 不存在 插入数据
      Long dishId = shoppingCartDTO.getDishId();
      Long setmealId = shoppingCartDTO.getSetmealId();
      // 判断本次添加的是菜品还是套餐
      if(dishId != null) {
        // 菜品
        Dish dish = dishMapper.getById(dishId);
        shoppingCart.setName(dish.getName());
        shoppingCart.setImage(dish.getImage());
        shoppingCart.setAmount(dish.getPrice());
	  } else {
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

  public List<ShoppingCart> showShoppingCart() {
    Long userId = BaseContext.getCurrentId();
    ShoppingCart shoppingCart = new ShoppingCart();
    shoppingCart.setUserId(userId);
    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
    return list;
  }

  public void cleanShoppingCart() {
    Long userId = BaseContext.getCurrentId();
    shoppingCartMapper.clean(userId);
  }
}
