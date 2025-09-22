package com.hoaithi.profile_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column
    String userId;
    @Column
    String fullName;
    @Column
    LocalDate dob;
    @Column
    String city;

    @Column
    String avatarUrl;

    @Column
    String bannerUrl;

    @Column
    @Builder.Default
    LocalDate createdAt = LocalDate.now();
}
