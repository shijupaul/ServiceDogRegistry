package org.policedog.registry.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.policedog.registry.dao.PoliceDogRepository;
import org.policedog.registry.domain.*;
import org.policedog.registry.dto.*;
import org.policedog.registry.exception.ResourceNotFoundException;
import org.policedog.registry.mapper.EntityDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.policedog.registry.domain.Status.LEFT;
import static org.policedog.registry.domain.Status.RETIRED;

@Slf4j
@RequiredArgsConstructor
@Service
public class DogService {

    private final PoliceDogRepository dogRepository;
    private final SupplierService supplierService;
    private final EntityDtoMapper entityDtoMapper;

    @Transactional
    public DogDetailDto createDog(CreateDogRequest createDogRequest) {
        String supplierCode = createDogRequest.getSupplierCode();
        Supplier supplier = getSupplierByCode(supplierCode);

        if (dogRepository.existsByBadgeNumber(createDogRequest.getBadgeNumber())) {
            log.error("Dog with badge number {} already exists", createDogRequest.getBadgeNumber());
            throw new IllegalArgumentException("Dog with badge number " + createDogRequest.getBadgeNumber() + " already exists");
        }

        PoliceDog dog = entityDtoMapper.toPoliceDog(createDogRequest);
        dog.setSupplier(supplier);
        supplier.getDogs().add(dog);

        var savedDog = dogRepository.save(dog);
        log.info("Created new dog with ID {}", savedDog.getId());
        return entityDtoMapper.toDogDetailDto(savedDog);
    }

    @Transactional(readOnly = true)
    public PageResponse<DogDetailDto> getDogs(SearchFilter filter, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<PoliceDog> pageRequest = dogRepository.findDogs(filter.getName(), filter.getBreed(), filter.getSupplierCode(), pageable);
        return entityDtoMapper.toDogDetailPageResponse(pageRequest);
    }

    @Transactional
    public void deleteDogById(Long id) {
        PoliceDog dog = getDogWithId(id);

        if (dog.getDeleted()) {
            log.warn("Dog with ID {} is already deleted", id);
            return;
        }

        dog.setDeleted(true);
        dog.setDeletedAt(LocalDateTime.now());
        dogRepository.save(dog);
        log.info("Soft deleted dog with ID {}", id);
    }

    @Transactional
    public DogDetailDto updateDog(Long id, UpdateDogRequest updateDogRequest) {
        PoliceDog dog = getDogWithId(id);

        if (dog.getDeleted()) {
            log.error("Cannot update deleted dog with ID {}", id);
            throw new IllegalStateException("Cannot update deleted dog with ID " + id);
        }

        if (dog.getStatus().equals(LEFT)) {
            log.error("Cannot update retired dog with Id {}.", id);
            throw new IllegalStateException("Cannot update retired dog with ID " + id);
        }

        String newBadgeNumber = updateDogRequest.getBadgeNumber();
        // Has badge number changed?
        if (!dog.getBadgeNumber().equals(newBadgeNumber)) {
            if (dogRepository.existsByBadgeNumber(newBadgeNumber)) {
                log.error("Dog with badge number {} already exists", newBadgeNumber);
                throw new IllegalArgumentException("Dog with badge number " + newBadgeNumber + " already exists");
            }
        }

        // Has supplier changed?
        if (!dog.getSupplier().getCode().equals(updateDogRequest.getSupplierCode())) {
            Supplier currentSupplier = dog.getSupplier();
            currentSupplier.getDogs().remove(dog);

            String supplierCode = updateDogRequest.getSupplierCode();
            Supplier newSupplier = getSupplierByCode(supplierCode);
            dog.setSupplier(newSupplier);
        }

        entityDtoMapper.updatePoliceDogFromDto(updateDogRequest, dog);

        var updatedDog = dogRepository.save(dog);
        log.info("Updated dog with ID {}", id);
        return entityDtoMapper.toDogDetailDto(updatedDog);
    }

    @Transactional
    public DogDetailDto retireDog(Long id, @Valid RetireDogRequest retireDogRequest) {
        PoliceDog dog = getDogWithId(id);

        if (dog.getDeleted()) {
            log.error("Cannot retire deleted dog with ID {}", id);
            throw new IllegalStateException("Cannot retire deleted dog with ID " + id);
        }
        if (dog.getStatus().equals(RETIRED)) {
            log.warn("Dog with ID {} is already retired", id);
            return entityDtoMapper.toDogDetailDto(dog);
        }

        dog.setStatus(Status.RETIRED);
        dog.setLeavingDate(retireDogRequest.getLeavingDate());
        dog.setLeavingReason(retireDogRequest.getLeavingReason());
        var retiredDog = dogRepository.save(dog);
        log.info("Retired dog with ID {}", id);
        return entityDtoMapper.toDogDetailDto(retiredDog);
    }

    @Transactional(readOnly = true)
    public DogDetailDto getDogById(Long id) {
        PoliceDog dog = getDogWithId(id);
        return entityDtoMapper.toDogDetailDto(dog);
    }

    public List<DogDetailDto> getDogsByGender(Gender gender) {
        List<PoliceDog> dogs = dogRepository.findAllByGender(gender);
        return mapDogs(dogs);
    }

    public List<DogDetailDto> getDogsByStatus(Status status) {
        List<PoliceDog> dogs = dogRepository.findAllByStatus(status);
        return mapDogs(dogs);
    }

    public List<DogDetailDto> getDogsByLeavingReason(LeavingReason leavingReason) {
        List<PoliceDog> dogs = dogRepository.findAllByLeavingReason(leavingReason);
        return mapDogs(dogs);
    }

    private PoliceDog getDogWithId(Long id) {
        return dogRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Dog with ID {} not found for deletion", id);
                    return new ResourceNotFoundException("Dog with ID " + id + " not found");
                });
    }

    private Supplier getSupplierByCode(String supplierCode) {
        return supplierService.getSupplierByCode(supplierCode);
    }

    private List<DogDetailDto> mapDogs(List<PoliceDog> dogs) {
        return dogs.stream()
                .map(entityDtoMapper::toDogDetailDto)
                .toList();
    }

}
