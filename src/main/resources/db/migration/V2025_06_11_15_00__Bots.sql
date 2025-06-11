create table if not exists bots
(
  id uuid primary key,
  bot_username varchar not null unique,
  bot_token varchar not null unique,
  description varchar,
  status bool
);

create table if not exists bots_chats
(
  id uuid primary key,
  chat_id bigint not null,
  bot_username varchar not null
);
