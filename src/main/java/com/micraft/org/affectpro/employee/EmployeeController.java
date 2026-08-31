package com.micraft.org.affectpro.employee;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import com.micraft.org.affectpro.api.ApiDtos;
import com.micraft.org.affectpro.api.ApiMapper;
import com.micraft.org.affectpro.assignment.AssignmentRepository;
import com.micraft.org.affectpro.common.ApiException;
import com.micraft.org.affectpro.location.Location;
import com.micraft.org.affectpro.location.LocationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeRepository employees; private final LocationRepository locations; private final AssignmentRepository assignments;
    public EmployeeController(EmployeeRepository employees,LocationRepository locations,AssignmentRepository assignments){this.employees=employees;this.locations=locations;this.assignments=assignments;}
    @GetMapping public ApiDtos.PageResponse<ApiDtos.EmployeeView> list(@RequestParam(defaultValue="") String search,@RequestParam(required=false) String civility,@RequestParam(required=false) String position,@RequestParam(required=false) String province,@RequestParam(required=false) Boolean assigned,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        List<ApiDtos.EmployeeView> all=employees.findAll().stream().filter(e->matches(e,search,civility,position,province,assigned)).map(e->ApiMapper.employee(e,assignments)).toList(); return page(all,page,size); }
    @GetMapping("/{id}") public ApiDtos.EmployeeView get(@PathVariable UUID id){return ApiMapper.employee(getEntity(id),assignments);}
    @PostMapping public ResponseEntity<ApiDtos.EmployeeView> create(@Valid @RequestBody ApiDtos.EmployeeRequest r){Employee e=new Employee();e.setEmployeeNumber("E%03d".formatted(employees.count()+1));apply(e,r);e=employees.save(e);return ResponseEntity.created(uri(e.getId())).body(ApiMapper.employee(e,assignments));}
    @PutMapping("/{id}") public ApiDtos.EmployeeView update(@PathVariable UUID id,@Valid @RequestBody ApiDtos.EmployeeRequest r){Employee e=getEntity(id);apply(e,r);return ApiMapper.employee(employees.save(e),assignments);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){if(assignments.existsByEmployeeId(id))throw new ApiException(HttpStatus.CONFLICT,"EMPLOYEE_HAS_HISTORY","Un employe avec un historique ne peut pas etre supprime.");employees.delete(getEntity(id));}
    @GetMapping("/{id}/assignments") public List<ApiDtos.AssignmentView> history(@PathVariable UUID id){getEntity(id);return assignments.findByEmployeeIdOrderByEffectiveDateDesc(id).stream().map(a->ApiMapper.assignment(a,assignments)).toList();}
    private void apply(Employee e,ApiDtos.EmployeeRequest r){e.setCivility(r.civility().trim());e.setLastName(r.lastName().trim());e.setFirstName(r.firstName().trim());e.setEmail(r.email().trim().toLowerCase());e.setPosition(r.position().trim());e.setCurrentLocation(r.currentLocationId()==null?null:location(r.currentLocationId()));}
    private Employee getEntity(UUID id){return employees.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"EMPLOYEE_NOT_FOUND","Employe introuvable."));}
    private Location location(UUID id){return locations.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"LOCATION_NOT_FOUND","Lieu introuvable."));}
    private boolean matches(Employee e,String search,String civility,String position,String province,Boolean assigned){String text=(e.getEmployeeNumber()+" "+e.getFirstName()+" "+e.getLastName()+" "+e.getEmail()).toLowerCase();return (search==null||search.isBlank()||text.contains(search.toLowerCase()))&&(civility==null||civility.equals(e.getCivility()))&&(position==null||position.equals(e.getPosition()))&&(province==null||(e.getCurrentLocation()!=null&&province.equals(e.getCurrentLocation().getProvince())))&&(assigned==null||assigned==(e.getCurrentLocation()!=null));}
    private URI uri(UUID id){return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();}
    private <T> ApiDtos.PageResponse<T> page(List<T> all,int page,int size){int safe=Math.max(1,Math.min(size,100)),p=Math.max(0,page),from=Math.min(p*safe,all.size());return new ApiDtos.PageResponse<>(all.subList(from,Math.min(from+safe,all.size())),p,safe,all.size(),(int)Math.ceil((double)all.size()/safe));}
}
