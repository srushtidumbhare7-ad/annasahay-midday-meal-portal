package com.annasahay.service;

import com.annasahay.entity.Menu;
import com.annasahay.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> getAllMenu() {
        return menuRepository.findAll();
    }

    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }

    public Optional<Menu> getMenuByDay(String day) {
        return menuRepository.findByDayIgnoreCase(day);
    }

    public Menu saveMenu(Menu menu) {
        Optional<Menu> existing = menuRepository.findByDayIgnoreCase(menu.getDay());
        if (existing.isPresent()) {
            Menu m = existing.get();
            m.setMealName(menu.getMealName());
            return menuRepository.save(m);
        }
        return menuRepository.save(menu);
    }

    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + id));
        menu.setDay(menuDetails.getDay());
        menu.setMealName(menuDetails.getMealName());
        return menuRepository.save(menu);
    }

    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
}
