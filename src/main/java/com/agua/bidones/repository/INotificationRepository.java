package com.agua.bidones.repository;

import com.agua.bidones.model.Notification;
import com.agua.bidones.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndUser(Long id, User user);

    List<Notification> findByUserAndReadFalse(User user);

    List<Notification> findTop100ByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

    void deleteByUser(User user);

    void deleteByOrder(com.agua.bidones.model.Order order);
}
