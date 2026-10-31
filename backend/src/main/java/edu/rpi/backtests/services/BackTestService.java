package edu.rpi.backtests.services;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import edu.rpi.backtests.model.BackTest;
import edu.rpi.backtests.model.Course;
import edu.rpi.backtests.model.Semester;
import edu.rpi.backtests.model.TestState;
import edu.rpi.backtests.repositories.BackTestRepository;
import edu.rpi.backtests.repositories.CourseRepository;
import edu.rpi.backtests.storage.CloudflareR2Client;


@Service
public class BackTestService {
   private final BackTestRepository backTests;
   private final CourseRepository courses;
   private final CloudflareR2Client r2;

   public BackTestService(BackTestRepository backTests, CourseRepository courses, CloudflareR2Client r2){
      this.backTests = backTests;
      this.courses = courses;
      this.r2 = r2;
   }

   public List<BackTest> getTestsForCourse(UUID courseId){
      return backTests.findByCourseId(courseId);
   }

   public String getDownloadUrl(UUID testId){
      BackTest test = backTests.findById(testId)
         .orElseThrow(() -> new NoSuchElementException("No backtest with id: " + testId));
      
         return r2.getDownloadUrl((test.getFileKey()));
   }

   /**
    * Creates a backtest from an uploaded file whose name follows
    * COURSE_PROFESSOR_YYYYMM_TESTTYPE_HASH.pdf, e.g. ENGR-2090_A-S-Shahsavari_202009_3-SOLUTIONS_28b1.pdf.
    * The file is stored in R2 under COURSE_<backtest id>.
    */
   public BackTest upload(MultipartFile file, String uploader, Boolean anonymous) throws IOException {
      ParsedFileName parsed = parseFileName(file.getOriginalFilename());

      // TODO: course names aren't in the file name; the code stands in until a real name is set
      Course course = courses.findByCode(parsed.courseCode())
         .orElseGet(() -> courses.save(new Course(parsed.courseCode(), parsed.courseCode())));

      BackTest test = new BackTest(course, parsed.semester(), parsed.professor(), parsed.state(),
         parsed.year(), uploader, anonymous, parsed.testType());

      r2.putObject(file.getOriginalFilename(), test.getFileKey(), file.getBytes());
      return backTests.save(test);
   }

   private record ParsedFileName(String courseCode, String professor, Integer year, Semester semester,
                                 String testType, TestState state) {}

   private ParsedFileName parseFileName(String fileName) {
      if (fileName == null) {
         throw new IllegalArgumentException("Uploaded file has no name");
      }
      String base = fileName.replaceFirst("\\.[^.]+$", "");   // drop the extension
      String[] parts = base.split("_");
      if (parts.length != 5) {
         throw new IllegalArgumentException(
            "Expected COURSE_PROFESSOR_YYYYMM_TESTTYPE_HASH, got: " + fileName);
      }
      // parts[4] is the 4-character hash, which isn't stored

      String courseCode = parts[0];
      String professor = isUnknown(parts[1]) ? null : parts[1].replace('-', ' ');

      Integer year = null;
      Semester semester = null;
      if (!isUnknown(parts[2])) {
         if (!parts[2].matches("\\d{6}")) {
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

      // "3-SOLUTIONS" is the solutions to test 3
      String testType = parts[3];
      TestState state = TestState.BLANK;
      if (testType.toUpperCase().endsWith("-SOLUTIONS")) {
         state = TestState.SOLUTION;
         testType = testType.substring(0, testType.length() - "-SOLUTIONS".length());
      }
      if (isUnknown(testType)) {
         testType = null;
      }

      return new ParsedFileName(courseCode, professor, year, semester, testType, state);
   }

   private boolean isUnknown(String value) {
      return value.equalsIgnoreCase("unknown");
   }
}
