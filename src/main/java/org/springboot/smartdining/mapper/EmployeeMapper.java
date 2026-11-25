package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.entity.Employee;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmployeeMapper {

    int insert(Employee employee);

    Employee findById(Long id);

    List<Employee> findAll();

    /** passwordHash null thì giữ mật khẩu đang có. */
    int update(Employee employee);

    int deleteById(Long id);

    /** Tìm theo username, không phân biệt hoa thường (theo bảng mã của cột). */
    Employee findByUsername(String username);

    /** Đọc và khóa nhân viên, để các thao tác trên phiên đăng nhập của họ chạy lần lượt. */
    Employee findByIdForUpdate(Long id);

    /** Nhân viên còn ACTIVE và phiên đăng nhập còn token chưa thu hồi, chưa hết hạn; không thì null. */
    Employee findActiveSession(@Param("employeeId") Long employeeId,
                               @Param("sessionId") String sessionId,
                               @Param("now") LocalDateTime now);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
