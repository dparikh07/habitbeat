package com.habitbeat.backend.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(length = 50)
    private String timezone;

    @Column(columnDefinition = "TEXT")
    private String inspiration;

    @Column(columnDefinition = "TEXT")
    private String about;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}