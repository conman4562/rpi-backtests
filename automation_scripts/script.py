"""Upload every backtest PDF in a folder to the backend's POST /backtests endpoint.

The endpoint parses the file name (COURSE_PROFESSOR_YYYYMM_TESTTYPE_HASH.pdf), creates the
course if needed, stores the PDF in R2 and inserts the back_test row, so this script only has
to send the files. Expected layout:  <backtests_dir>/<COURSE>/<file name>.pdf

The endpoint isn't idempotent (uploading a file twice creates two rows), so each successful
upload is recorded in uploaded.log next to this script and skipped on later runs. Delete that
file, or pass --no-resume, to upload everything again.

The backtests folder comes from, in order: the command-line argument, the BACKTESTS_DIR
environment variable, or BACKTESTS_DIR in backend/.env. A relative path is taken from the repo
root. The endpoint URL comes from BACKTESTS_API_URL the same way, defaulting to
http://127.0.0.1:8080/backtests.

Usage:
  python script.py [backtests_dir] [--url URL] [--uploader NAME] [--dry-run] [--no-resume]
"""

import argparse
import os
import sys
import urllib.error
import urllib.request
import uuid
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = REPO_ROOT / "backend" / ".env"
LOG_FILE = Path(__file__).resolve().parent / "uploaded.log"
# 127.0.0.1 rather than localhost: on Windows, localhost tries IPv6 first and Docker's IPv6 port forward hangs
DEFAULT_URL = "http://127.0.0.1:8080/backtests"


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


def resolve_backtests_dir(arg):
    value = arg if arg is not None else read_env_var("BACKTESTS_DIR")
    if value is None:
        raise SystemExit(f"No backtests folder: pass one as an argument or set BACKTESTS_DIR in {ENV_FILE}")
    path = Path(value)
    return path if path.is_absolute() else REPO_ROOT / path


def read_uploaded():
    if not LOG_FILE.is_file():
        return set()
    return set(LOG_FILE.read_text(encoding="utf-8").splitlines())


def multipart_body(pdf, fields):
    """Builds a multipart/form-data body with the PDF as the 'file' part."""
    boundary = uuid.uuid4().hex
    chunks = []
    for name, value in fields.items():
        chunks.append(
            f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode()
        )
    chunks.append(
        (
            f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{pdf.name}"\r\n'
            "Content-Type: application/pdf\r\n\r\n"
        ).encode()
    )
    chunks.append(pdf.read_bytes())
    chunks.append(f"\r\n--{boundary}--\r\n".encode())
    return b"".join(chunks), f"multipart/form-data; boundary={boundary}"


def upload(pdf, url, fields):
    """POSTs one PDF. Returns None on success or an error message. Raises URLError if the server is unreachable."""
    body, content_type = multipart_body(pdf, fields)
    request = urllib.request.Request(url, data=body, method="POST", headers={"Content-Type": content_type})
    try:
        with urllib.request.urlopen(request, timeout=60):
            return None
    except urllib.error.HTTPError as err:
        detail = err.read().decode("utf-8", errors="replace").strip()
        return f"HTTP {err.code}: {detail[:200]}"
    except (TimeoutError, ConnectionError) as err:
        # The server may or may not have stored the file; check before re-uploading it
        return f"{type(err).__name__}: {err}"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("backtests_dir", nargs="?", help="defaults to BACKTESTS_DIR from the environment or backend/.env")
    ap.add_argument("--url", default=read_env_var("BACKTESTS_API_URL") or DEFAULT_URL)
    ap.add_argument("--uploader", default="anonymous")
    ap.add_argument("--dry-run", action="store_true", help="list what would be uploaded without sending anything")
    ap.add_argument("--no-resume", action="store_true", help="upload files even if uploaded.log says they're done")
    args = ap.parse_args()

    backtests_dir = resolve_backtests_dir(args.backtests_dir)
    if not backtests_dir.is_dir():
        ap.error(f"not a directory: {backtests_dir}")

    pdfs = sorted(backtests_dir.glob("*/*.pdf"))
    done = set() if args.no_resume else read_uploaded()
    todo = [pdf for pdf in pdfs if f"{pdf.parent.name}/{pdf.name}" not in done]
    print(f"{len(pdfs)} PDFs found, {len(pdfs) - len(todo)} already uploaded, {len(todo)} to upload -> {args.url}")

    if args.dry_run:
        return

    fields = {"uploader": args.uploader, "anonymous": "false"}
    failed = []
    with LOG_FILE.open("a", encoding="utf-8", newline="\n") as log:
        for i, pdf in enumerate(todo, 1):
            key = f"{pdf.parent.name}/{pdf.name}"
            try:
                error = upload(pdf, args.url, fields)
            except urllib.error.URLError as err:
                sys.exit(f"\ncan't reach {args.url} ({err.reason}); is the backend running? "
                         f"{i - 1 - len(failed)} uploaded this run, re-run to resume")
            if error:
                failed.append((key, error))
                print(f"[{i}/{len(todo)}] FAILED {key}: {error}")
            else:
                log.write(key + "\n")
                log.flush()
                print(f"[{i}/{len(todo)}] ok {key}")

    print(f"\n{len(todo) - len(failed)} uploaded, {len(failed)} failed")
    for key, error in failed:
        print(f"  {key}: {error}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
