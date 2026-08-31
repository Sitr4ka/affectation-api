package com.micraft.org.affectpro.employee;

import java.util.UUID;

import com.micraft.org.affectpro.location.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue
    private UUID id;
    @Setter
    @Column(nullable = false, unique = true, length = 20)
    private String employeeNumber;
    @Setter
    @Column(nullable = false, length = 20)
    private String civility;
    @Setter
    @Column(nullable = false, length = 100)
    private String lastName;
    @Setter
    @Column(nullable = false, length = 100)
    private String firstName;
    @Setter
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Setter
    @Column(nullable = false, length = 120)
    private String position;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

}
