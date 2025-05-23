package com.glennsyj.rivals.api.rivalry.repository;

import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RivalryRepository extends JpaRepository<Rivalry, Long> {

    Optional<Rivalry> findRivalryById(Long id);
}
