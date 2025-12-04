package org.policedog.registry.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.NONE;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PoliceDog {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Long id;

    private String name;

    private String breed;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(unique = true, nullable = false)
    private String badgeNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    @CreatedDate
    private LocalDate dateAcquired;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate leavingDate;

    @Enumerated(EnumType.STRING)
    private LeavingReason leavingReason;

    @Embedded
    private Characteristics characteristics;

    @Version
    @Setter(NONE)
    private Long version;

    private Boolean deleted = false;

    private LocalDateTime deletedAt;
}
