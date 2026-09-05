package com.annasahay.controller;

import com.annasahay.entity.Attendance;
import com.annasahay.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    @GetMapping("/by-date")
    public List<Attendance> getAttendanceByDate(@RequestParam String date) {
        return attendanceService.getAttendanceByDate(LocalDate.parse(date));
    }

    @PostMapping
    public ResponseEntity<?> saveAttendance(@RequestBody List<Attendance> attendanceList) {
        try {
            for (Attendance attendance : attendanceList) {
                attendanceService.saveAttendance(attendance);
            }
            return ResponseEntity.ok(Map.of("message", "Attendance saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
