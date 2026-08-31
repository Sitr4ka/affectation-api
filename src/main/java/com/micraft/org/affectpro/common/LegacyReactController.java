package com.micraft.org.affectpro.common;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.micraft.org.affectpro.assignment.Assignment;
import com.micraft.org.affectpro.assignment.AssignmentRepository;
import com.micraft.org.affectpro.employee.Employee;
import com.micraft.org.affectpro.employee.EmployeeRepository;
import com.micraft.org.affectpro.location.Location;
import com.micraft.org.affectpro.location.LocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur transitoire pour le client React historique, qui appelait les
 * anciens scripts PHP. L'API REST versionnee sous /api/v1 reste inchangée.
 */
@RestController
@RequestMapping("/affect-pro/backend")
public class LegacyReactController {
    private final EmployeeRepository employees;
    private final LocationRepository locations;
    private final AssignmentRepository assignments;

    public LegacyReactController(EmployeeRepository employees, LocationRepository locations, AssignmentRepository assignments) {
        this.employees = employees;
        this.locations = locations;
        this.assignments = assignments;
    }

    @GetMapping("/employeApi.php")
    public Map<String, Object> employees(@RequestParam(required = false) String action) {
        if ("nextId".equals(action)) return success("nextNumEmp", next("E", employees.count() + 1));
        if ("countNonAssigned".equals(action)) return success("count", employees.findAll().stream().filter(employee -> employee.getCurrentLocation() == null).count());
        return success("employe", employees.findAll().stream().map(this::employee).toList());
    }

    @PostMapping("/employeApi.php")
    public Map<String, Object> createEmployee(@RequestBody EmployeePayload payload) {
        Employee employee = new Employee();
        employee.setEmployeeNumber(valueOr(payload.numEmp(), next("E", employees.count() + 1)));
        apply(employee, payload);
        employees.save(employee);
        return success("numEmp", employee.getEmployeeNumber());
    }

    @PutMapping("/employeApi.php")
    public Map<String, Object> updateEmployee(@RequestBody EmployeePayload payload) {
        Employee employee = findEmployee(payload.numEmp());
        apply(employee, payload);
        employees.save(employee);
        return success();
    }

    @DeleteMapping("/employeApi.php")
    public Map<String, Object> deleteEmployee(@RequestBody NumberPayload payload) {
        Employee employee = findEmployee(payload.numEmp());
        if (assignments.existsByEmployeeId(employee.getId())) throw conflict("Cet employe possède un historique d'affectation.");
        employees.delete(employee);
        return success();
    }

    @GetMapping("/placeApi.php")
    public Map<String, Object> locations(@RequestParam(required = false) String action) {
        if ("nextId".equals(action)) return success("nextIdLieu", next("L", locations.count() + 1));
        return success("lieu", locations.findAll().stream().map(this::location).toList());
    }

    @PostMapping("/placeApi.php")
    public Map<String, Object> createLocation(@RequestBody LocationPayload payload) {
        Location location = new Location();
        location.setLocationNumber(valueOr(payload.idLieu(), next("L", locations.count() + 1)));
        apply(location, payload);
        locations.save(location);
        return success("idLieu", location.getLocationNumber());
    }

    @PutMapping("/placeApi.php")
    public Map<String, Object> updateLocation(@RequestBody LocationPayload payload) {
        Location location = findLocation(payload.idLieu());
        apply(location, payload);
        locations.save(location);
        return success();
    }

    @DeleteMapping("/placeApi.php")
    public Map<String, Object> deleteLocation(@RequestBody NumberPayload payload) {
        Location location = findLocation(payload.idLieu());
        if (employees.findAll().stream().anyMatch(e -> e.getCurrentLocation() != null && e.getCurrentLocation().getId().equals(location.getId()))
                || assignments.existsByPreviousLocationIdOrNewLocationId(location.getId(), location.getId())) throw conflict("Ce lieu est encore référence.");
        locations.delete(location);
        return success();
    }

    @GetMapping("/affectationApi.php")
    public Map<String, Object> assignments(@RequestParam(required = false) String action) {
        if ("nextId".equals(action)) return success("nextNumAffect", next("A", assignments.count() + 1));
        if ("countThisMonth".equals(action)) return success("count", assignments.findAll().stream().filter(assignment -> YearMonth.from(assignment.getEffectiveDate()).equals(YearMonth.now())).count());
        return success("affectation", assignments.findAll().stream().map(this::assignment).toList());
    }

