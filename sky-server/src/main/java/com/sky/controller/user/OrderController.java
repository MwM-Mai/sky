package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class OrderController {

  @Autowired
  private OrderService orderService;

  /**
   * 用户下单
   *
   * @param ordersSubmitDTO
   * @return
   */
  @PostMapping("/submit")
  @ApiOperation("用户下单")
  public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
	log.info("用户下单: {}", ordersSubmitDTO);
	OrderSubmitVO order = orderService.submitOrder(ordersSubmitDTO);
	return Result.success(order);
  }

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  @PutMapping("/payment")
  @ApiOperation("订单支付")
  public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
	log.info("订单支付：{}", ordersPaymentDTO);
	OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
	log.info("生成预支付交易单：{}", orderPaymentVO);
	return Result.success(orderPaymentVO);
  }

  /**
   * 客户催单
   * @return
   */
  	@GetMapping("/reminder/{id}")
	public Result reminder(@PathVariable Long id) {
	  orderService.reminder(id);
		return Result.success();
	}
}
