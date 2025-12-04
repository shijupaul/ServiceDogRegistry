package org.policedog.registry.dao;

import org.policedog.registry.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

}
