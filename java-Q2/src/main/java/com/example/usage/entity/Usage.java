package com.example.usage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "output_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private String code;
    private Integer month;
    private Integer year;
    private Double value;
    private String unit;
}
