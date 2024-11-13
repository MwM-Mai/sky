package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
  /**
   * 根据菜品id查询对应的套餐id
   * @param ids
   * @return
   */
	public List<Long> getSetmealDishByDishId(List<Long> ids);
}
