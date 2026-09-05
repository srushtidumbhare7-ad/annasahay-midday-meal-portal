package com.annasahay.controller;

import com.annasahay.entity.Attendance;
import com.annasahay.entity.Consumption;
import com.annasahay.entity.Ingredient;
import com.annasahay.entity.Stock;
import com.annasahay.service.AttendanceService;
import com.annasahay.service.ConsumptionService;
import com.annasahay.service.IngredientService;
import com.annasahay.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ConsumptionService consumptionService;

    @Autowired
    private StockService stockService;

    @Autowired
    private IngredientService ingredientService;

    @GetMapping("/attendance")
    public List<Attendance> getAttendanceReport(@RequestParam String startDate, @RequestParam String endDate) {
        return attendanceService.getAttendanceHistory(LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @GetMapping("/stock-usage")
    public List<Consumption> getStockUsageReport(@RequestParam String startDate, @RequestParam String endDate) {
        return consumptionService.getConsumptionHistory(LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @GetMapping("/inventory")
    public List<Map<String, Object>> getInventoryReport() {
        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        List<Map<String, Object>> inventory = new ArrayList<>();
        for (Ingredient ing : ingredients) {
            double available = stockService.getAvailableQuantityForIngredient(ing);
            Map<String, Object> map = new HashMap<>();
            map.put("ingredientId", ing.getId());
            map.put("ingredientName", ing.getIngredientName());
            map.put("unit", ing.getUnit());
            map.put("availableQuantity", available);
            inventory.add(map);
        }
        return inventory;
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> monthlyAttendance = attendanceService.getAttendanceHistory(start, end);
        int totalPresentCount = monthlyAttendance.stream().mapToInt(Attendance::getPresentStudents).sum();
        long datesWithAttendanceCount = monthlyAttendance.stream().map(Attendance::getDate).distinct().count();
        double avgAttendance = datesWithAttendanceCount > 0 ? (double) totalPresentCount / datesWithAttendanceCount : 0.0;

        List<Consumption> monthlyConsumption = consumptionService.getConsumptionHistory(start, end);
        Map<String, Double> consumptionSummary = monthlyConsumption.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getIngredient().getIngredientName(),
                        Collectors.summingDouble(Consumption::getQuantityUsed)
                ));

        List<Stock> allStocks = stockService.getAllStock();
        List<Stock> monthlyStocks = allStocks.stream()
                .filter(s -> !s.getReceivedDate().isBefore(start) && !s.getReceivedDate().isAfter(end))
                .collect(Collectors.toList());
        Map<String, Double> refillsSummary = monthlyStocks.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getIngredient().getIngredientName(),
                        Collectors.summingDouble(Stock::getReceivedQuantity)
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("yearMonth", yearMonth);
        report.put("totalAttendance", totalPresentCount);
        report.put("avgAttendance", Math.round(avgAttendance * 100.0) / 100.0);
        report.put("consumption", consumptionSummary);
        report.put("refills", refillsSummary);

        return ResponseEntity.ok(report);
    }
}
