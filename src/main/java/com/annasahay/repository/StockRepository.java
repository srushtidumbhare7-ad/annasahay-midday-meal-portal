package com.annasahay.repository;

import com.annasahay.entity.Stock;
import com.annasahay.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByIngredientOrderByReceivedDateAsc(Ingredient ingredient);
    List<Stock> findByIngredientAndAvailableQuantityGreaterThanOrderByReceivedDateAsc(Ingredient ingredient, double greaterThan);
}
