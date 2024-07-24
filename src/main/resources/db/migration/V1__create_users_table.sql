CREATE TABLE IF NOT EXISTS users(
    id serial PRIMARY KEY,
    login varchar(64) UNIQUE NOT NULL,
    password varchar(64) NOT NULL
)