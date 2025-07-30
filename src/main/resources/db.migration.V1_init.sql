CREATE TABLE app_users (
                           id SERIAL PRIMARY KEY,
                           username VARCHAR(100) UNIQUE NOT NULL,
                           password_hash VARCHAR(100) NOT NULL
);

CREATE TABLE refresh_tokens (
                                token VARCHAR(36) PRIMARY KEY,
                                username VARCHAR(100) NOT NULL REFERENCES app_users(username) ON DELETE CASCADE,
                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
