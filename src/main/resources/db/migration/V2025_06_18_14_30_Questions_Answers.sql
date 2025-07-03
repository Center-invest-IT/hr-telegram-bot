create table if not exists questions
(
    id uuid primary key,
    bot_id uuid not null,
    question varchar not null,
    created_time timestamp not null
);

ALTER TABLE answers
ALTER COLUMN question_id TYPE uuid USING question_id::uuid;

ALTER TABLE answers
ADD CONSTRAINT fk_answers_questions
FOREIGN KEY (question_id)
REFERENCES questions(id);