"""ETL pipeline to consolidate regional order data into SQLite."""
from __future__ import annotations

import argparse
import json
import sqlite3
from pathlib import Path
from typing import Dict, Iterable

import pandas as pd


def read_orders(file_path: Path, region: str) -> pd.DataFrame:
    """Read raw order data for a region and append region identifier."""
    if not file_path.exists():
        raise FileNotFoundError(f"Missing source file: {file_path}")

    dtype_overrides = {"OrderId": str}
    df = pd.read_csv(file_path, dtype=dtype_overrides)
    df["region"] = region
    return df


def _parse_discount(value) -> float:
    """Extract the numeric discount from the JSON-like PromotionDiscount column."""
    if pd.isna(value) or value == "":
        return 0.0
    if isinstance(value, (int, float)):
        return float(value)

    text = str(value).strip()
    try:
        payload = json.loads(text)
        return float(payload.get("Amount", 0))
    except (json.JSONDecodeError, TypeError, ValueError):
        # Fall back to stripping non-numeric characters.
        cleaned = "".join(ch for ch in text if ch.isdigit() or ch == ".")
        return float(cleaned) if cleaned else 0.0


def transform_orders(df: pd.DataFrame) -> pd.DataFrame:
    """Apply business rules to clean and enrich the combined dataset."""
    working = df.copy()

    numeric_columns = ["QuantityOrdered", "ItemPrice", "batch_id", "OrderItemId"]
    for column in numeric_columns:
        working[column] = pd.to_numeric(working[column], errors="coerce")

    working["PromotionDiscount"] = working["PromotionDiscount"].apply(_parse_discount)
    working["QuantityOrdered"] = working["QuantityOrdered"].fillna(0).astype(int)
    working["ItemPrice"] = working["ItemPrice"].fillna(0.0).astype(float)
    working["batch_id"] = working["batch_id"].fillna(0).astype(int)
    working["OrderItemId"] = working["OrderItemId"].fillna(0.0).astype(float)

    working["total_sales"] = working["QuantityOrdered"] * working["ItemPrice"]
    working["net_sale"] = working["total_sales"] - working["PromotionDiscount"]

    working = working[working["net_sale"] > 0]

    # De-duplicate orders, keeping the highest net sale per OrderId.
    working = working.sort_values(["OrderId", "net_sale"], ascending=[True, False])
    working = working.drop_duplicates(subset="OrderId", keep="first")

    output_columns = [
        "OrderId",
        "OrderItemId",
        "QuantityOrdered",
        "ItemPrice",
        "PromotionDiscount",
        "total_sales",
        "net_sale",
        "region",
        "batch_id",
    ]
    return working[output_columns].reset_index(drop=True)


def load_to_sqlite(df: pd.DataFrame, database_path: Path, table_name: str = "orders") -> None:
    """Persist the transformed dataframe into a SQLite table."""
    database_path.parent.mkdir(parents=True, exist_ok=True)

    create_table_sql = f"""
    CREATE TABLE IF NOT EXISTS {table_name} (
        OrderId TEXT PRIMARY KEY,
        OrderItemId DOUBLE,
        QuantityOrdered INTEGER,
        ItemPrice REAL,
        PromotionDiscount REAL,
        total_sales REAL,
        net_sale REAL,
        region TEXT,
        batch_id INTEGER
    );
    """

    with sqlite3.connect(database_path) as conn:
        conn.execute(create_table_sql)
        df.to_sql(table_name, conn, if_exists="replace", index=False)


def get_validation_queries(table_name: str = "orders") -> Dict[str, str]:
    """Provide SQL queries to validate the loaded data."""
    return {
        "row_count": f"SELECT COUNT(*) AS row_count FROM {table_name};",
        "duplicate_orders": (
            f"SELECT OrderId, COUNT(*) AS occurrences FROM {table_name} "
            "GROUP BY OrderId HAVING COUNT(*) > 1;"
        ),
        "non_positive_net_sales": (
            f"SELECT OrderId, net_sale FROM {table_name} WHERE net_sale <= 0;"
        ),
        "sales_by_region": (
            f"SELECT region, SUM(net_sale) AS total_net_sale FROM {table_name} GROUP BY region;"
        ),
    }


def run_etl(
    source_files: Iterable[tuple[Path, str]],
    database_path: Path,
    table_name: str = "orders",
) -> pd.DataFrame:
    frames = [read_orders(path, region) for path, region in source_files]
    combined = pd.concat(frames, ignore_index=True)
    transformed = transform_orders(combined)
    load_to_sqlite(transformed, database_path, table_name=table_name)
    return transformed


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Consolidate regional order data into SQLite.")
    parser.add_argument(
        "--region-a",
        type=Path,
        default=Path(__file__).parent / "order_region_a(in).csv",
        help="Path to the Region A CSV file.",
    )
    parser.add_argument(
        "--region-b",
        type=Path,
        default=Path(__file__).parent / "order_region_b(in).csv",
        help="Path to the Region B CSV file.",
    )
    parser.add_argument(
        "--database",
        type=Path,
        default=Path(__file__).parent / "sales.db",
        help="Path to the SQLite database file to create or overwrite.",
    )
    parser.add_argument(
        "--table",
        default="orders",
        help="Name of the destination table in SQLite.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    transformed = run_etl(
        source_files=[(args.region_a, "A"), (args.region_b, "B")],
        database_path=args.database,
        table_name=args.table,
    )

    print(f"Loaded {len(transformed)} rows into {args.database}::{args.table}.")
    print("Validation queries:")
    for label, query in get_validation_queries(args.table).items():
        print(f"- {label}: {query}")


if __name__ == "__main__":
    main()
