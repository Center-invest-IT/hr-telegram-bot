create table if not exists answers
(
    id          uuid primary key,
    user_id     bigint    not null,
    username    varchar   not null,
    question_id varchar   not null,
    answer      varchar   not null,
    date_time   timestamp not null
);

create table if not exists active_questions
(
    user_id     bigint  not null,
    question_id varchar
);