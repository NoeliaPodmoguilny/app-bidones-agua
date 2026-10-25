package com.agua.bidones.service;

import com.agua.bidones.dto.OrderFilterDTO;
import com.agua.bidones.model.Order;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    public static Specification<Order> withFilters(String authIdentifier, OrderFilterDTO filter) {

        return (root, query, cb) -> {

            Class<?> resultType = query.getResultType();
            if (resultType != Long.class && resultType != long.class) {
                root.fetch("cliente", JoinType.LEFT);
                root.fetch("repartidor", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (authIdentifier != null && !authIdentifier.isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("repartidor").get("admin").get("identifier"),
                                authIdentifier
                        )
                );
            }
            if (filter.getStatus() != null) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            if (filter.getClienteName() != null &&
                    !filter.getClienteName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("cliente").get("name")),
                                "%" + filter.getClienteName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getRepartidorName() != null &&
                    !filter.getRepartidorName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("repartidor").get("name")),
                                "%" + filter.getRepartidorName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getDateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.<java.time.Instant>get("createdAt"),
                                filter.getDateFrom().atStartOfDay(ZONE).toInstant()
                        )
                );
            }

            if (filter.getDateTo() != null) {
                predicates.add(
                        cb.lessThan(
                                root.<java.time.Instant>get("createdAt"),
                                filter.getDateTo().plusDays(1).atStartOfDay(ZONE).toInstant()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}