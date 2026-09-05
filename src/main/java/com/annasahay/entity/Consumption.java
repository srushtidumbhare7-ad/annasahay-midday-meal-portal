package com.annasahay.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "consumption")
public class Consumption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity_used", nullable = false)
    private double quantityUsed;

    @Column(nullable = false)
    private LocalDate date;

    // Constructors
    public Consumption() {}

    public Consumption(Ingredient ingredient, double quantityUsed, LocalDate date) {
        this.ingredient = ingredient;
        this.quantityUsed = quantityUsed;
        this.date = date;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public double getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(double quantityUsed) { this.quantityUsed = quantityUsed; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
