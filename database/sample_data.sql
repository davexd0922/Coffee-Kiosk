INSERT INTO roles (name, description) VALUES
('ADMIN', 'Full access to POS, products, inventory, reports, and history'),
('CASHIER', 'POS and transaction history access');

INSERT INTO users (role_id, username, password_hash, full_name) VALUES
((SELECT role_id FROM roles WHERE name = 'ADMIN'), 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin User'),
((SELECT role_id FROM roles WHERE name = 'CASHIER'), 'cashier', 'b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba', 'Cashier User');

INSERT INTO categories (name, description) VALUES
('Coffee', 'Espresso-based and brewed coffee drinks'),
('Non-coffee Drinks', 'Chocolate, tea, and refreshers'),
('Pastries', 'Baked goods and snacks'),
('Add-ons', 'Extra shots, syrups, and toppings');

INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('Bean & Roast Supply', 'Mika Santos', '0917-111-2222', 'orders@beanroast.test', 'Quezon City'),
('Dairy Fresh PH', 'Jon Reyes', '0917-333-4444', 'sales@dairyfresh.test', 'Makati City'),
('Bakehouse Wholesale', 'Lea Cruz', '0917-555-6666', 'hello@bakehouse.test', 'Manila City');

INSERT INTO products (category_id, name, price, stock_quantity, image_path) VALUES
((SELECT category_id FROM categories WHERE name = 'Coffee'), 'Americano', 95.00, 40, ''),
((SELECT category_id FROM categories WHERE name = 'Coffee'), 'Cafe Latte', 125.00, 35, ''),
((SELECT category_id FROM categories WHERE name = 'Coffee'), 'Caramel Macchiato', 145.00, 30, ''),
((SELECT category_id FROM categories WHERE name = 'Non-coffee Drinks'), 'Hot Chocolate', 110.00, 25, ''),
((SELECT category_id FROM categories WHERE name = 'Non-coffee Drinks'), 'Matcha Latte', 135.00, 25, ''),
((SELECT category_id FROM categories WHERE name = 'Pastries'), 'Butter Croissant', 85.00, 20, ''),
((SELECT category_id FROM categories WHERE name = 'Pastries'), 'Chocolate Muffin', 90.00, 20, ''),
((SELECT category_id FROM categories WHERE name = 'Add-ons'), 'Extra Espresso Shot', 35.00, 50, ''),
((SELECT category_id FROM categories WHERE name = 'Add-ons'), 'Vanilla Syrup', 20.00, 50, '');

INSERT INTO ingredients (supplier_id, name, unit) VALUES
((SELECT supplier_id FROM suppliers WHERE name = 'Bean & Roast Supply'), 'Coffee Beans', 'grams'),
((SELECT supplier_id FROM suppliers WHERE name = 'Dairy Fresh PH'), 'Fresh Milk', 'ml'),
((SELECT supplier_id FROM suppliers WHERE name = 'Dairy Fresh PH'), 'Chocolate Powder', 'grams'),
((SELECT supplier_id FROM suppliers WHERE name = 'Dairy Fresh PH'), 'Matcha Powder', 'grams'),
((SELECT supplier_id FROM suppliers WHERE name = 'Bean & Roast Supply'), 'Caramel Syrup', 'ml'),
((SELECT supplier_id FROM suppliers WHERE name = 'Bean & Roast Supply'), 'Vanilla Syrup', 'ml'),
((SELECT supplier_id FROM suppliers WHERE name = 'Bakehouse Wholesale'), 'Croissant', 'pcs'),
((SELECT supplier_id FROM suppliers WHERE name = 'Bakehouse Wholesale'), 'Muffin', 'pcs');

INSERT INTO inventory (ingredient_id, quantity_on_hand, reorder_level) VALUES
((SELECT ingredient_id FROM ingredients WHERE name = 'Coffee Beans'), 5000, 1000),
((SELECT ingredient_id FROM ingredients WHERE name = 'Fresh Milk'), 8000, 2000),
((SELECT ingredient_id FROM ingredients WHERE name = 'Chocolate Powder'), 2500, 500),
((SELECT ingredient_id FROM ingredients WHERE name = 'Matcha Powder'), 1800, 400),
((SELECT ingredient_id FROM ingredients WHERE name = 'Caramel Syrup'), 1500, 300),
((SELECT ingredient_id FROM ingredients WHERE name = 'Vanilla Syrup'), 1500, 300),
((SELECT ingredient_id FROM ingredients WHERE name = 'Croissant'), 20, 5),
((SELECT ingredient_id FROM ingredients WHERE name = 'Muffin'), 20, 5);

INSERT INTO product_ingredients (product_id, ingredient_id, quantity_required) VALUES
((SELECT product_id FROM products WHERE name = 'Americano'), (SELECT ingredient_id FROM ingredients WHERE name = 'Coffee Beans'), 18),
((SELECT product_id FROM products WHERE name = 'Cafe Latte'), (SELECT ingredient_id FROM ingredients WHERE name = 'Coffee Beans'), 18),
((SELECT product_id FROM products WHERE name = 'Cafe Latte'), (SELECT ingredient_id FROM ingredients WHERE name = 'Fresh Milk'), 180),
((SELECT product_id FROM products WHERE name = 'Caramel Macchiato'), (SELECT ingredient_id FROM ingredients WHERE name = 'Coffee Beans'), 18),
((SELECT product_id FROM products WHERE name = 'Caramel Macchiato'), (SELECT ingredient_id FROM ingredients WHERE name = 'Fresh Milk'), 160),
((SELECT product_id FROM products WHERE name = 'Caramel Macchiato'), (SELECT ingredient_id FROM ingredients WHERE name = 'Caramel Syrup'), 20),
((SELECT product_id FROM products WHERE name = 'Hot Chocolate'), (SELECT ingredient_id FROM ingredients WHERE name = 'Fresh Milk'), 180),
((SELECT product_id FROM products WHERE name = 'Hot Chocolate'), (SELECT ingredient_id FROM ingredients WHERE name = 'Chocolate Powder'), 25),
((SELECT product_id FROM products WHERE name = 'Matcha Latte'), (SELECT ingredient_id FROM ingredients WHERE name = 'Fresh Milk'), 180),
((SELECT product_id FROM products WHERE name = 'Matcha Latte'), (SELECT ingredient_id FROM ingredients WHERE name = 'Matcha Powder'), 15),
((SELECT product_id FROM products WHERE name = 'Butter Croissant'), (SELECT ingredient_id FROM ingredients WHERE name = 'Croissant'), 1),
((SELECT product_id FROM products WHERE name = 'Chocolate Muffin'), (SELECT ingredient_id FROM ingredients WHERE name = 'Muffin'), 1),
((SELECT product_id FROM products WHERE name = 'Extra Espresso Shot'), (SELECT ingredient_id FROM ingredients WHERE name = 'Coffee Beans'), 9),
((SELECT product_id FROM products WHERE name = 'Vanilla Syrup'), (SELECT ingredient_id FROM ingredients WHERE name = 'Vanilla Syrup'), 15);
