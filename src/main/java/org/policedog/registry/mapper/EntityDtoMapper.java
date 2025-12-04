package org.policedog.registry.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.policedog.registry.domain.Characteristics;
import org.policedog.registry.domain.PoliceDog;
import org.policedog.registry.domain.Supplier;
import org.policedog.registry.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityDtoMapper {

    DogDetailDto toDogDetailDto(PoliceDog dog);

    DogSummaryDto toDogSummaryDto(PoliceDog dog);

    SupplierSummaryDto toSupplierSummaryDto(Supplier supplier);

    CharacteristicsDto toCharacteristicsDto(Characteristics characteristics);

    SupplierDetailDto toSupplierDetailDto(Supplier supplier);

    default PageResponse<DogDetailDto> toDogDetailPageResponse(Page<PoliceDog> page) {
        if (page == null) {
            return null;
        }

        List<DogDetailDto> dogDetailDtos = page.getContent().stream()
                .map(this::toDogDetailDto)
                .toList();

        PageResponse.PageMetadata metadata = new PageResponse.PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        return new PageResponse<>(dogDetailDtos, metadata);
    }

    @Mappings({@Mapping(target = "id", ignore = true),
    @Mapping(target = "supplier", ignore = true),
    @Mapping(target = "dateAcquired", ignore = true),
    @Mapping(target = "leavingDate", ignore = true),
    @Mapping(target = "leavingReason", ignore = true),
    @Mapping(target = "version", ignore = true),
    @Mapping(target = "deleted", ignore = true),
    @Mapping(target = "deletedAt", ignore = true)})
    PoliceDog toPoliceDog(CreateDogRequest createDogRequest);

    Characteristics toCharacteristics(CharacteristicsDto characteristicsDto);

    @Mappings({@Mapping(target = "id", ignore = true),
            @Mapping(target = "supplier", ignore = true),
            @Mapping(target = "dateAcquired", ignore = true),
            @Mapping(target = "birthDate", ignore = true),
            @Mapping(target = "leavingDate", ignore = true),
            @Mapping(target = "leavingReason", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true)})
    void updatePoliceDogFromDto(UpdateDogRequest updateDogRequest, @MappingTarget PoliceDog dog);

    void updateCharacteristicsFromDto(CharacteristicsDto characteristicsDto, @MappingTarget Characteristics characteristics);

    default PageResponse<SupplierDetailDto> toSupplierDetailPageResponse(Page<Supplier> page) {
        if (page == null) {
            return null;
        }

        List<SupplierDetailDto> supplierDetailDtos = page.getContent().stream()
                .map(this::toSupplierDetailDto)
                .toList();

        PageResponse.PageMetadata metadata = new PageResponse.PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        return new PageResponse<>(supplierDetailDtos, metadata);
    }

    @Mappings({@Mapping(target = "id", ignore = true),
            @Mapping(target = "dogs", ignore = true),
            @Mapping(target = "version", ignore = true)})
    Supplier toSupplier(SupplierRequest createSupplierRequest);

    @Mapping(target = "id", ignore = true)
    void updateSupplierFromDto(SupplierRequest supplierRequest, @MappingTarget Supplier supplier);
}
