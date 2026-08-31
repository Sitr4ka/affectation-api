package com.micraft.org.affectpro.location;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import com.micraft.org.affectpro.api.ApiDtos;
import com.micraft.org.affectpro.api.ApiMapper;
import com.micraft.org.affectpro.assignment.AssignmentRepository;
import com.micraft.org.affectpro.common.ApiException;
import com.micraft.org.affectpro.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private final LocationRepository locations; private final EmployeeRepository employees; private final AssignmentRepository assignments;
    public LocationController(LocationRepository locations, EmployeeRepository employees, AssignmentRepository assignments) { this.locations = locations; this.employees = employees; this.assignments = assignments; }
    @GetMapping public ApiDtos.PageResponse<ApiDtos.LocationView> list(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "") String province, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<ApiDtos.LocationView> all = locations.findAll().stream().filter(l -> contains(l.getDesignation(), search) && contains(l.getProvince(), province)).map(l -> ApiMapper.location(l, employees.findAll().stream().filter(e -> e.getCurrentLocation() != null && e.getCurrentLocation().getId().equals(l.getId())).count())).toList();
        return page(all, page, size);
    }
    @GetMapping("/{id}") public ApiDtos.LocationView get(@PathVariable UUID id) { Location l = getEntity(id); return ApiMapper.location(l, employees.findAll().stream().filter(e -> e.getCurrentLocation()!=null && e.getCurrentLocation().getId().equals(id)).count()); }
    @PostMapping public ResponseEntity<ApiDtos.LocationView> create(@Valid @RequestBody ApiDtos.LocationRequest r) { Location l = new Location(); l.setLocationNumber("L%03d".formatted(locations.count()+1)); apply(l,r); l=locations.save(l); return ResponseEntity.created(location(l.getId())).body(ApiMapper.location(l,0)); }
    @PutMapping("/{id}") public ApiDtos.LocationView update(@PathVariable UUID id,@Valid @RequestBody ApiDtos.LocationRequest r) { Location l=getEntity(id); apply(l,r); return ApiMapper.location(locations.save(l),0); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id) { if (employees.findAll().stream().anyMatch(e->e.getCurrentLocation()!=null&&e.getCurrentLocation().getId().equals(id)) || assignments.existsByPreviousLocationIdOrNewLocationId(id,id)) throw new ApiException(HttpStatus.CONFLICT,"LOCATION_IN_USE","Ce lieu ne peut pas etre supprime car il est encore reference."); locations.delete(getEntity(id)); }
    private Location getEntity(UUID id) { return locations.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"LOCATION_NOT_FOUND","Lieu introuvable.")); }
    private void apply(Location l, ApiDtos.LocationRequest r) { l.setDesignation(r.designation().trim()); l.setProvince(r.province().trim()); }
    private URI location(UUID id) { return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri(); }
    private boolean contains(String value,String query) { return query==null||query.isBlank()||value.toLowerCase().contains(query.toLowerCase()); }
    private <T> ApiDtos.PageResponse<T> page(List<T> all,int page,int size) { int safe=Math.max(1,Math.min(size,100)); int p=Math.max(0,page); int from=Math.min(p*safe,all.size()); return new ApiDtos.PageResponse<>(all.subList(from,Math.min(from+safe,all.size())),p,safe,all.size(),(int)Math.ceil((double)all.size()/safe)); }
}
