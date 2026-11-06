ALTER TABLE test RENAME TO back_test;

-- Postgres named these after the old table; rename them so nothing still says "test"
ALTER SEQUENCE test_id_seq RENAME TO back_test_id_seq;
ALTER INDEX test_pkey RENAME TO back_test_pkey;
ALTER INDEX idx_test_course_id RENAME TO idx_back_test_course_id;
ALTER TABLE back_test RENAME CONSTRAINT test_course_id_fkey TO back_test_course_id_fkey;
ALTER TABLE back_test RENAME CONSTRAINT test_semester_check TO back_test_semester_check;
