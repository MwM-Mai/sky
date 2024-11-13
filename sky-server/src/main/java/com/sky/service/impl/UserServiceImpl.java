package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

  // 微信服务接口地址
  private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

  @Autowired
  private WeChatProperties weChatProperties;
  @Autowired
  private UserMapper userMapper;

  public User wxLogin(UserLoginDTO userLoginDTO) {
	String openid = getOpenid(userLoginDTO.getCode());
	// 判断openid是否为空,为空,异常
	if (openid == null) {
		throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
	}

	// 判断微信用户是否为新用户
	User user = userMapper.getByOpenid(openid);
	if(user == null) {
	  user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
	  userMapper.reset(user);
	}

	return user;
  }

  private String getOpenid(String code) {
	// 调用微信登录接口, 获取微信用户的openid
	Map<String, String> paramMap = new HashMap<>();
	paramMap.put("appid", weChatProperties.getAppid());
	paramMap.put("secret", weChatProperties.getSecret());
	paramMap.put("js_code", code);
	paramMap.put("grant_type", "authorization_code");
	String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);
	JSONObject jsonObject = JSON.parseObject(json);
	String openid = jsonObject.getString("openid");

	return openid;
  }
}
