"""Create a course for every entry in catalog.json through the backend's POST /courses endpoint.

catalog.json maps COURSE-CODE (e.g. CSCI-1100) to an entry with a "name" (and other fields the
courses table has no column for, such as the description). Each entry is sent as
{"code": ..., "name": ...}.

The endpoint only creates courses: if a code already exists it answers 400 "already exists" and
leaves the stored name alone. Those are counted as skipped, not failures, so the script is safe to
re-run. That also means a course created earlier by a backtest upload (whose name is just its
code) keeps that name; run this script before uploading backtests, or fix those names separately.

The endpoint URL comes from, in order: --url, the COURSES_API_URL environment variable, or
COURSES_API_URL in backend/.env, defaulting to http://127.0.0.1:8080/courses.

Usage:
  python populate_courses.py [--catalog FILE] [--url URL] [--workers N] [--dry-run]
"""

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = REPO_ROOT / "backend" / ".env"
DEFAULT_CATALOG = Path(__file__).resolve().parent / "catalog.json"
# 127.0.0.1 rather than localhost: on Windows, localhost tries IPv6 first and Docker's IPv6 port forward hangs
DEFAULT_URL = "http://127.0.0.1:8080/courses"

# Column limits from the course table: code VARCHAR(20), name VARCHAR(255)
MAX_CODE_LENGTH = 20
MAX_NAME_LENGTH = 255

CREATED, EXISTS, FAILED, UNREACHABLE = "created", "exists", "failed", "unreachable"


def read_env_var(name, env_file=ENV_FILE):
    """Looks up name in the environment, then in a KEY=VALUE .env file. Returns None if unset."""
    if os.environ.get(name):
        return os.environ[name]
    if not env_file.is_file():
        return None
    for line in env_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        if key.strip() == name:
            return value.strip().strip("\"'") or None
    return None


def load_courses(catalog_path):
    """Returns (courses, problems): courses is a sorted list of (code, name); problems explains skipped entries."""
    catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    courses, problems = [], []
    for code, entry in sorted(catalog.items()):
        name = (entry.get("name") or "").strip() if isinstance(entry, dict) else ""
        if not name:
            problems.append((code, "no name"))
        elif len(code) > MAX_CODE_LENGTH:
            problems.append((code, f"code longer than {MAX_CODE_LENGTH} characters"))
        elif len(name) > MAX_NAME_LENGTH:
            problems.append((code, f"name longer than {MAX_NAME_LENGTH} characters"))
        else:
            courses.append((code, name))
    return courses, problems


def create_course(url, code, name):
    """POSTs one course. Returns (status, detail) where status is CREATED, EXISTS, FAILED or UNREACHABLE."""
    body = json.dumps({"code": code, "name": name}).encode("utf-8")
    request = urllib.request.Request(
        url, data=body, method="POST", headers={"Content-Type": "application/json"}
    )
    try:
        with urllib.request.urlopen(request, timeout=60):
            return CREATED, ""
    except urllib.error.HTTPError as err:
        detail = err.read().decode("utf-8", errors="replace").strip()
        if err.code == 400 and "already exists" in detail:
            return EXISTS, ""
        return FAILED, f"HTTP {err.code}: {detail[:200]}"
    except urllib.error.URLError as err:
        return UNREACHABLE, str(err.reason)
    except (TimeoutError, ConnectionError) as err:
        # The server may or may not have created it; a re-run reports it as already existing if it did
        return FAILED, f"{type(err).__name__}: {err}"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--catalog", type=Path, default=DEFAULT_CATALOG)
    ap.add_argument("--url", default=read_env_var("COURSES_API_URL") or DEFAULT_URL)
    ap.add_argument("--workers", type=int, default=4, help="concurrent requests (default 4)")
    ap.add_argument("--dry-run", action="store_true", help="validate the catalog and show counts without sending anything")
    args = ap.parse_args()

    if not args.catalog.is_file():
        ap.error(f"catalog not found: {args.catalog}")

    courses, problems = load_courses(args.catalog)
    print(f"{len(courses) + len(problems)} catalog entries, {len(problems)} invalid, "
          f"{len(courses)} to send -> {args.url}")
    for code, problem in problems:
        print(f"INVALID {code}: {problem}")

    if args.dry_run or not courses:
        return

    counts = {CREATED: 0, EXISTS: 0, FAILED: 0}
    failed = []
    with ThreadPoolExecutor(max_workers=max(1, args.workers)) as pool:
        futures = {pool.submit(create_course, args.url, code, name): code for code, name in courses}
        for done, future in enumerate(as_completed(futures), 1):
            code = futures[future]
            status, detail = future.result()
            if status == UNREACHABLE:
                pool.shutdown(wait=True, cancel_futures=True)
                sys.exit(f"\ncan't reach {args.url} ({detail}); is the backend running? "
                         f"{counts[CREATED]} created, {counts[EXISTS]} already existed before stopping; re-run to resume")
            counts[status] += 1
            if status == FAILED:
                failed.append((code, detail))
                print(f"[{done}/{len(courses)}] FAILED {code}: {detail}")
            elif done % 250 == 0 or done == len(courses):
                print(f"[{done}/{len(courses)}] {counts[CREATED]} created, {counts[EXISTS]} already existed")

    print(f"\n{counts[CREATED]} created, {counts[EXISTS]} already existed, "
          f"{counts[FAILED]} failed, {len(problems)} invalid")
    for code, detail in failed:
        print(f"  {code}: {detail}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
