package com.agua.bidones.enums;

public enum UserType {

    CLIENTE,
    REPARTIDOR,
    ADMIN;


    public static UserType getCliente(){ return CLIENTE;}
    public static UserType getRepartidor(){ return REPARTIDOR;}
    public static UserType getAdmin(){ return ADMIN;}
}
