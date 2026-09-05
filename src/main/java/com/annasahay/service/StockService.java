package com.annasahay.service;

import com.annasahay.entity.Stock;
import com.annasahay.entity.Ingredient;
import com.annasahay.repository.StockRepository;
import com.annasahay.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    public List<Stock> getAllStock() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    public Stock addStock(Long ingredientId, double receivedQuantity, LocalDate receivedDate) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id " + ingredientId));
        Stock stock = new Stock(ingredient, receivedQuantity, receivedQuantity, receivedDate);
        return stockRepository.save(stock);
    }

    public Stock updateStock(Long id, Stock stockDetails) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock record not found with id " + id));
        stock.setReceivedQuantity(stockDetails.getReceivedQuantity());
        stock.setAvailableQuantity(stockDetails.getAvailableQuantity());
        stock.setReceivedDate(stockDetails.getReceivedDate());
        if (stockDetails.getIngredient() != null && stockDetails.getIngredient().getId() != null) {
            Ingredient ingredient = ingredientRepository.findById(stockDetails.getIngredient().getId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found"));
            stock.setIngredient(ingredient);
        }
        return stockRepository.save(stock);
    }

    public double getAvailableQuantityForIngredient(Ingredient ingredient) {
        List<Stock> activeStocks = stockRepository.findByIngredientAndAvailableQuantityGreaterThanOrderByReceivedDateAsc(ingredient, 0.0);
        return activeStocks.stream().mapToDouble(Stock::getAvailableQuantity).sum();
    }

    public void deleteStock(Long id) {
        stockRepository.deleteById(id);
    }
}
