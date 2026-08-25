package com.agua.bidones.repository;

import com.agua.bidones.enums.Status;
import com.agua.bidones.model.Order;
import com.agua.bidones.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByClienteOrderByCreatedAtDesc(User cliente);

    List<Order> findByRepartidorOrderByCreatedAtDesc(User repartidor);

}
