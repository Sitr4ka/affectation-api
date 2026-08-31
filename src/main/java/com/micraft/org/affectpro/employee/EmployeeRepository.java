package com.micraft.org.affectpro.employee;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
}
