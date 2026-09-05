package com.annasahay.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "present_students", nullable = false)
    private int presentStudents;

    // Constructors
    public Attendance() {}

    public Attendance(LocalDate date, String className, int presentStudents) {
        this.date = date;
        this.className = className;
        this.presentStudents = presentStudents;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getPresentStudents() { return presentStudents; }
    public void setPresentStudents(int presentStudents) { this.presentStudents = presentStudents; }
}
