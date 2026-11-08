package edu.rpi.backtests.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.models.Course;
import edu.rpi.backtests.models.Semester;
import edu.rpi.backtests.models.TestState;
import edu.rpi.backtests.repositories.BackTestRepository;
import edu.rpi.backtests.repositories.CourseRepository;
import edu.rpi.backtests.storage.CloudflareR2Client;

@Service
public class BackTestService {
  private final BackTestRepository backTests;
  private final CourseRepository courses;
  private final CloudflareR2Client r2;

  public BackTestService(BackTestRepository backTests, CourseRepository courses, CloudflareR2Client r2) {
    this.backTests = backTests;
    this.courses = courses;
    this.r2 = r2;
  }

  public List<BackTest> getTestsForCourse(UUID courseId) {
    return backTests.findByCourseId(courseId);
  }

  public List<BackTest> getTestsForCourse(String courseCode) {
    return backTests.findByCourseCode(courseCode);
  }

  public String getDownloadUrl(UUID testId) {
    BackTest test = backTests.findById(testId)
        .orElseThrow(() -> new NoSuchElementException("No backtest with id: " + testId));

    return r2.getDownloadUrl((test.getFileKey()));
  }

  /**
   * Creates a backtest from an uploaded file whose name follows
   * COURSE_PROFESSOR_YYYYMM_TESTTYPE_STATE_ID.pdf, e.g.
   * MATH-4800_William-D-Henshaw_202009_EXAM-1_SOLUTION_5d109c65.pdf.
   * The file is stored in R2 under COURSE_<backtest id>.
   */
  public BackTest upload(MultipartFile file, String uploader, Boolean anonymous) throws IOException {
    ParsedFileName parsed = parseFileName(file.getOriginalFilename());

    // TODO: course names aren't in the file name; the code stands in until a real
    // name is set
    Course course = courses.findByCode(parsed.courseCode())
        .orElseGet(() -> courses.save(new Course(parsed.courseCode(), parsed.courseCode())));

    BackTest test = new BackTest(course, parsed.semester(), parsed.professor(), parsed.state(),
        parsed.year(), uploader, anonymous, parsed.testType());

    r2.putObject(file.getOriginalFilename(), test.getFileKey(), file.getContentType(), file.getBytes());
    return backTests.save(test);
  }

  private record ParsedFileName(String courseCode, String professor, Integer year, Semester semester,
      String testType, TestState state) {
  }

  private ParsedFileName parseFileName(String fileName) {
    if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
      throw new IllegalArgumentException("Expected a PDF filename");
    }
    String base = fileName.substring(0, fileName.length() - 4);
    String[] parts = base.split("_", -1);
    if (parts.length != 6) {
      throw new IllegalArgumentException(
          "Expected COURSE_PROFESSOR_YYYYMM_TESTTYPE_STATE_ID, got: " + fileName);
    }
    for (String part : parts) {
      if (part.isBlank()) {
        throw new IllegalArgumentException("Filename fields must not be empty: " + fileName);
      }
    }
    // parts[5] identifies the source file; database IDs are generated separately.

    String courseCode = parts[0];
    if (isUnknown(courseCode)) {
      throw new IllegalArgumentException("Course is UNKNOWN; review this file before uploading");
    }
    if (!courseCode.matches("[A-Z]{2,4}-[0-9]{4}")) {
      throw new IllegalArgumentException("Expected a course code such as CSCI-1100, got: " + courseCode);
    }

    TestState state = switch (parts[4].toUpperCase(Locale.ROOT)) {
      case "BLANK" -> TestState.BLANK;
      case "SOLUTION" -> TestState.SOLUTION;
      case "USERFILLED", "FILLED" -> TestState.FILLED;
      case "UNKNOWN" -> throw new IllegalArgumentException("State is UNKNOWN; review this file before uploading");
      default -> throw new IllegalArgumentException("Unknown exam state: " + parts[4]);
    };

    String professor = null;
    if (!isUnknown(parts[1])) {
      String[] name = parts[1].split("-", 3);
      if (name.length != 3 || name[0].isBlank() || name[2].isBlank()
          || !name[1].matches("@|[A-Z]{1,3}")) {
        throw new IllegalArgumentException("Expected professor as Firstname-Middleinitial-Lastname, got: " + parts[1]);
      }
      professor = name[0] + (name[1].equals("@") ? " " : " " + name[1] + " ") + name[2];
    }

    Integer year = null;
    Semester semester = null;
    if (!isUnknown(parts[2])) {
      if (!parts[2].matches("[0-9]{6}")) {
        throw new IllegalArgumentException("Expected term as YYYYMM, got: " + parts[2]);
      }
      year = Integer.parseInt(parts[2].substring(0, 4));
      semester = switch (parts[2].substring(4)) {
        case "01" -> Semester.SPRING;
        case "05" -> Semester.SUMMER;
        case "09" -> Semester.FALL;
        default -> throw new IllegalArgumentException("Unknown term month in: " + parts[2]);
      };
    }

    String testType = isUnknown(parts[3]) ? null : parts[3];

    return new ParsedFileName(courseCode, professor, year, semester, testType, state);
  }

  private boolean isUnknown(String value) {
    return value.equalsIgnoreCase("unknown");
  }
}
