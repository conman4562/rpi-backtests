CREATE TABLE course (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE test (
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT       NOT NULL REFERENCES course(id),
    semester   VARCHAR(10)  NOT NULL CHECK (semester IN ('FALL', 'SPRING')),
    year       INT          NOT NULL,
    uploader   VARCHAR(255) NOT NULL DEFAULT 'anonymous',
    anonymous  BOOLEAN      NOT NULL DEFAULT false,
    approved   BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_test_course_id ON test(course_id);
