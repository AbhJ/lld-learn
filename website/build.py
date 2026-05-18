#!/usr/bin/env python3
# Copyright (c) 2026 Abhijay (abj). All rights reserved.
# This source code is proprietary and confidential. Unauthorized copying,
# modification, distribution, or use of this file, via any medium, is
# strictly prohibited without prior written permission of the author.
"""
Scans problems/ directory and generates data.json for the website.
Run: python3 website/build.py (from the repo root)
  or: python3 build.py (from within website/)
"""

import json
import os
import re
import sys

# Determine paths
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
PROBLEMS_DIR = os.path.join(REPO_ROOT, "problems")
OUTPUT_FILE = os.path.join(SCRIPT_DIR, "data.json")

# Difficulty heuristics based on problem complexity indicators
DIFFICULTY_MAP = {
    # Simple state machines / single entity
    "05-traffic-signal": "Easy",
    "06-snake-and-ladder": "Easy",
    "07-tic-tac-toe": "Easy",
    "04-vending-machine": "Easy",
    "17-logging-framework": "Easy",
    "27-rate-limiter": "Easy",
    "28-url-shortener": "Easy",
    "35-object-pool": "Easy",
    "44-music-player": "Easy",
    "45-shopping-cart": "Easy",
    # Medium complexity
    "01-parking-lot": "Medium",
    "02-elevator-system": "Medium",
    "03-library-management": "Medium",
    "09-hotel-management": "Medium",
    "10-movie-ticket-booking": "Medium",
    "11-atm-machine": "Medium",
    "12-car-rental-system": "Medium",
    "16-notification-system": "Medium",
    "18-cache-system": "Medium",
    "19-pub-sub-system": "Medium",
    "20-task-scheduler": "Medium",
    "21-file-system": "Medium",
    "23-splitwise": "Medium",
    "29-key-value-store": "Medium",
    "30-connection-pool": "Medium",
    "33-event-bus": "Medium",
    "34-circuit-breaker": "Medium",
    "36-command-pattern-editor": "Medium",
    "37-state-machine": "Medium",
    "38-restaurant-ordering": "Medium",
    "40-inventory-management": "Medium",
    "42-calendar-system": "Medium",
    "43-document-editor": "Medium",
    "46-order-management": "Medium",
    "47-conference-room-booking": "Medium",
    "48-pizza-delivery": "Medium",
    "49-vehicle-tracking": "Medium",
    "50-card-game-blackjack": "Medium",
    # High complexity - multiple subsystems, concurrency, distributed
    "08-chess-game": "Hard",
    "13-food-delivery": "Hard",
    "14-ride-sharing": "Hard",
    "15-social-media-feed": "Hard",
    "22-spreadsheet": "Hard",
    "24-online-auction": "Hard",
    "25-stock-exchange": "Hard",
    "26-payment-gateway": "Hard",
    "31-thread-pool": "Hard",
    "32-producer-consumer": "Hard",
    "39-airline-reservation": "Hard",
    "41-chat-application": "Hard",
}


def extract_patterns(readme_content):
    """Extract design patterns from the README's pattern table."""
    patterns = []
    # Look for pattern table rows: | PatternName | ... | ... |
    table_pattern = re.compile(
        r"^\|\s*([A-Za-z /\-]+?)\s*\|.*\|.*\|", re.MULTILINE
    )
    in_pattern_section = False
    for line in readme_content.split("\n"):
        if "Design Patterns Used" in line or "Pattern" in line and "Where Used" in line:
            in_pattern_section = True
            continue
        if in_pattern_section:
            if line.startswith("|") and "---" not in line and "Pattern" not in line:
                match = re.match(r"^\|\s*([A-Za-z /\-]+?)\s*\|", line)
                if match:
                    pattern_name = match.group(1).strip()
                    if pattern_name and pattern_name.lower() not in ("pattern", "---"):
                        patterns.append(pattern_name)
            elif not line.startswith("|") and line.strip() and not line.startswith(" "):
                in_pattern_section = False
    return patterns


def extract_title(readme_content, dir_name):
    """Extract title from first H1 heading or derive from directory name."""
    match = re.search(r"^#\s+(.+)$", readme_content, re.MULTILINE)
    if match:
        return match.group(1).strip()
    # Fallback: derive from directory name
    name_part = dir_name.split("-", 1)[1] if "-" in dir_name else dir_name
    return name_part.replace("-", " ").title()


# Strip a leading /* ... */ block comment if it contains "Copyright".
# Matches the standard 7-line header at the top of every Java file in this repo
# without touching legitimate doc comments mid-file.
_COPYRIGHT_HEADER_RE = re.compile(
    r"\A\s*/\*[\s\S]*?Copyright[\s\S]*?\*/\s*\n?"
)


