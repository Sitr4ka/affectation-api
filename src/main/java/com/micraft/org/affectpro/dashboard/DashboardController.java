package com.micraft.org.affectpro.dashboard;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.micraft.org.affectpro.api.ApiDtos;
import com.micraft.org.affectpro.assignment.AssignmentRepository;
import com.micraft.org.affectpro.employee.EmployeeRepository;
import com.micraft.org.affectpro.location.LocationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final EmployeeRepository employees;
    private final LocationRepository locations;
    private final AssignmentRepository assignments;

    public DashboardController(EmployeeRepository employees, LocationRepository locations, AssignmentRepository assignments) {
        this.employees = employees;
        this.locations = locations;
        this.assignments = assignments;
    }

    /*Test CICD */

    @GetMapping("/summary")
    public ApiDtos.DashboardSummary summary() {
        YearMonth month = YearMonth.now();
        long thisMonth = assignments.findAll().stream().filter(a -> YearMonth.from(a.getEffectiveDate()).equals(month)).count();
        long unassigned = employees.findAll().stream().filter(e -> e.getCurrentLocation() == null).count();
        return new ApiDtos.DashboardSummary(employees.count(), locations.count(), thisMonth, unassigned);
    }

    @GetMapping("/assignments-by-month")
    public List<ApiDtos.MonthCount> byMonth(@RequestParam(defaultValue = "8") int months) {
        int count = Math.max(1, Math.min(months, 24));
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> YearMonth.now().minusMonths(count - 1L - i)).map(m -> new ApiDtos.MonthCount(m.toString(), assignments.findAll().stream().filter(a -> YearMonth.from(a.getEffectiveDate()).equals(m)).count())).toList();
    }

    @GetMapping("/employees-by-province")
    public List<ApiDtos.ProvinceCount> byProvince() {
        long total = employees.count();
        Map<String, Long> counts = employees.findAll().stream().filter(e -> e.getCurrentLocation() != null).collect(Collectors.groupingBy(e -> e.getCurrentLocation().getProvince(), Collectors.counting()));
        return counts.entrySet().stream().map(e -> new ApiDtos.ProvinceCount(e.getKey(), e.getValue(), total == 0 ? 0 : Math.round(e.getValue() * 10000.0 / total) / 100.0)).toList();
    }
}
