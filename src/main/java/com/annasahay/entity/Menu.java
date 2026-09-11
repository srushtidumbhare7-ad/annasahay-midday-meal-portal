package com.annasahay.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`day`", nullable = false, unique = true)
    private String day; // Monday - Saturday

    @Column(name = "meal_name", nullable = false)
    private String mealName;

    // Constructors
    public Menu() {}

    public Menu(String day, String mealName) {
        this.day = day;
        this.mealName = mealName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
}
