package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Employee;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    int insert(Employee employee);

    Employee findById(Long id);

    List<Employee> findAll();

    /** passwordHash null thì giữ mật khẩu đang có. */
    int update(Employee employee);

    int deleteById(Long id);

    /** Id tài khoản ADMIN, null nếu chưa có. */
    Long findAdminId();
}
