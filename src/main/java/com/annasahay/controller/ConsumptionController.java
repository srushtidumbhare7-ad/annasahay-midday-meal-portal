package com.annasahay.controller;

import com.annasahay.dto.ConsumptionRequest;
import com.annasahay.entity.Consumption;
import com.annasahay.service.ConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    @Autowired
    private ConsumptionService consumptionService;

    @GetMapping
    public List<Consumption> getAllConsumption() {
        return consumptionService.getAllConsumption();
    }

    @PostMapping
    public ResponseEntity<?> recordConsumption(@RequestBody ConsumptionRequest request) {
        try {
            Consumption recorded = consumptionService.recordConsumption(
                    request.getIngredientId(),
                    request.getQuantityUsed(),
                    request.getDate()
            );
            return ResponseEntity.ok(recorded);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumption(@PathVariable Long id) {
        try {
            consumptionService.deleteConsumption(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
