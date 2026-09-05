package com.annasahay.service;

import com.annasahay.entity.Consumption;
import com.annasahay.entity.Stock;
import com.annasahay.entity.Ingredient;
import com.annasahay.repository.ConsumptionRepository;
import com.annasahay.repository.StockRepository;
import com.annasahay.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ConsumptionService {

    @Autowired
    private ConsumptionRepository consumptionRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    public List<Consumption> getAllConsumption() {
        return consumptionRepository.findAll();
    }

    public List<Consumption> getConsumptionByDate(LocalDate date) {
        return consumptionRepository.findByDate(date);
    }

    @Transactional
    public Consumption recordConsumption(Long ingredientId, double quantityUsed, LocalDate date) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id " + ingredientId));

        double remainingToDeduct = quantityUsed;
        List<Stock> activeStocks = stockRepository.findByIngredientAndAvailableQuantityGreaterThanOrderByReceivedDateAsc(ingredient, 0.0);
        
        double totalAvailable = activeStocks.stream().mapToDouble(Stock::getAvailableQuantity).sum();
        if (totalAvailable < quantityUsed) {
            throw new RuntimeException("Insufficient stock for " + ingredient.getIngredientName() + 
                ". Available: " + totalAvailable + " " + ingredient.getUnit() + 
                ", Requested: " + quantityUsed + " " + ingredient.getUnit());
        }

        for (Stock stock : activeStocks) {
            if (remainingToDeduct <= 0) break;
            
            double available = stock.getAvailableQuantity();
            if (available >= remainingToDeduct) {
                stock.setAvailableQuantity(available - remainingToDeduct);
                remainingToDeduct = 0;
            } else {
                stock.setAvailableQuantity(0);
                remainingToDeduct -= available;
            }
            stockRepository.save(stock);
        }

        Consumption consumption = new Consumption(ingredient, quantityUsed, date);
        return consumptionRepository.save(consumption);
    }

    @Transactional
    public void deleteConsumption(Long id) {
        Consumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consumption record not found with id " + id));
        
        Ingredient ingredient = consumption.getIngredient();
        double quantityToRestore = consumption.getQuantityUsed();
        List<Stock> stocks = stockRepository.findByIngredientOrderByReceivedDateAsc(ingredient);
        
        if (!stocks.isEmpty()) {
            for (Stock stock : stocks) {
                if (quantityToRestore <= 0) break;
                double maxRestore = stock.getReceivedQuantity() - stock.getAvailableQuantity();
                if (maxRestore > 0) {
                    if (maxRestore >= quantityToRestore) {
                        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantityToRestore);
                        quantityToRestore = 0;
                    } else {
                        stock.setAvailableQuantity(stock.getReceivedQuantity());
                        quantityToRestore -= maxRestore;
                    }
                    stockRepository.save(stock);
                }
            }
            if (quantityToRestore > 0) {
                Stock latest = stocks.get(stocks.size() - 1);
                latest.setAvailableQuantity(latest.getAvailableQuantity() + quantityToRestore);
                stockRepository.save(latest);
            }
        } else {
            Stock restoredStock = new Stock(ingredient, quantityToRestore, quantityToRestore, LocalDate.now());
            stockRepository.save(restoredStock);
        }

        consumptionRepository.delete(consumption);
    }

    public List<Consumption> getConsumptionHistory(LocalDate startDate, LocalDate endDate) {
        return consumptionRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);
    }
}
