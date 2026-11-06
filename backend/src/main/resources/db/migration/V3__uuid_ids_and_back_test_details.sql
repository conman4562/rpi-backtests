-- Switches both tables to UUID ids and adds the new back_test columns.
-- The tables are recreated rather than altered, so refuse to run if they hold any data.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM back_test) OR EXISTS (SELECT 1 FROM course) THEN
        RAISE EXCEPTION 'V3 recreates course and back_test; they must be empty';
    END IF;
END $$;

DROP TABLE back_test;
DROP TABLE course;

CREATE TABLE course (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE back_test (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id  UUID         NOT NULL REFERENCES course(id),
    semester   VARCHAR(10)  NOT NULL CHECK (semester IN ('FALL', 'SPRING', 'SUMMER')),
    year       INT          NOT NULL,
    professor  VARCHAR(255),
    test_type  VARCHAR(255) NOT NULL,
    state      VARCHAR(20)  NOT NULL CHECK (state IN ('BLANK', 'FILLED', 'SOLUTION')),
    uploader   VARCHAR(255) NOT NULL DEFAULT 'anonymous',
    anonymous  BOOLEAN      NOT NULL DEFAULT false,
    approved   BOOLEAN      NOT NULL DEFAULT false,
    file_key   VARCHAR(512) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_back_test_course_id ON back_test(course_id);
