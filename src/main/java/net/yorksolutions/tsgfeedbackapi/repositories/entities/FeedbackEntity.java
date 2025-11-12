package net.yorksolutions.tsgfeedbackapi.repositories.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
public class FeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "member_id", nullable = false, length = 36)
    private String memberId;

    @Column(name = "provider_name", nullable = false, length = 80)
    private String providerName;

    @Column(nullable = false)
    private int rating;

    @Column(length = 200)
    private String comment;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, columnDefinition = "timestamptz")
    private Instant submittedAt;
}

