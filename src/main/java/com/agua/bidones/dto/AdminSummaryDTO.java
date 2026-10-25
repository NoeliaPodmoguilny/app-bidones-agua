package com.agua.bidones.dto;

import java.util.Map;

/** Resumen liviano del panel admin: cantidades del día por estado y pedidos abiertos de días anteriores. */
public record AdminSummaryDTO(
        Map<String, Long> today,
        long todayTotal,
        long carryOver
) {
}
