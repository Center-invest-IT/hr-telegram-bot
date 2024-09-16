create table if not exists user_state
(
    user_id  bigint  not null,
    username varchar not null,
    state    varchar not null
);