-- Seed School Admin User (Username: admin, Password: admin123 hashed in SHA-256)
INSERT INTO admin (id, username, password, school_name) 
VALUES (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Government Primary School, Sector 5')
ON DUPLICATE KEY UPDATE password=VALUES(password), school_name=VALUES(school_name);

-- Seed Default Ingredients
INSERT INTO ingredient (id, ingredient_name, unit) VALUES 
(1, 'Rice', 'kg'), 
(2, 'Dal', 'kg'), 
(3, 'Oil', 'liter'), 
(4, 'Salt', 'kg'), 
(5, 'Vegetables', 'kg'), 
(6, 'Turmeric', 'kg')
ON DUPLICATE KEY UPDATE ingredient_name=VALUES(ingredient_name), unit=VALUES(unit);

-- Seed Weekly Menu
INSERT INTO menu (id, day, meal_name) VALUES 
(1, 'Monday', 'Dal Chawal & Veggies'), 
(2, 'Tuesday', 'Kheer & Puri'), 
(3, 'Wednesday', 'Egg Curry & Rice'), 
(4, 'Thursday', 'Khichdi & Aloo Bharta'), 
(5, 'Friday', 'Veg Pulav & Soyabean Curry'), 
(6, 'Saturday', 'Jeera Rice & Dal Fry')
ON DUPLICATE KEY UPDATE meal_name=VALUES(meal_name);

-- Seed Initial Stock Refill Batches
INSERT INTO stock (id, ingredient_id, received_quantity, available_quantity, received_date) VALUES 
(1, 1, 100.0, 95.0, '2026-07-01'), 
(2, 2, 50.0, 48.0, '2026-07-01'), 
(3, 3, 20.0, 18.5, '2026-07-01'), 
(4, 4, 10.0, 9.8, '2026-07-01'), 
(5, 5, 30.0, 12.0, '2026-07-01'), 
(6, 6, 5.0, 4.9, '2026-07-01')
ON DUPLICATE KEY UPDATE available_quantity=VALUES(available_quantity);

-- Seed Daily Meal Consumption History
INSERT INTO consumption (id, ingredient_id, quantity_used, date) VALUES 
(1, 1, 5.0, '2026-07-15'), 
(2, 2, 2.0, '2026-07-15'), 
(3, 3, 1.5, '2026-07-15'), 
(4, 4, 0.2, '2026-07-15'), 
(5, 5, 18.0, '2026-07-15'), 
(6, 6, 0.1, '2026-07-15')
ON DUPLICATE KEY UPDATE quantity_used=VALUES(quantity_used);

-- Seed Attendance History
INSERT INTO attendance (id, date, class_name, present_students) VALUES 
(1, '2026-07-15', 'Class 1', 25), 
(2, '2026-07-15', 'Class 2', 28), 
(3, '2026-07-15', 'Class 3', 27), 
(4, '2026-07-15', 'Class 4', 24), 
(5, '2026-07-15', 'Class 5', 29), 
(6, '2026-07-15', 'Class 6', 22), 
(7, '2026-07-15', 'Class 7', 26), 
(8, '2026-07-15', 'Class 8', 25),
(9, '2026-07-16', 'Class 1', 26), 
(10, '2026-07-16', 'Class 2', 29), 
(11, '2026-07-16', 'Class 3', 28), 
(12, '2026-07-16', 'Class 4', 25), 
(13, '2026-07-16', 'Class 5', 30), 
(14, '2026-07-16', 'Class 6', 23), 
(15, '2026-07-16', 'Class 7', 27), 
(16, '2026-07-16', 'Class 8', 26)
ON DUPLICATE KEY UPDATE present_students=VALUES(present_students);
