-- Uploaded file names can say "unknown" for the term or test type, so these are optional
ALTER TABLE back_test ALTER COLUMN year DROP NOT NULL;
ALTER TABLE back_test ALTER COLUMN semester DROP NOT NULL;
ALTER TABLE back_test ALTER COLUMN test_type DROP NOT NULL;
