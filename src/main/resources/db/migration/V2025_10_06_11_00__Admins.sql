create table if not exists admins
(
    id        uuid      primary key,
    login     varchar   not null,
    password_hash   varchar   not null
);