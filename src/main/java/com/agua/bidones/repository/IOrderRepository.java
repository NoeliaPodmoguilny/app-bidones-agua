package com.agua.bidones.repository;

import com.agua.bidones.enums.Status;
import com.agua.bidones.model.Order;
import com.agua.bidones.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"cliente", "repartidor"})
    List<Order> findByClienteOrderByCreatedAtDesc(User cliente);

    @EntityGraph(attributePaths = {"cliente", "repartidor"})
    List<Order> findByRepartidorOrderByCreatedAtDesc(User repartidor);

    @EntityGraph(attributePaths = {"cliente", "repartidor"})
    @Query("select o from Order o where o.cliente = :cliente and (o.createdAt >= :since or o.status in :open) order by o.createdAt desc")
    List<Order> findRecentByCliente(@Param("cliente") User cliente,
                                    @Param("since") Instant since,
                                    @Param("open") Collection<Status> open);

    @EntityGraph(attributePaths = {"cliente", "repartidor"})
    @Query("select o from Order o where o.repartidor = :repartidor and (o.createdAt >= :since or o.status in :open) order by o.createdAt desc")
    List<Order> findRecentByRepartidor(@Param("repartidor") User repartidor,
                                       @Param("since") Instant since,
                                       @Param("open") Collection<Status> open);

    @EntityGraph(attributePaths = {"cliente"})
    List<Order> findByRepartidorAndStatusIn(User repartidor, Collection<Status> statuses);

    @EntityGraph(attributePaths = {"cliente"})
    List<Order> findByClienteAndStatusIn(User cliente, Collection<Status> statuses);

    @Query("select o.status, count(o) from Order o where o.repartidor.admin.identifier = :admin and o.createdAt >= :from and o.createdAt < :to group by o.status")
    List<Object[]> countByStatusBetween(@Param("admin") String admin,
                                        @Param("from") Instant from,
                                        @Param("to") Instant to);

    @Query("select count(o) from Order o where o.repartidor.admin.identifier = :admin and o.createdAt < :before and o.status in :open")
    long countOpenBefore(@Param("admin") String admin,
                         @Param("before") Instant before,
                         @Param("open") Collection<Status> open);
}
