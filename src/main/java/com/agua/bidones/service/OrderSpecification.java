package com.agua.bidones.service;

import com.agua.bidones.dto.OrderFilterDTO;
import com.agua.bidones.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> withFilters(String authIdentifier, OrderFilterDTO filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (authIdentifier != null && !authIdentifier.isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("repartidor").get("admin").get("identifier"),
                                authIdentifier
                        )
                );
            }
            // 2. FILTROS EXISTENTES
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
                                root.get("createdAt"),
                                filter.getDateFrom().atStartOfDay()
                        )
                );
            }

            if (filter.getDateTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                filter.getDateTo().atTime(23,59,59)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}