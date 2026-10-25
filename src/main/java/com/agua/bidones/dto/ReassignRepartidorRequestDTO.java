package com.agua.bidones.dto;

/** Repartidor que recibe los clientes (y pedidos abiertos). Puede faltar si no hay nada que mover. */
public record ReassignRepartidorRequestDTO(Long newRepartidorId) {
}
