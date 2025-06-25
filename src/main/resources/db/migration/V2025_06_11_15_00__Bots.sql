create table if not exists bots
(
    id uuid primary key,
    bot_username varchar not null unique,
    bot_token varchar not null unique,
    description varchar,
    chat_id bigint null,
    status varchar not null
);