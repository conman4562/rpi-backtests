package edu.rpi.backtests.model;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course")
public class Course {
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

   @Column(nullable = false, unique = true, length = 20)
   private String code;

   @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
   private OffsetDateTime createdAt;

   @OneToMany(mappedBy = "course")
   private List<BackTest> tests = new ArrayList<>();

   protected Course() {}

   public Course(String code) { this.code = code; }

   public UUID getId() { return id; }
   public String getCode() { return code; }
   public OffsetDateTime getCreatedAt() { return createdAt; }
   public List<BackTest> getTests() { return tests; }

}
