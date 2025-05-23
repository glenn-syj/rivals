package com.glennsyj.rivals.api.rivalry.entity;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "rivalry_participants")
public class RivalryParticipant {

    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riot_account_id", nullable = false)
    private RiotAccount riotAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rivalry_id", nullable = false)
    private Rivalry rivalry;

    @Enumerated(EnumType.STRING)
    private RivalSide side;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RivalryParticipant() {}

    public RivalryParticipant(RiotAccount riotAccount, Rivalry rivalry, RivalSide side) {
        this.riotAccount = riotAccount;
        this.rivalry = rivalry;
        this.side = side;
    }

    public void participate(Rivalry rivalry) {
        this.rivalry = rivalry;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public RiotAccount getRiotAccount() {
        return riotAccount;
    }

    public Rivalry getRivalry() {
        return rivalry;
    }

    public RivalSide getSide() {
        return side;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}