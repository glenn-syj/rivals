package com.glennsyj.rivals.api.rivalry.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="rivalries")
@EntityListeners(AuditingEntityListener.class)
public class Rivalry {

    @Id
    @Tsid
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 소환사의 협곡 랭크로 확장 시 고려
    // 각 Side 마다 특정 인원이 동시에 포함된 비교가 필요할 수 있으므로 Set 대신 List
    @OneToMany(mappedBy = "rivalry")
    private List<RivalryParticipant> participants = new ArrayList<>();

    protected Rivalry() {
    }

    public void addParticipant(RivalryParticipant participant) {
        if (!isDuplicatedOnSameSide(participant)) {
            participants.add(participant);
            participant.participate(this);
        } else {
            throw new IllegalArgumentException("Participant already exists on the same side.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<RivalryParticipant> getParticipants() {
        return participants;
    }

    private boolean isDuplicatedOnSameSide(RivalryParticipant participant) {
        for (RivalryParticipant p : participants) {
            if (p.getRiotAccount().equals(participant.getRiotAccount()) &&
                    p.getSide() == participant.getSide()) {
                return true;
            }
        }
        return false;
    }
}
