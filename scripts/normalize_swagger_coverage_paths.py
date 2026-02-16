#!/usr/bin/env python3
"""
Adds /api/v1 prefix to paths in swagger-coverage-output JSON files
so they match the OpenAPI spec and coverage is correctly calculated.
"""
import json
import sys
from pathlib import Path

PREFIX = "/api/v1"
DIR_DEFAULT = "swagger-coverage-output"


def normalize_path(path: str) -> str:
    if not path or path.startswith(PREFIX):
        return path
    return PREFIX + (path if path.startswith("/") else "/" + path)


def process_file(filepath: Path) -> bool:
    try:
        data = json.loads(filepath.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as e:
        print(f"Warning: could not read {filepath}: {e}", file=sys.stderr)
        return False

    paths = data.get("paths")
    if not paths or not isinstance(paths, dict):
        return False

    new_paths = {}
    for key, value in paths.items():
        new_key = normalize_path(key)
        new_paths[new_key] = value

    data["paths"] = new_paths
    filepath.write_text(json.dumps(data, separators=(",", ":"), ensure_ascii=False), encoding="utf-8")
    return True


def main():
    dir_path = Path(sys.argv[1] if len(sys.argv) > 1 else DIR_DEFAULT)
    if not dir_path.is_dir():
        print(f"Directory {dir_path} not found, skipping")
        sys.exit(0)

    count = 0
    for f in dir_path.glob("*-coverage.json"):
        if f.is_file() and process_file(f):
            count += 1

    print(f"Normalized paths in {count} coverage file(s)")


if __name__ == "__main__":
    main()
