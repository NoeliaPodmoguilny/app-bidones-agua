package com.agua.bidones.dto;

/** Cuántos clientes y pedidos abiertos pasaron al nuevo repartidor. */
public record ReassignResultDTO(int clientsMoved, int ordersMoved) {
}
