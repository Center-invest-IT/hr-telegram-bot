create table if not exists questions
(
    id uuid primary key,
    question varchar not null,
    created_time timestamp not null
);


ALTER TABLE answers RENAME COLUMN date_time TO created_time;

ALTER TABLE answers
ALTER COLUMN question_id TYPE uuid USING question_id::uuid;

ALTER TABLE answers
ADD CONSTRAINT fk_answers_questions
FOREIGN KEY (question_id)
REFERENCES questions(id);