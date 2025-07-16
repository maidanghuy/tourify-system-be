package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "subcompany_details", schema = "Tourify")
public class SubCompanyDetails {

    @Id
    @Column(name = "user_id")
    String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "website")
    String website;

    @Column(name = "description")
    String description;
}
