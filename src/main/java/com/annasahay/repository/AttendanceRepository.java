package com.annasahay.repository;

import com.annasahay.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDate(LocalDate date);
    Optional<Attendance> findByDateAndClassName(LocalDate date, String className);
    List<Attendance> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
