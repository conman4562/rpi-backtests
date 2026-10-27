package edu.rpi.backtests.model;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "back_test")
public class BackTest {

   @Id
   @GeneratedValue (strategy = GenerationType.UUID)
   private UUID id;

   @Enumerated (EnumType.STRING)
   @Column (nullable = false)
   private Semester semester;
   
   @Column (nullable = false)
   private Integer year;

   @Column (nullable = false)
   private String uploader = "anonymous";

   @Column (nullable = false)
   private Boolean anonymous = false;

   @Column (nullable = false)
   private Boolean approved = false;

   @Column (name = "file_key", nullable = false, unique = true)
   private String fileKey;

   @Column (name = "created_at", nullable = false, insertable = false, updatable = false)
   private OffsetDateTime createdAt;

   @ManyToOne(fetch= FetchType.LAZY, optional = false)
   @JoinColumn(name = "course_id")
   private Course course;

   protected BackTest() {}
   
   public BackTest(Course course, Semester semester, Integer year, String uploader, Boolean anonymous, String fileKey){
      this.course = course;
      this.semester = semester;
      this.year = year;
      this.uploader = uploader;
      this.anonymous = anonymous;
      this.fileKey = fileKey;
   }

   public UUID getId() { return id; }
   public Semester getSemester() { return semester; }
   public Integer getYear() { return year; }
   public String getUploader() { return uploader; }
   public Boolean getAnonymous() { return anonymous; }
   public Boolean getApproved() { return approved; }
   public OffsetDateTime getCreatedAt() { return createdAt; }
   public Course getCourse() { return course; }
   public String getFileKey() { return fileKey; }
}
