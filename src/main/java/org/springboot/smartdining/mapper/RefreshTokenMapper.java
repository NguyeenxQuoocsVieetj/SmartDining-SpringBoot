package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.entity.RefreshToken;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    RefreshToken findById(Long id);

    RefreshToken findByTokenHash(String tokenHash);

    /** Thu hồi một token còn hiệu lực; trả 0 nếu đã thu hồi. */
    int revoke(@Param("id") Long id, @Param("revokedAt") LocalDateTime revokedAt);

    /** Thu hồi mọi token chưa thu hồi của một phiên. */
    int revokeSession(@Param("sessionId") String sessionId, @Param("revokedAt") LocalDateTime revokedAt);

    /** Thu hồi mọi phiên của một nhân viên. */
    int revokeByEmployee(@Param("employeeId") Long employeeId, @Param("revokedAt") LocalDateTime revokedAt);

    int deleteExpiredByEmployee(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now);
}
