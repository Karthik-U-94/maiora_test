package com.example.usage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutputDto {
    private String Location;
    private String Code;
    private int Month;
    private int Year;
    private Double Value;
    private String Unit;
}
