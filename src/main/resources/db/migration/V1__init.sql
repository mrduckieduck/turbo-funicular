CREATE TABLE users
(
    id             SERIAL,
    login          VARCHAR(200) NOT NULL UNIQUE,
    avatar_url     VARCHAR(2000) DEFAULT '',
    bio            VARCHAR(2000) DEFAULT '',
    gh_id          INTEGER      NOT NULL UNIQUE,
    name           VARCHAR(200)  DEFAULT '',
    public_gists   INTEGER       DEFAULT 0,
    public_profile BOOLEAN       DEFAULT true
);
