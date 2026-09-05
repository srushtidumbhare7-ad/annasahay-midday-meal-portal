package com.annasahay.repository;

import com.annasahay.entity.Consumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsumptionRepository extends JpaRepository<Consumption, Long> {
    List<Consumption> findByDate(LocalDate date);
    List<Consumption> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
