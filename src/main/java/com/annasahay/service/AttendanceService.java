package com.annasahay.service;

import com.annasahay.entity.Attendance;
import com.annasahay.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public Attendance saveAttendance(Attendance attendance) {
        Optional<Attendance> existing = attendanceRepository.findByDateAndClassName(attendance.getDate(), attendance.getClassName());
        if (existing.isPresent()) {
            Attendance record = existing.get();
            record.setPresentStudents(attendance.getPresentStudents());
            return attendanceRepository.save(record);
        }
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceHistory(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);
    }
}
