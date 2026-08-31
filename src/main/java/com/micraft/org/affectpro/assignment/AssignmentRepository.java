package com.micraft.org.affectpro.assignment;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByEmployeeIdOrderByEffectiveDateDesc(UUID employeeId);
    List<Assignment> findByEffectiveDateBetween(LocalDate from, LocalDate to);
    long countByEmployeeId(UUID employeeId);
    boolean existsByEmployeeId(UUID employeeId);
    boolean existsByPreviousLocationIdOrNewLocationId(UUID previousLocationId, UUID newLocationId);
    Optional<Assignment> findByAssignmentNumber(String assignmentNumber);
}
