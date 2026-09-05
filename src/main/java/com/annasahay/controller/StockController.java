package com.annasahay.controller;

import com.annasahay.entity.Stock;
import com.annasahay.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    public List<Stock> getAllStock() {
        return stockService.getAllStock();
    }

    @PostMapping
    public ResponseEntity<?> addStock(@RequestBody Map<String, Object> payload) {
        try {
            Long ingredientId = Long.valueOf(payload.get("ingredientId").toString());
            double receivedQuantity = Double.parseDouble(payload.get("receivedQuantity").toString());
            LocalDate receivedDate = LocalDate.parse(payload.get("receivedDate").toString());
            
            Stock stock = stockService.addStock(ingredientId, receivedQuantity, receivedDate);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Stock stockDetails) {
        try {
            Stock updated = stockService.updateStock(id, stockDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStock(@PathVariable Long id) {
        try {
            stockService.deleteStock(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
