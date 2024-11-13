package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sky.utils.WeChatPayUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private OrderDetailMapper orderDetailMapper;
  @Autowired
  private AddressBookMapper addressBookMapper;
  @Autowired
  private ShoppingCartMapper shoppingCartMapper;
  @Autowired
  private UserMapper userMapper;
  @Autowired
  private WeChatPayUtil weChatPayUtil;
  @Autowired
  private WebSocketServer webSocketServer;


  /**
   * 用户下单
   *
   * @param ordersSubmitDTO
   * @return
   */
  @Transactional
  public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
	// 业务异常 // 收货地址为空 购物车为空
	AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
	if (address == null) {
	  throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
	}
	Long userId = BaseContext.getCurrentId();

	ShoppingCart shoppingCart = new ShoppingCart();
	shoppingCart.setUserId(userId);
	List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
	if (list == null || list.isEmpty()) {
	  throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
	}
	// 在订单表插入一条数据
	Orders orders = new Orders();
	BeanUtils.copyProperties(ordersSubmitDTO, orders);
	orders.setUserId(userId);
	orders.setOrderTime(LocalDateTime.now());
	orders.setPayStatus(Orders.UN_PAID);
	orders.setStatus(Orders.PENDING_PAYMENT);
	orders.setNumber(String.valueOf(System.currentTimeMillis()));
	orders.setPhone(address.getPhone());
	orders.setAddress(orders.getAddress());
	orders.setConsignee(orders.getConsignee());

	orderMapper.insert(orders);

	List<OrderDetail> orderDetailList = new ArrayList<>();
	// 订单明细表插入n条数据
	for (ShoppingCart cart : list) {
	  OrderDetail orderDetail = new OrderDetail();
	  BeanUtils.copyProperties(cart, orderDetail);
	  orderDetail.setOrderId(orders.getId());
	  orderDetailList.add(orderDetail);
	}
	orderDetailMapper.insertBatch(orderDetailList);
	// 删除购物车数据
	shoppingCartMapper.clean(userId);
	OrderSubmitVO orderSubmitVo = OrderSubmitVO.builder()
			.id(orders.getId())
			.orderTime(orders.getOrderTime())
			.orderNumber(orders.getNumber())
			.orderAmount(orders.getAmount())
			.build();

	// 封装VO返回结果

	return orderSubmitVo;
  }

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
	// 当前登录用户id
	Long userId = BaseContext.getCurrentId();
	User user = userMapper.getById(userId);

	//调用微信支付接口，生成预支付交易单
	JSONObject jsonObject = weChatPayUtil.pay(
			ordersPaymentDTO.getOrderNumber(), //商户订单号
			new BigDecimal("0.01"), //支付金额，单位 元
			"苍穹外卖订单", //商品描述
			user.getOpenid() //微信用户的openid
	);

	if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
	  throw new OrderBusinessException("该订单已支付");
	}

	OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
	vo.setPackageStr(jsonObject.getString("package"));

	return vo;
  }

  /**
   * 支付成功，修改订单状态
   *
   * @param outTradeNo
   */
  public void paySuccess(String outTradeNo) {

	// 根据订单号查询订单
	Orders ordersDB = orderMapper.getByNumber(outTradeNo);

	// 根据订单id更新订单的状态、支付方式、支付状态、结账时间
	Orders orders = Orders.builder()
			.id(ordersDB.getId())
			.status(Orders.TO_BE_CONFIRMED)
			.payStatus(Orders.PAID)
			.checkoutTime(LocalDateTime.now())
			.build();

	orderMapper.update(orders);

	// 通过websocket向客户端浏览器推送消息
	HashMap<String, Object> map = new HashMap<>();
	map.put("type", 1); // 1: 来单提醒 2: 用户催单
	map.put("orderId", orders.getId());
	map.put("content", "订单号: " + outTradeNo);
	String jsonString = JSON.toJSONString(map);
	webSocketServer.sendToAllClient(jsonString);
  }

  public void reminder(Long id) {
	Orders orders = orderMapper.getById(id);
	if(orders == null) {
	  throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
	}

	HashMap<String, Object> map = new HashMap<>();
	map.put("type", 2); // 1: 来单提醒 2: 用户催单
	map.put("orderId", id);
	map.put("content", "订单号: " + orders.getNumber());
	String jsonString = JSON.toJSONString(map);
	webSocketServer.sendToAllClient(jsonString);
  }
}
