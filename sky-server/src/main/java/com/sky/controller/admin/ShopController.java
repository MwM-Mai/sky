package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {
  private static final String SHOP_STATUS = "SHOP_STATUS";

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  /**
   * 设置店铺营业状态
   * @param status
   * @return
   */
  @PutMapping("/{status}")
  @ApiOperation("设置店铺营业状态")
  public Result setStatus(@PathVariable Integer status) {
	log.info("设置店铺运营状态为: {}", status);
	ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
	valueOperations.set(SHOP_STATUS, status);

	return Result.success();
  }

  @GetMapping("/status")
  @ApiOperation("获取店铺营业状态")
  public Result<Integer> getStatus() {
	ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
	Integer shopStatus = (Integer) valueOperations.get(SHOP_STATUS);

	return Result.success(shopStatus);
  }
}
