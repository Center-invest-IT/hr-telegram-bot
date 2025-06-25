create table if not exists admins
(
    id        uuid      primary key,
    login     varchar   not null,
    password_hash   varchar   not null
);
INSERT INTO admins (id, login, password_hash)
VALUES ('ca2bb162-6a47-4f4c-8fb5-fbc7435c6940', 'admin', '$2a$10$kt0QVaecTjs33AYQg6EfSu1Id8aEbizyP0qnW/tPNf7.O.scRJBk6');