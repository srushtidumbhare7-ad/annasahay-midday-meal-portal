package com.annasahay.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "received_quantity", nullable = false)
    private double receivedQuantity;

    @Column(name = "available_quantity", nullable = false)
    private double availableQuantity;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    // Constructors
    public Stock() {}

    public Stock(Ingredient ingredient, double receivedQuantity, double availableQuantity, LocalDate receivedDate) {
        this.ingredient = ingredient;
        this.receivedQuantity = receivedQuantity;
        this.availableQuantity = availableQuantity;
        this.receivedDate = receivedDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public double getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(double receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public double getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(double availableQuantity) { this.availableQuantity = availableQuantity; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
}