    @PostMapping("/affectationApi.php") @Transactional
    public Map<String, Object> createAssignment(@RequestBody AssignmentPayload payload) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentNumber(valueOr(payload.numAffect(), next("A", assignments.count() + 1)));
        apply(assignment, payload, true);
        assignments.save(assignment);
        return success("numAffect", assignment.getAssignmentNumber());
    }

    @PutMapping("/affectationApi.php") @Transactional
    public Map<String, Object> updateAssignment(@RequestBody AssignmentPayload payload) {
        Assignment assignment = findAssignment(payload.numAffect());
        apply(assignment, payload, false);
        assignments.save(assignment);
        refreshLocation(assignment.getEmployee());
        return success();
    }

    @DeleteMapping("/affectationApi.php") @Transactional
    public Map<String, Object> deleteAssignment(@RequestBody NumberPayload payload) {
        Assignment assignment = findAssignment(payload.numAffect());
        Employee employee = assignment.getEmployee();
        assignments.delete(assignment);
        assignments.flush();
        refreshLocation(employee);
        return success();
    }

    private void apply(Employee employee, EmployeePayload payload) {
        employee.setCivility(required(payload.civilite(), "civilite")); employee.setLastName(required(payload.nom(), "nom"));
        employee.setFirstName(required(payload.prenom(), "prenom")); employee.setEmail(required(payload.mail(), "mail").toLowerCase());
        employee.setPosition(required(payload.poste(), "poste"));
        employee.setCurrentLocation(blank(payload.lieuactuel()) ? null : findLocation(payload.lieuactuel()));
    }
    private void apply(Location location, LocationPayload payload) { location.setDesignation(required(payload.designation(), "designation")); location.setProvince(required(payload.province(), "province")); }
    private void apply(Assignment assignment, AssignmentPayload payload, boolean creating) {
        Employee employee = findEmployee(payload.numEmp()); Location newLocation = findLocation(payload.nouveauLieu());
        if (employee.getCurrentLocation() != null && employee.getCurrentLocation().getId().equals(newLocation.getId())) throw conflict("Le nouveau lieu doit être différent du lieu actuel.");
        assignment.setEmployee(employee); assignment.setPreviousLocation(employee.getCurrentLocation()); assignment.setNewLocation(newLocation);
        assignment.setEffectiveDate(parseDate(payload.datePriseService(), payload.dateAffect())); assignment.setReason(valueOr(payload.raison(), "Affectation interne"));
        if (creating) employee.setCurrentLocation(newLocation);
    }
    private void refreshLocation(Employee employee) { employee.setCurrentLocation(assignments.findByEmployeeIdOrderByEffectiveDateDesc(employee.getId()).stream().findFirst().map(Assignment::getNewLocation).orElse(null)); employees.save(employee); }
    private Map<String, Object> employee(Employee e) {
        Map<String, Object> data = new LinkedHashMap<>();
        Location currentLocation = e.getCurrentLocation();
        data.put("numEmp", e.getEmployeeNumber()); data.put("civilite", e.getCivility());
        data.put("nom", e.getLastName()); data.put("prenom", e.getFirstName());
        data.put("mail", e.getEmail()); data.put("poste", e.getPosition());
        data.put("lieuactuel", currentLocation == null ? null : currentLocation.getLocationNumber());
        // Champs attendus par la page Employes pour le lieu, le filtre et le statut.
        data.put("lieu_designation", currentLocation == null ? null : currentLocation.getDesignation());
        data.put("province", currentLocation == null ? null : currentLocation.getProvince());
        return data;
    }
    private Map<String, Object> location(Location l) { return Map.of("idLieu", l.getLocationNumber(), "designation", l.getDesignation(), "province", l.getProvince(), "nombre_employes", employees.findAll().stream().filter(e -> e.getCurrentLocation() != null && e.getCurrentLocation().getId().equals(l.getId())).count()); }
    private Map<String, Object> assignment(Assignment a) { Map<String, Object> data = new LinkedHashMap<>(); data.put("numAffect", a.getAssignmentNumber()); data.putAll(employee(a.getEmployee())); data.put("ancienLieu", a.getPreviousLocation() == null ? null : a.getPreviousLocation().getLocationNumber()); data.put("ancienLieuNom", a.getPreviousLocation() == null ? null : a.getPreviousLocation().getDesignation()); data.put("nouveauLieu", a.getNewLocation().getLocationNumber()); data.put("nouveauLieuNom", a.getNewLocation().getDesignation()); data.put("dateAffect", a.getEffectiveDate().toString()); data.put("datePriseService", a.getEffectiveDate().toString()); data.put("raison", a.getReason()); return data; }
    private Employee findEmployee(String number) { return employees.findByEmployeeNumber(required(number, "numEmp")).orElseThrow(() -> notFound("Employe introuvable.")); }
    private Location findLocation(String number) { return locations.findByLocationNumber(required(number, "idLieu")).orElseThrow(() -> notFound("Lieu introuvable.")); }
    private Assignment findAssignment(String number) { return assignments.findByAssignmentNumber(required(number, "numAffect")).orElseThrow(() -> notFound("Affectation introuvable.")); }
    private LocalDate parseDate(String first, String fallback) { try { return LocalDate.parse(valueOr(first, fallback)); } catch (Exception e) { throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE", "Date d'affectation invalide."); } }
    private String required(String value, String field) { if (blank(value)) throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_FIELD", "Le champ " + field + " est obligatoire."); return value.trim(); }
    private String valueOr(String value, String fallback) { return blank(value) ? fallback : value.trim(); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String next(String prefix, long value) { return "%s%03d".formatted(prefix, value); }
    private ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message); }
    private ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message); }
    private Map<String, Object> success() { return success(null, null); }
    private Map<String, Object> success(String key, Object value) { Map<String, Object> response = new LinkedHashMap<>(); response.put("success", true); if (key != null) response.put(key, value); return response; }

    private record EmployeePayload(String numEmp, String civilite, String nom, String prenom, String mail, String poste, String lieuactuel) { }
    private record LocationPayload(String idLieu, String designation, String province) { }
    private record AssignmentPayload(String numAffect, String numEmp, String ancienLieu, String nouveauLieu, String dateAffect, String datePriseService, String raison) { }
    private record NumberPayload(String numEmp, String idLieu, String numAffect) { }
}
