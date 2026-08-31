package com.micraft.org.affectpro.assignment;

import java.time.LocalDate;
import java.util.UUID;

import com.micraft.org.affectpro.employee.Employee;
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
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue
    private UUID id;
    @Setter
    @Column(nullable = false, unique = true, length = 20)
    private String assignmentNumber;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_location_id")
    private Location previousLocation;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "new_location_id", nullable = false)
    private Location newLocation;
    @Setter
    @Column(nullable = false)
    private LocalDate effectiveDate;
    @Setter
    @Column(nullable = false, length = 500)
    private String reason;

}
