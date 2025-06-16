create table if not exists admins
(
    adminid         uuid      primary key,
    adminlogin       varchar   not null,
    hashpassword    varchar   not null
);