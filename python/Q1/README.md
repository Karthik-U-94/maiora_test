# Regional Orders ETL

## How to Run
- **Prepare environment**
  - Install Python 3.9+
  - Install dependencies:
    ```bash
    pip install pandas
    ```
- **Execute ETL**
  - From the project directory:
    ```bash
    python etl.py --region-a "order_region_a(in).csv" --region-b "order_region_b(in).csv" --database sales.db --table orders
    ```
  - Defaults assume CSVs sit alongside `etl.py`; omit flags if that’s the case.
- **Inspect database**
  - SQLite DB `sales.db` is created if missing. Example checks:
    ```bash
    sqlite3 sales.db "SELECT COUNT(*) FROM orders;"
    sqlite3 sales.db "SELECT * FROM orders LIMIT 5;"
    ```

## Assumptions and Decisions
- **Schema**: `OrderItemId` stored as floating point (`DOUBLE`) per request; `OrderId` acts as primary key.
- **Discount parsing**: `PromotionDiscount` values are JSON strings like `{ "Amount": "10" }`; script reads `Amount`. Non-JSON fallback strips numeric characters.
- **Duplicates**: When both regions supply same `OrderId`, only the first occurrence (Region A by default) remains after dropping duplicates to satisfy the "no duplicate OrderId" rule.
- **Filtering**: Orders with `net_sale <= 0` are removed.
- **SQLite usage**: Table recreated on each run via `IF EXISTS REPLACE` to ensure idempotent ETL loads.
