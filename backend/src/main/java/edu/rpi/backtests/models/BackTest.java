package edu.rpi.backtests.models;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "back_test")
public class BackTest implements Persistable<UUID> {

   // Assigned in the constructor (not generated on save) because the R2 file key is built from it
   @Id
   private UUID id;

   // Lets save() insert a new row even though the id is already set; see isNew()
   @Transient
   private boolean isNew = true;

   // Semester, year and test type are null when the uploaded file name says "unknown"
   @Enumerated (EnumType.STRING)
   @Column (nullable = true)
   private Semester semester;

   @Column (nullable = true)
   private Integer year;

   @Column (nullable = true)
   private String professor;

   @Column (nullable = true)
   private String testType;

   @Enumerated (EnumType.STRING)
   @Column(nullable = false)
   private TestState state;

   @Column (nullable = false)
   private String uploader = "anonymous";

   @Column (nullable = false)
   private Boolean anonymous = false;

   @Column (nullable = false)
   private Boolean approved = false;

   @Column (name = "file_key", nullable = false, unique = true)
   private String fileKey;

   // Set by the database default; @Generated makes Hibernate read it back after the insert,
   // otherwise it stays null on a freshly saved entity (and the upload response rejects that)
   @Generated(event = EventType.INSERT)
   @Column (name = "created_at", nullable = false, insertable = false, updatable = false)
   private OffsetDateTime createdAt;

   @ManyToOne(fetch= FetchType.LAZY, optional = false)
   @JoinColumn(name = "course_id")
   private Course course;

   protected BackTest() {}
   
   public BackTest(Course course, Semester semester, String professor, TestState state, Integer year, String uploader, Boolean anonymous, String testType){
      this.id = UUID.randomUUID();
      this.course = course;
      this.semester = semester;
      this.professor = professor;
      this.state = state;
      this.year = year;
      this.uploader = uploader;
      this.anonymous = anonymous;
      this.testType = testType;
      this.fileKey = course.getCode() + "_" + id;
   }

   @Override
   public boolean isNew() { return isNew; }

   @PostLoad
   @PostPersist
   void markNotNew() { isNew = false; }

   @Override
   public UUID getId() { return id; }
   public Semester getSemester() { return semester; }
   public Integer getYear() { return year; }
   public String getProfessor() { return professor; }
   public String getTestType() { return testType; }
   public TestState getState() { return state; }
   public String getUploader() { return uploader; }
   public Boolean getAnonymous() { return anonymous; }
   public Boolean getApproved() { return approved; }
   public OffsetDateTime getCreatedAt() { return createdAt; }
   public Course getCourse() { return course; }
   public String getFileKey() { return fileKey; }
   public void approve() { this.approved = true; }
}
