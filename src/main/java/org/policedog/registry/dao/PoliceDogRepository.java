package org.policedog.registry.dao;

import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.PoliceDog;
import org.policedog.registry.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PoliceDogRepository extends JpaRepository<PoliceDog, Long> {

    @Query("SELECT d FROM PoliceDog d " +
            "WHERE (:name IS NULL OR d.name LIKE %:name%) " +
            "AND (:breed IS NULL OR d.breed LIKE %:breed%) " +
            "AND (:supplierCode IS NULL OR d.supplier.code LIKE %:supplierCode%)" +
            "AND d.deleted = false")
    Page<PoliceDog> findDogs(@Param("name") String name, @Param("breed") String breed, @Param("supplierCode") String supplierCode, Pageable pageable);

    boolean existsByBadgeNumber(String badgeNumber);

    List<PoliceDog> findAllByGender(Gender gender);

    List<PoliceDog> findAllByStatus(Status status);

    List<PoliceDog> findAllByLeavingReason(LeavingReason leavingReason);
}
