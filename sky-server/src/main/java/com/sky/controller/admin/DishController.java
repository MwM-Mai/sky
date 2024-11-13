package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品管理相关接口")
public class DishController {

  @Autowired
  private DishService dishService;

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  /**
   * 新增菜品
   * @param dishDTO
   * @return
   */
  @PostMapping()
  @ApiOperation("新增菜品")
  public Result save (@RequestBody DishDTO dishDTO) {
	log.info("新增菜品: {}", dishDTO);
	dishService.insetWithFlavor(dishDTO);

	// 清理缓存数据
	String key = "dish_" + dishDTO.getCategoryId();
	redisTemplate.delete(key);
	return Result.success();
  }

  /**
   *  菜品分页查询
   * @param dishPageQueryDTO
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("菜品分页查询")
  public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
	log.info("菜品分页查询: {}", dishPageQueryDTO);
	PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

	return Result.success(pageResult);
  }

  /**
   * 分局ids删除菜品
   * @return
   */
  @DeleteMapping()
  @ApiOperation("菜品批量删除")
  public Result delete(@RequestParam List<Long> ids) {
	log.info("菜品批量删除: {}", ids);

	dishService.deleteButch(ids);

	// 将所有以dish_开头的缓存清理
	Set<Object> keys = redisTemplate.keys("dish_*");
	assert keys != null;
	redisTemplate.delete(keys);
	return Result.success();
  }

  /**
   * 根据id查询菜品
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  @ApiOperation("根据id查询菜品")
  public Result<DishVO> getById(@PathVariable Long id) {
	log.info("根据id查询菜品, {}", id);

	DishVO dishVO = dishService.getByIdWithFlavor(id);
	return Result.success(dishVO);
  }

  /**
   * 根据id修改菜品
   * @param dishDTO
   * @return
   */
  @PutMapping
  @ApiOperation("根据id修改菜品")
  public Result update(@RequestBody DishDTO dishDTO) {
	log.info("根据id修改菜品, {}", dishDTO);
	dishService.updateWithFlavor(dishDTO);

	// 将所有以dish_开头的缓存清理
	Set<Object> keys = redisTemplate.keys("dish_*");
	assert keys != null;
	redisTemplate.delete(keys);
	return  Result.success();
  }
}
