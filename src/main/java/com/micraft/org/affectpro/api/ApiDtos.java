package com.micraft.org.affectpro.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ApiDtos {
    private ApiDtos() { }
    public record LocationRequest(@NotBlank String designation, @NotBlank String province) { }
    public record LocationView(UUID id, String locationNumber, String designation, String province, long employeeCount) { }
    public record EmployeeRequest(@NotBlank String civility, @NotBlank String lastName, @NotBlank String firstName, @NotBlank @Email String email, @NotBlank String position, UUID currentLocationId) { }
    public record LocationBrief(UUID id, String designation, String province) { }
    public record EmployeeView(UUID id, String employeeNumber, String civility, String lastName, String firstName, String email, String position, LocationBrief currentLocation, long assignmentCount) { }
    public record AssignmentRequest(@NotNull UUID employeeId, @NotNull UUID newLocationId, @NotNull LocalDate effectiveDate, @NotBlank @Size(max = 500) String reason) { }
    public record AssignmentView(UUID id, String assignmentNumber, EmployeeView employee, LocationBrief previousLocation, LocationBrief newLocation, LocalDate effectiveDate, String reason) { }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
    public record DashboardSummary(long employeeCount, long locationCount, long assignmentsThisMonth, long unassignedEmployeeCount) { }
    public record MonthCount(String month, long assignments) { }
    public record ProvinceCount(String province, long employees, double percentage) { }
}
