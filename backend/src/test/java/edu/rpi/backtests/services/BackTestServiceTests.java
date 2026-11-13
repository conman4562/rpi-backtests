package edu.rpi.backtests.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.models.Course;
import edu.rpi.backtests.models.Semester;
import edu.rpi.backtests.models.TestState;
import edu.rpi.backtests.repositories.BackTestRepository;
import edu.rpi.backtests.repositories.CourseRepository;
import edu.rpi.backtests.storage.CloudflareR2Client;

class BackTestServiceTests {
  private final BackTestRepository backTests = mock(BackTestRepository.class);
  private final CourseRepository courses = mock(CourseRepository.class);
  private final CloudflareR2Client r2 = mock(CloudflareR2Client.class);
  private final BackTestService service = new BackTestService(backTests, courses, r2);

  @BeforeEach
  void setUp() {
    when(courses.findByCode(anyString())).thenAnswer(call ->
        Optional.of(new Course(call.getArgument(0), "Course")));
    when(backTests.save(any(BackTest.class))).thenAnswer(call -> call.getArgument(0));
  }

  private BackTest upload(String filename) throws Exception {
    return service.adminUpload(new MockMultipartFile("file", filename, "application/pdf",
        "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8)), "test-uploader", false);
  }

  @Test
  void importsSixFieldsAndStoresExplicitSolutionState() throws Exception {
    BackTest test = upload("MATH-4800_William-D-Henshaw_202009_EXAM-1_SOLUTION_5d109c65.pdf");
    assertEquals("MATH-4800", test.getCourse().getCode());
    assertEquals("William D Henshaw", test.getProfessor());
    assertEquals(2020, test.getYear());
    assertEquals(Semester.FALL, test.getSemester());
    assertEquals("EXAM-1", test.getTestType());
    assertEquals(TestState.SOLUTION, test.getState());
    verify(r2).putObject(eq("MATH-4800_William-D-Henshaw_202009_EXAM-1_SOLUTION_5d109c65.pdf"),
        eq(test.getFileKey()), eq("application/pdf"), any(byte[].class));
  }

  @ParameterizedTest
  @CsvSource({"BLANK, BLANK", "SOLUTION, SOLUTION", "USERFILLED, FILLED", "FILLED, FILLED"})
  void mapsFilenameStatesToExistingDatabaseStates(String filenameState, TestState expected) throws Exception {
    BackTest test = upload("CSCI-1100_Uzma-@-Mushtaque_202301_QUIZ-14_" + filenameState + "_12345678.pdf");
    assertEquals(expected, test.getState());
    assertEquals("Uzma Mushtaque", test.getProfessor());
    assertEquals(Semester.SPRING, test.getSemester());
  }

  @Test
  void preservesCompoundSurnameAndArbitraryPracticeType() throws Exception {
    BackTest test = upload("CSCI-2200_Malik-@-Magdon-Ismail_202005_PRACTICE-EXAM-3-A_BLANK_12345678.pdf");
    assertEquals("Malik Magdon-Ismail", test.getProfessor());
    assertEquals("PRACTICE-EXAM-3-A", test.getTestType());
    assertEquals(Semester.SUMMER, test.getSemester());
  }

  @Test
  void keepsOptionalUnknownMetadataNull() throws Exception {
    BackTest test = upload("CSCI-1100_UNKNOWN_UNKNOWN_UNKNOWN_BLANK_12345678.pdf");
    assertNull(test.getProfessor());
    assertNull(test.getYear());
    assertNull(test.getSemester());
    assertNull(test.getTestType());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {
      "UNKNOWN_UNKNOWN_202001_EXAM-1_BLANK_12345678.pdf",
      "CSCI-1100_UNKNOWN_202001_EXAM-1_UNKNOWN_12345678.pdf",
      "CSCI-1100_UNKNOWN_202001_EXAM-1_INVALID_12345678.pdf",
      "CSCI-1100_UNKNOWN_202013_EXAM-1_BLANK_12345678.pdf",
      "CSCI-1100_UNKNOWN_20250502_EXAM-1_BLANK_12345678.pdf",
      "CSCI-1100_UNKNOWN_202001_EXAM-1_BLANK_.pdf",
      "CSCI-1100_UNKNOWN_202001__BLANK_12345678.pdf",
      "CSCI1100_UNKNOWN_202001_EXAM-1_BLANK_12345678.pdf",
      "CSCI-1100_Uzma--Mushtaque_202001_EXAM-1_BLANK_12345678.pdf",
      "CSCI-1100_UNKNOWN_202001_EXAM-1_12345678.pdf"
  })
  void rejectsUnresolvedOrMalformedFilenamesBeforeAnyStorage(String filename) {
    assertThrows(IllegalArgumentException.class, () -> upload(filename));
    verifyNoInteractions(courses, backTests, r2);
  }
}
