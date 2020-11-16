CREATE TABLE users
(
    id           SERIAL,
    login        VARCHAR(200) NOT NULL UNIQUE,
    avatar_url   VARCHAR(2000),
    bio          VARCHAR(2000),
    gh_id        integer      NOT NULL UNIQUE,
    name         VARCHAR(200),
    public_gists integer DEFAULT 0
);
