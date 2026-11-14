import contextlib
import io
import json
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from automation_scripts import populate_courses as pc


def run_main(*argv):
    output = io.StringIO()
    code = 0
    with patch("sys.argv", ["populate_courses.py", *argv]), contextlib.redirect_stdout(output):
        try:
            pc.main()
        except SystemExit as exit_status:
            code = exit_status.code
    return code, output.getvalue()


class PopulateCoursesTests(unittest.TestCase):
    def write_catalog(self, directory, entries):
        path = Path(directory) / "catalog.json"
        path.write_text(json.dumps(entries))
        return path

    def test_load_courses_reports_entries_the_table_cannot_hold(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.write_catalog(directory, {
                "CSCI-1100": {"name": "  Computer Science I "},
                "ADMN-0001": {"name": ""},
                "X" * 21: {"name": "Too long a code"},
                "MATH-2400": {"name": "N" * 256},
            })
            courses, problems = pc.load_courses(path)
        self.assertEqual(courses, [("CSCI-1100", "Computer Science I")])
        self.assertEqual(sorted(code for code, _ in problems), sorted(["ADMN-0001", "X" * 21, "MATH-2400"]))

    def test_dry_run_sends_nothing(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.write_catalog(directory, {"CSCI-1100": {"name": "Computer Science I"}})
            with patch.object(pc, "create_course") as create:
                code, output = run_main("--catalog", str(path), "--dry-run")
        create.assert_not_called()
        self.assertEqual(code, 0)
        self.assertIn("1 to send", output)

    def test_existing_courses_are_skipped_not_failures(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.write_catalog(directory, {
                "CSCI-1100": {"name": "Computer Science I"},
                "CSCI-1200": {"name": "Data Structures"},
            })
            results = {"CSCI-1100": (pc.CREATED, ""), "CSCI-1200": (pc.EXISTS, "")}
            with patch.object(pc, "create_course", side_effect=lambda url, code, name: results[code]):
                code, output = run_main("--catalog", str(path), "--url", "http://example.invalid/courses")
        self.assertEqual(code, 0)
        self.assertIn("1 created, 1 already existed, 0 failed", output)

    def test_failures_are_reported_and_set_exit_code(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.write_catalog(directory, {"CSCI-1100": {"name": "Computer Science I"}})
            with patch.object(pc, "create_course", return_value=(pc.FAILED, "HTTP 500: boom")):
                code, output = run_main("--catalog", str(path), "--url", "http://example.invalid/courses")
        self.assertEqual(code, 1)
        self.assertIn("CSCI-1100: HTTP 500: boom", output)

    def test_unreachable_server_aborts_the_run(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.write_catalog(directory, {"CSCI-1100": {"name": "Computer Science I"}})
            with patch.object(pc, "create_course", return_value=(pc.UNREACHABLE, "refused")):
                code, _ = run_main("--catalog", str(path), "--url", "http://example.invalid/courses")
        self.assertIn("can't reach", str(code))

    def test_create_course_recognizes_the_already_exists_response(self):
        body = io.BytesIO(b'{"error":"Course with code CSCI-1100 already exists"}')
        error = urllib.error.HTTPError("http://x/courses", 400, "Bad Request", {}, body)
        with patch("urllib.request.urlopen", side_effect=error):
            self.assertEqual(pc.create_course("http://x/courses", "CSCI-1100", "n"), (pc.EXISTS, ""))

    def test_create_course_reports_other_http_errors(self):
        body = io.BytesIO(b'{"error":"nope"}')
        error = urllib.error.HTTPError("http://x/courses", 500, "Server Error", {}, body)
        with patch("urllib.request.urlopen", side_effect=error):
            status, detail = pc.create_course("http://x/courses", "CSCI-1100", "n")
        self.assertEqual(status, pc.FAILED)
        self.assertIn("HTTP 500", detail)


if __name__ == "__main__":
    unittest.main()
