package com.agua.bidones.enums;

public enum Status {

    PENDIENTE,
    ENTREGADO,
    RECHAZADO;

    public static Status getPendiente(){return PENDIENTE;}
    public static Status getEntregado(){return ENTREGADO;}
    public static Status getRechazado(){return RECHAZADO;}
}
