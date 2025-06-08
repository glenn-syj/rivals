package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TftMatchRepository extends JpaRepository<TftMatch, Long> {
}
