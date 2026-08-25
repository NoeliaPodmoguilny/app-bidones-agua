package com.agua.bidones.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"user", "message", "jwt", "status"})
public record AuthResponseDTO(UserResponseDTO user,String message, String jwt, boolean status) {

}
