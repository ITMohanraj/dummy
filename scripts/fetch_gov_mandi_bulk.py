"""
Bulk Mandi Government Data Fetcher for CropDeal
Fetches live daily mandi rates from data.gov.in (Resource ID: 9ef84268-d588-465a-a308-a864a43d0070)
and ingests into MySQL price_db.mandi_price_records
"""

import urllib.request
import json
import time
import subprocess
import os
import sys
from datetime import date, datetime

API_URL = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070"
API_KEY = "579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b"
TARGET_RECORDS = 1000
PAGE_SIZE = 10

def escape_sql(val):
    if val is None:
        return "NULL"
    s = str(val).replace("\\", "\\\\").replace("'", "''")
    return f"'{s}'"

def fetch_all():
    print(f"[*] Starting fetch of up to {TARGET_RECORDS} live government mandi records from data.gov.in...", flush=True)
    records = []
    offset = 0
    consecutive_errors = 0

    while len(records) < TARGET_RECORDS:
        url = f"{API_URL}?api-key={API_KEY}&format=json&offset={offset}&limit={PAGE_SIZE}"
        req = urllib.request.Request(url, headers={
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 CropDeal/1.0',
            'Accept': 'application/json'
        })

        success = False
        for attempt in range(4):
            try:
                with urllib.request.urlopen(req, timeout=10) as resp:
                    payload = json.loads(resp.read().decode('utf-8'))
                    batch = payload.get('records', [])
                    if not batch:
                        print(f"[*] No more records returned at offset={offset}.", flush=True)
                        return records

                    records.extend(batch)
                    print(f"[+] Offset {offset:4d} -> Fetched {len(batch)} items. Total: {len(records)}/{TARGET_RECORDS}", flush=True)
                    offset += PAGE_SIZE
                    success = True
                    consecutive_errors = 0
                    time.sleep(0.3) # Rate limit pacing
                    break
            except Exception as e:
                err_str = str(e)
                wait_sec = 2 * (attempt + 1)
                print(f"[!] Warning: Offset {offset} attempt #{attempt+1} encountered: {err_str}. Pausing {wait_sec}s...", flush=True)
                time.sleep(wait_sec)

        if not success:
            consecutive_errors += 1
            if consecutive_errors >= 4:
                print(f"[X] Stopping after {consecutive_errors} consecutive failures. Progress so far: {len(records)} records.", flush=True)
                break
            offset += PAGE_SIZE

    return records

def ingest_to_mysql(records):
    if not records:
        print("[!] No records to insert.", flush=True)
        return

    print(f"[*] Preparing MySQL batch insert for {len(records)} records...", flush=True)
    
    today = date.today().isoformat()
    now = datetime.now().isoformat()

    sql_statements = ["DELETE FROM mandi_price_records;"]

    for r in records:
        commodity = r.get("commodity", "Unknown")
        normalized = commodity.strip().lower()
        variety = r.get("variety", "Standard")
        grade = r.get("grade", "FAQ")
        state = r.get("state", "National")
        district = r.get("district", "General")
        market = r.get("market", "APMC Mandi")
        
        try:
            min_p = float(r.get("min_price", 2000))
        except:
            min_p = 2000.0
            
        try:
            max_p = float(r.get("max_price", 3000))
        except:
            max_p = 3000.0
            
        try:
            modal_p = float(r.get("modal_price", 2500))
        except:
            modal_p = 2500.0

        price_per_kg = round(modal_p / 100.0, 2)

        sql = f"INSERT INTO mandi_price_records (commodity, normalized_commodity, variety, grade, state, district, market, min_price, max_price, modal_price, source_unit, converted_price_per_kg, record_date, synced_at) VALUES ({escape_sql(commodity)}, {escape_sql(normalized)}, {escape_sql(variety)}, {escape_sql(grade)}, {escape_sql(state)}, {escape_sql(district)}, {escape_sql(market)}, {min_p}, {max_p}, {modal_p}, 'QUINTAL', {price_per_kg}, {escape_sql(today)}, {escape_sql(now)});"
        sql_statements.append(sql)

    full_sql = "\n".join(sql_statements)

    print(f"[*] Streaming SQL batch into container cropdeal-price-db...", flush=True)
    res = subprocess.run(
        ["docker", "exec", "-i", "cropdeal-price-db", "mysql", "-uroot", "-prootpassword", "price_db"],
        input=full_sql,
        capture_output=True,
        text=True
    )
    if res.returncode == 0:
        print(f"[OK] SUCCESS! Ingested {len(records)} live government mandi records into price_db.mandi_price_records!", flush=True)
    else:
        print(f"[X] MySQL Import error: {res.stderr}", flush=True)

if __name__ == "__main__":
    recs = fetch_all()
    print(f"[*] Total records successfully retrieved: {len(recs)}", flush=True)
    ingest_to_mysql(recs)
