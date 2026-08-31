package com.micraft.org.affectpro.api;

import com.micraft.org.affectpro.assignment.Assignment;
import com.micraft.org.affectpro.assignment.AssignmentRepository;
import com.micraft.org.affectpro.employee.Employee;
import com.micraft.org.affectpro.location.Location;

public final class ApiMapper {
    private ApiMapper() { }
    public static ApiDtos.LocationBrief locationBrief(Location location) {
        return location == null ? null : new ApiDtos.LocationBrief(location.getId(), location.getDesignation(), location.getProvince());
    }
    public static ApiDtos.LocationView location(Location location, long employeeCount) {
        return new ApiDtos.LocationView(location.getId(), location.getLocationNumber(), location.getDesignation(), location.getProvince(), employeeCount);
    }
    public static ApiDtos.EmployeeView employee(Employee employee, AssignmentRepository assignments) {
        return new ApiDtos.EmployeeView(employee.getId(), employee.getEmployeeNumber(), employee.getCivility(), employee.getLastName(),
                employee.getFirstName(), employee.getEmail(), employee.getPosition(), locationBrief(employee.getCurrentLocation()), assignments.countByEmployeeId(employee.getId()));
    }
    public static ApiDtos.AssignmentView assignment(Assignment assignment, AssignmentRepository assignments) {
        return new ApiDtos.AssignmentView(assignment.getId(), assignment.getAssignmentNumber(), employee(assignment.getEmployee(), assignments),
                locationBrief(assignment.getPreviousLocation()), locationBrief(assignment.getNewLocation()), assignment.getEffectiveDate(), assignment.getReason());
    }
}
