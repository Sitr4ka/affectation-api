package com.micraft.org.affectpro.assignment;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.micraft.org.affectpro.api.ApiDtos;
import com.micraft.org.affectpro.api.ApiMapper;
import com.micraft.org.affectpro.common.ApiException;
import com.micraft.org.affectpro.employee.Employee;
import com.micraft.org.affectpro.employee.EmployeeRepository;
import com.micraft.org.affectpro.location.Location;
import com.micraft.org.affectpro.location.LocationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {
    private final AssignmentRepository assignments; private final EmployeeRepository employees; private final LocationRepository locations;
    public AssignmentController(AssignmentRepository assignments,EmployeeRepository employees,LocationRepository locations){this.assignments=assignments;this.employees=employees;this.locations=locations;}
    @GetMapping public ApiDtos.PageResponse<ApiDtos.AssignmentView> list(@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(required=false) UUID employeeId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        List<ApiDtos.AssignmentView> all=assignments.findAll().stream().filter(a->(from==null||!a.getEffectiveDate().isBefore(from))&&(to==null||!a.getEffectiveDate().isAfter(to))&&(employeeId==null||a.getEmployee().getId().equals(employeeId))).sorted(Comparator.comparing(Assignment::getEffectiveDate).reversed()).map(a->ApiMapper.assignment(a,assignments)).toList();return page(all,page,size);}
    @GetMapping("/{id}") public ApiDtos.AssignmentView get(@PathVariable UUID id){return ApiMapper.assignment(getEntity(id),assignments);}
    @PostMapping @Transactional public ResponseEntity<ApiDtos.AssignmentView> create(@Valid @RequestBody ApiDtos.AssignmentRequest r){Assignment a=new Assignment();a.setAssignmentNumber("A%03d".formatted(assignments.count()+1));apply(a,r,true);a=assignments.save(a);return ResponseEntity.created(uri(a.getId())).body(ApiMapper.assignment(a,assignments));}
    @PutMapping("/{id}") @Transactional public ApiDtos.AssignmentView update(@PathVariable UUID id,@Valid @RequestBody ApiDtos.AssignmentRequest r){Assignment a=getEntity(id);Employee previousEmployee=a.getEmployee();apply(a,r,false);a=assignments.save(a);refreshCurrentLocation(previousEmployee);if(!previousEmployee.getId().equals(a.getEmployee().getId()))refreshCurrentLocation(a.getEmployee());return ApiMapper.assignment(a,assignments);}
    @DeleteMapping("/{id}") @Transactional @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){Assignment a=getEntity(id);Employee e=a.getEmployee();assignments.delete(a);assignments.flush();refreshCurrentLocation(e);}
    private void apply(Assignment a,ApiDtos.AssignmentRequest r,boolean creating){Employee e=employee(r.employeeId());Location target=location(r.newLocationId());Location old=e.getCurrentLocation();if(old!=null&&old.getId().equals(target.getId()))throw new ApiException(HttpStatus.CONFLICT,"SAME_LOCATION","Le nouveau lieu doit etre different du lieu actuel.");a.setEmployee(e);a.setPreviousLocation(old);a.setNewLocation(target);a.setEffectiveDate(r.effectiveDate());a.setReason(r.reason().trim());if(creating)e.setCurrentLocation(target);}
    private void refreshCurrentLocation(Employee employee){Location latest=assignments.findByEmployeeIdOrderByEffectiveDateDesc(employee.getId()).stream().findFirst().map(Assignment::getNewLocation).orElse(null);employee.setCurrentLocation(latest);employees.save(employee);}
    private Assignment getEntity(UUID id){return assignments.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"ASSIGNMENT_NOT_FOUND","Affectation introuvable."));}
    private Employee employee(UUID id){return employees.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"EMPLOYEE_NOT_FOUND","Employe introuvable."));}
    private Location location(UUID id){return locations.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"LOCATION_NOT_FOUND","Lieu introuvable."));}
    private URI uri(UUID id){return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();}
    private <T> ApiDtos.PageResponse<T> page(List<T> all,int page,int size){int safe=Math.max(1,Math.min(size,100)),p=Math.max(0,page),start=Math.min(p*safe,all.size());return new ApiDtos.PageResponse<>(all.subList(start,Math.min(start+safe,all.size())),p,safe,all.size(),(int)Math.ceil((double)all.size()/safe));}
}
