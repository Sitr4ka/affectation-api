package com.micraft.org.affectpro.location;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Column(nullable = false, unique = true, length = 20)
    private String locationNumber;

    @Setter
    @Column(nullable = false, length = 150)
    private String designation;

    @Setter
    @Column(nullable = false, length = 100)
    private String province;

}
