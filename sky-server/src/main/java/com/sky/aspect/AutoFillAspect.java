package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面, 实现公共字段填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

  /**
   * 切入点
   */
  @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
  public void autoFillPointCut(){}

  /**
   * 前置通知,在通知中进行公共字段的赋值
   * @param joinPoint
   * @return
   * @throws Throwable
   */
  @Before("autoFillPointCut()")
  public void autoFillAdvice(JoinPoint joinPoint) throws Throwable {
    log.info("开始对公共字段进行自动填充...");

    // 1. 获取当前被拦截方法上的数据库操作类型
    MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 方法前面对象
    AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获取方法上的注解对象
    OperationType operationType = autoFill.value(); // 获取数据库操作类型

    // 2. 获取当前被拦截方法参数的实体对象
    Object[] args = joinPoint.getArgs();
    if(args == null || args.length == 0) {
      return;
    }
    Object entity = args[0]; // mapper中第一个参数默认是 实体类

    // 3. 准备赋值数据
    LocalDateTime now = LocalDateTime.now();
    Long currentId = BaseContext.getCurrentId();

    // 4. 根据当前不同的操作类型, 为对应的数据通过反射进行赋值
    if(operationType == OperationType.INSERT) {
      // 为4个公共字段进行赋值

      // 通过获取实体类的运行时类 getDeclaredConstructor(方法名, 方法的参数类型) 获取已声明的方法
      Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
      Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
      Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
      Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
      setCreateTime.invoke(entity, now);
      setUpdateTime.invoke(entity, now);
      setCreateUser.invoke(entity, currentId);
      setUpdateUser.invoke(entity, currentId);
    } else if(operationType == OperationType.UPDATE) {
      // 为2个公共字段进行赋值
      Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
      Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
      setUpdateUser.invoke(entity, currentId);
      setUpdateTime.invoke(entity, now);
    }
  }
}
