import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from automation_scripts import script


class UploadScriptTests(unittest.TestCase):
    def test_holds_unresolved_files_without_uploading_or_recording_them(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            course = root / "archive-course"
            course.mkdir()
            valid = course / "CSCI-1100_Uzma-@-Mushtaque_202301_QUIZ-1_USERFILLED_12345678.pdf"
            held = [
                course / "UNKNOWN_UNKNOWN_202301_QUIZ-1_BLANK_22345678.pdf",
                course / "CSCI-1100_UNKNOWN_202301_QUIZ-1_UNKNOWN_32345678.pdf",
                course / "CSCI-1100_UNKNOWN_202313_QUIZ-1_BLANK_42345678.pdf",
                course / "CSCI-1100_UNKNOWN_202301_QUIZ-1_52345678.pdf",
            ]
            for pdf in [valid, *held]:
                pdf.write_bytes(b"%PDF-1.4\n")
            log = root / "uploaded.log"
            output = io.StringIO()
            with patch.object(script, "LOG_FILE", log), \
                    patch.object(script, "upload", return_value=None) as upload, \
                    patch("sys.argv", ["script.py", str(root), "--url", "http://example.invalid/backtests"]), \
                    contextlib.redirect_stdout(output):
                with self.assertRaises(SystemExit) as exit_status:
                    script.main()
            self.assertEqual(exit_status.exception.code, 0)
            self.assertEqual([call.args[0] for call in upload.call_args_list], [valid])
            self.assertEqual(log.read_text().splitlines(), [f"archive-course/{valid.name}"])
            self.assertIn("4 held for review", output.getvalue())
            for pdf in held:
                self.assertIn(pdf.name, output.getvalue())

    def test_dry_run_preserves_resume_log_and_reports_held_files(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            course = root / "CSCI-1100"
            course.mkdir()
            names = [
                "CSCI-1100_UNKNOWN_UNKNOWN_UNKNOWN_BLANK_12345678.pdf",
                "CSCI-1100_UNKNOWN_202301_PRACTICE-EXAM-3-A_SOLUTION_22345678.pdf",
                "CSCI-1100_UNKNOWN_202301_QUIZ-1_UNKNOWN_32345678.pdf",
            ]
            for name in names:
                (course / name).write_bytes(b"%PDF-1.4\n")
            log = root / "uploaded.log"
            original_log = f"CSCI-1100/{names[0]}\n"
            log.write_text(original_log)
            output = io.StringIO()
            with patch.object(script, "LOG_FILE", log), \
                    patch.object(script, "upload") as upload, \
                    patch("sys.argv", ["script.py", str(root), "--dry-run", "--url", "http://example.invalid/backtests"]), \
                    contextlib.redirect_stdout(output):
                script.main()
            upload.assert_not_called()
            self.assertEqual(log.read_text(), original_log)
            self.assertIn("1 already uploaded", output.getvalue())
            self.assertIn("1 held for review", output.getvalue())
            self.assertIn("1 to upload", output.getvalue())


if __name__ == "__main__":
    unittest.main()
