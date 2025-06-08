package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TftMatchParticipantRepository extends JpaRepository<TftMatchParticipant, Long> {
}
