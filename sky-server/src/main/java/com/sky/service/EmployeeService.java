package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

  /**
   * 员工登录
   *
   * @param employeeLoginDTO
   * @return
   */
  Employee login(EmployeeLoginDTO employeeLoginDTO);

  /**
   * 新增员工
   *
   * @param employeeDTO
   */
  void save(EmployeeDTO employeeDTO);

  /**
   * 员工分页查询
   *
   * @param employeePageQueryDTO
   */
  PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

  /**
   * 启用禁用员工
   * @param Integer status
   * @param Long id
   * @return
   */
  void startOrStop(Integer status, Long id);

  /**
   * 根据id查询员工
   * @param Long id
   */
  Employee getById(Long id);

  /**
   * 修改员工数据
   * @param employeeDTO
   */
  void update(EmployeeDTO employeeDTO);
}