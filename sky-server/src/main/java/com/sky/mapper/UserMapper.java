package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
  @Select("select id, openid, name, phone, sex, id_number, avatar, create_time from user where openid = #{openid}")
  User getByOpenid(String openid);

  void reset(User user);

  @Select("select id, openid, name, phone, sex, id_number, avatar, create_time from user where id = #{userId}")
  User getById(Long userId);


  /**
   * 根据动态条件统计用户数量
   * @param map
   * @return
   */
  Integer countByMap(Map map);
}
