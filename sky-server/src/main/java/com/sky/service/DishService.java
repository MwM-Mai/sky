package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
  /**
   * 新增菜品和口味
   * @param dishDTO
   * @return
   */
  void insetWithFlavor(DishDTO dishDTO);

  /**
   * 菜品分页查询
   * @param dishPageQueryDTO
   * @return
   */
  PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

  /**
   * 菜品批量删除
   * @param ids
   */
  void deleteButch(List<Long> ids);

  /**
   * 根据id查询菜品和口味数据
   * @param id
   * @return
   */
  DishVO getByIdWithFlavor(Long id);

  /**
   * 根据id查询菜品和口味数据
   * @param dishDTO
   * @return
   */
  void updateWithFlavor(DishDTO dishDTO);

  /**
   * 条件查询菜品和口味
   * @param dish
   * @return
   */
  List<DishVO> listWithFlavor(Dish dish);
}
