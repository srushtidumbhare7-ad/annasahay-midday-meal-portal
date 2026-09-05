package com.annasahay.controller;

import com.annasahay.entity.Menu;
import com.annasahay.entity.Ingredient;
import com.annasahay.entity.Attendance;
import com.annasahay.service.MenuService;
import com.annasahay.service.StockService;
import com.annasahay.service.AttendanceService;
import com.annasahay.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StockService stockService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private IngredientService ingredientService;

    private static final double LOW_STOCK_THRESHOLD = 10.0;
    private static final int TOTAL_STUDENTS_ENROLLED = 240;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        LocalDate today = LocalDate.now();
        
        List<Attendance> todayAttendance = attendanceService.getAttendanceByDate(today);
        int presentCount = todayAttendance.stream().mapToInt(Attendance::getPresentStudents).sum();

        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (dayOfWeek.equalsIgnoreCase("Sunday")) {
            dayOfWeek = "Saturday";
        }
        Optional<Menu> todayMenuOpt = menuService.getMenuByDay(dayOfWeek);
        String menuName = todayMenuOpt.map(Menu::getMealName).orElse("No Menu Configured");

        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        List<Map<String, Object>> stockList = new ArrayList<>();
        int lowStockCount = 0;

        for (Ingredient ingredient : ingredients) {
            double available = stockService.getAvailableQuantityForIngredient(ingredient);
            Map<String, Object> item = new HashMap<>();
            item.put("ingredientId", ingredient.getId());
            item.put("ingredientName", ingredient.getIngredientName());
            item.put("unit", ingredient.getUnit());
            item.put("availableQuantity", available);
            item.put("isLowStock", available < LOW_STOCK_THRESHOLD);
            stockList.add(item);

            if (available < LOW_STOCK_THRESHOLD) {
                lowStockCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", TOTAL_STUDENTS_ENROLLED);
        summary.put("todayAttendance", presentCount);
        summary.put("todayMenu", menuName);
        summary.put("lowStockCount", lowStockCount);
        summary.put("stocks", stockList);
        summary.put("date", today.toString());

        return ResponseEntity.ok(summary);
    }
}
