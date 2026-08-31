package com.micraft.org.affectpro.location;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByLocationNumber(String locationNumber);
}
