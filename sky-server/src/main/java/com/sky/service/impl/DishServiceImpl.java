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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private DishFlavorMapper dishFlavorMapper;
  @Autowired
  private SetmealDishMapper setmealDishMapper;

  /**
   * 新增菜品和口味
   * @param dishDTO
   * @return
   */
  @Transactional // 事务注解
  public void insetWithFlavor(DishDTO dishDTO) {
    // 菜品表插入数据
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.insert(dish);

    // 获取insert语句生成的主键值 在xml文件中配置属性会自动返回
    Long id = dish.getId();

    List<DishFlavor> flavors = dishDTO.getFlavors();
    if(flavors != null && !flavors.isEmpty()) {
      flavors.forEach(flavor -> {
        flavor.setDishId(id);
      });
      // 插入口味数据
      dishFlavorMapper.insertBatch(flavors);
    }

  }

  /**
   * 菜品分页查询
   * @param dishPageQueryDTO
   * @return
   */
  public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
    PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
    Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
    return new PageResult(page.getTotal(), page.getResult());
  }

  /**
   * 菜品批量删除
   * @param ids
   */
  @Transactional
  public void deleteButch(List<Long> ids) {
    // 菜品是否能够删除--是否起售
    for (Long id : ids) {
      Dish dish = dishMapper.getById(id);
      if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
      }
    }
    // 是否被套餐关联
    List<Long> setmealIds = setmealDishMapper.getSetmealDishByDishId(ids);
    if(!setmealIds.isEmpty()) {
      throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
    }
    // 删除菜品和口味数据
    //for (Long id : ids) {
    //  // 删除菜品数据
    //  dishMapper.delete(id);
    //  // 删除口味数据
    //  dishFlavorMapper.deleteByDishId(id);
    //}

    // 优化删除代码,减少sql操作 进行批量删除
    dishMapper.deleteByIds(ids);
    dishFlavorMapper.deleteByDishIds(ids);
  }

  /**
   * 根据id查询菜品和口味数据
   * @param id
   * @return
   */
  public DishVO getByIdWithFlavor(Long id) {
    Dish dish = dishMapper.getById(id);
    List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
    DishVO dishVO = new DishVO();
    BeanUtils.copyProperties(dish, dishVO);
    dishVO.setFlavors(dishFlavors);

    return dishVO;
  }

  /**
   * 根据id修改菜品和口味数据
   * @param dishDTO
   */
  @Transactional
  public void updateWithFlavor(DishDTO dishDTO) {
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.update(dish);
    Long id = dish.getId();

    // 删除原有的口味数据, 再添加新的数据
    dishFlavorMapper.deleteByDishId(id);

    if(!dishDTO.getFlavors().isEmpty()) {
      List<DishFlavor> flavors = dishDTO.getFlavors();
      for (DishFlavor flavor : flavors) {
        flavor.setDishId(id);
      }
      dishFlavorMapper.insertBatch(flavors);
    }
  }

  /**
   * 条件查询菜品和口味
   * @param dish
   * @return
   */
  public List<DishVO> listWithFlavor(Dish dish) {
    List<Dish> dishList = dishMapper.list(dish);

    List<DishVO> dishVOList = new ArrayList<>();

    for (Dish d : dishList) {
      DishVO dishVO = new DishVO();
      BeanUtils.copyProperties(d,dishVO);

      //根据菜品id查询对应的口味
      List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

      dishVO.setFlavors(flavors);
      dishVOList.add(dishVO);
    }

    return dishVOList;
  }

}
