package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

  /**
   * 批量插入口味数据
   * @param flavors
   */
  void insertBatch(List<DishFlavor> flavors);

  /**
   * 根据菜品id删除口味
   * @param dishId
   */
  @Delete("delete from dish_flavor where dish_id = #{dishId}")
  void deleteByDishId(Long dishId);

  void deleteByDishIds(List<Long> dishIds);

  /**
   * 根据菜品id查询口味数据
   * @param id
   * @return
   */
  @Select("select id, dish_id, name, value from dish_flavor where dish_id = #{dishId}")
  List<DishFlavor> getByDishId(Long dishId);
}
