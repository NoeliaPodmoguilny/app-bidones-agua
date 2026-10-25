package com.agua.bidones.enums;

public enum NotificationType {

    ORDEN_PENDIENTE,
    ORDEN_EN_CAMINO,
    ORDEN_ENTREGADA,
    ORDEN_RECHAZADA;

    public static NotificationType getOrdenPendiente(){return ORDEN_PENDIENTE;};
    public static NotificationType getOrdenEnCamino(){return ORDEN_EN_CAMINO;};
    public static NotificationType getOrdenEntregada(){return ORDEN_ENTREGADA;};
    public static NotificationType getOrdenRechazada(){return ORDEN_RECHAZADA;};
}
