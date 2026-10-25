package com.agua.bidones.repository;

import com.agua.bidones.enums.UserType;
import com.agua.bidones.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.admin.identifier = :adminIdentifier AND (u.active IS NULL OR u.active = true)")
    List<User> findRepartidoresByAdminIdentifier(@Param("userType") UserType userType, @Param("adminIdentifier") String adminIdentifier);

    @Query("SELECT c FROM User c WHERE c.userType = :userType AND c.repartidor.admin.identifier = :adminIdentifier")
    List<User> findClientesByAdminIdentifier(@Param("userType") UserType userType, @Param("adminIdentifier") String adminIdentifier);

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.admin.identifier = :adminIdentifier AND u.active = false")
    List<User> findInactiveRepartidoresByAdminIdentifier(@Param("userType") UserType userType, @Param("adminIdentifier") String adminIdentifier);

    User findUserByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    List<User> findByPushToken(String pushToken);

    List<User> findByRepartidorAndUserTypeOrderByNameAsc(User repartidor, UserType userType);
}
