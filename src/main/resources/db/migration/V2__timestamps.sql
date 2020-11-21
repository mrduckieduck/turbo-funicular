alter table users
    add column created_at timestamp null default now();
alter table users
    add column last_updated timestamp null default now();