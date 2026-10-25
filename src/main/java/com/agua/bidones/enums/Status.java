package com.agua.bidones.enums;

public enum Status {

    PENDIENTE,
    EN_CAMINO,
    ENTREGADO,
    RECHAZADO;

    public static Status getPendiente(){return PENDIENTE;}
    public static Status getEnCamino(){return EN_CAMINO;}
    public static Status getEntregado(){return ENTREGADO;}
    public static Status getRechazado(){return RECHAZADO;}
}
