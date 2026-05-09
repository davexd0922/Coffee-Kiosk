ALTER TABLE products
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE products
DROP CONSTRAINT IF EXISTS products_status_check;

ALTER TABLE products
ADD CONSTRAINT products_status_check
CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'UNAVAILABLE'));

UPDATE products
SET status = CASE
    WHEN stock_quantity <= 0 THEN 'OUT_OF_STOCK'
    ELSE 'AVAILABLE'
END
WHERE status IS NULL OR status = '';