def strip_copyright_header(content):
    """Remove the leading copyright /* ... */ block from Java source for display."""
    return _COPYRIGHT_HEADER_RE.sub("", content, count=1)


def get_java_files(problem_dir):
    """Get all .java files from a problem's naive/ and optimized/ directories."""
    files = []

    for variant in ["naive", "optimized", "concurrent"]:
        variant_dir = os.path.join(problem_dir, variant)
        if not os.path.isdir(variant_dir):
            continue
        # Walk through all subdirectories (model/, service/, strategy/, etc.)
        for root, dirs, filenames in sorted(os.walk(variant_dir)):
            for fname in sorted(filenames):
                if fname.endswith(".java"):
                    filepath = os.path.join(root, fname)
                    # Create display name like "naive/service/ParkingLot.java"
                    rel_path = os.path.relpath(filepath, problem_dir)
                    try:
                        with open(filepath, "r", encoding="utf-8") as f:
                            content = f.read()
                        content = strip_copyright_header(content)
                        files.append({"name": rel_path, "content": content})
                    except (IOError, UnicodeDecodeError) as e:
                        print(f"  Warning: Could not read {filepath}: {e}")

    # Fallback: check for src/ or flat .java files (legacy)
    if not files:
        src_dir = os.path.join(problem_dir, "src")
        search_dir = src_dir if os.path.isdir(src_dir) else problem_dir
        for fname in sorted(os.listdir(search_dir)):
            if fname.endswith(".java"):
                filepath = os.path.join(search_dir, fname)
                try:
                    with open(filepath, "r", encoding="utf-8") as f:
                        content = f.read()
                    content = strip_copyright_header(content)
                    files.append({"name": fname, "content": content})
                except (IOError, UnicodeDecodeError) as e:
                    print(f"  Warning: Could not read {filepath}: {e}")
    return files


def process_problem(dir_name):
    """Process a single problem directory and return its data."""
    problem_dir = os.path.join(PROBLEMS_DIR, dir_name)

    if not os.path.isdir(problem_dir):
        return None

    # Extract number and id
    match = re.match(r"^(\d+)-(.+)$", dir_name)
    if not match:
        return None

    number = int(match.group(1))
    problem_id = dir_name

    # Read README.md
    readme_path = os.path.join(problem_dir, "README.md")
    readme_content = ""
    if os.path.exists(readme_path):
        try:
            with open(readme_path, "r", encoding="utf-8") as f:
                readme_content = f.read()
        except (IOError, UnicodeDecodeError) as e:
            print(f"  Warning: Could not read {readme_path}: {e}")

    # Read VARIATIONS.md (may not exist)
    variations_path = os.path.join(problem_dir, "VARIATIONS.md")
    variations_content = None
    if os.path.exists(variations_path):
        try:
            with open(variations_path, "r", encoding="utf-8") as f:
                variations_content = f.read()
        except (IOError, UnicodeDecodeError):
            pass

    # Extract metadata
    title = extract_title(readme_content, dir_name)
    patterns = extract_patterns(readme_content)
    difficulty = DIFFICULTY_MAP.get(dir_name, "Medium")

    # Get Java files
    java_files = get_java_files(problem_dir)

    return {
        "id": problem_id,
        "number": number,
        "title": title,
        "difficulty": difficulty,
        "patterns": patterns,
        "readme": readme_content,
        "variations": variations_content,
        "files": java_files,
    }


def main():
    if not os.path.exists(PROBLEMS_DIR):
        print(f"Error: Problems directory not found at {PROBLEMS_DIR}")
        sys.exit(1)

    print(f"Scanning problems in: {PROBLEMS_DIR}")

    # Get all problem directories sorted
    dirs = sorted(
        d
        for d in os.listdir(PROBLEMS_DIR)
        if os.path.isdir(os.path.join(PROBLEMS_DIR, d)) and re.match(r"^\d+-", d)
    )

    print(f"Found {len(dirs)} problem directories")

    problems = []
    for dir_name in dirs:
        print(f"  Processing: {dir_name}")
        problem_data = process_problem(dir_name)
        if problem_data:
            problems.append(problem_data)

    # Write output
    output = {"problems": problems}
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)

    print(f"\nGenerated {OUTPUT_FILE}")
    print(f"Total problems: {len(problems)}")
    total_files = sum(len(p["files"]) for p in problems)
    print(f"Total Java files: {total_files}")

    # Summary of patterns
    all_patterns = set()
    for p in problems:
        all_patterns.update(p["patterns"])
    print(f"Unique patterns found: {len(all_patterns)}")
    for pat in sorted(all_patterns):
        count = sum(1 for p in problems if pat in p["patterns"])
        print(f"  - {pat}: {count} problems")


if __name__ == "__main__":
    main()
