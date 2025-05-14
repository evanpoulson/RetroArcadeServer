CREATE TABLE profiles (
                          profile_id SERIAL PRIMARY KEY,
                          username VARCHAR(50) UNIQUE NOT NULL,
                          nickname VARCHAR(50),
                          email VARCHAR(255) UNIQUE NOT NULL,
                          hashed_password VARCHAR(255) NOT NULL,
                          bio TEXT,
                          profile_pic_path TEXT,
                          is_online BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE friends (
                         profile_id INT NOT NULL,
                         friend_id INT NOT NULL,
                         status VARCHAR(20) DEFAULT 'pending',
                         PRIMARY KEY (profile_id, friend_id),
                         FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
                         FOREIGN KEY (friend_id) REFERENCES profiles(profile_id) ON DELETE CASCADE
);

CREATE TABLE games (
                       game_id SERIAL PRIMARY KEY,
                       game_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE game_stats (
                            stat_id SERIAL PRIMARY KEY,
                            profile_id INT NOT NULL,
                            game_id INT NOT NULL,
                            wins INT DEFAULT 0,
                            losses INT DEFAULT 0,
                            total_games INT DEFAULT 0,
                            win_loss_ratio DOUBLE PRECISION DEFAULT 0.00,
                            rating INT DEFAULT 0,
                            rank VARCHAR(50) DEFAULT 'unranked',
                            FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
                            FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                            UNIQUE (profile_id, game_id)
);

CREATE TABLE achievements (
                              achievement_id SERIAL PRIMARY KEY,
                              profile_id INT NOT NULL,
                              game_id INT NOT NULL,
                              achievement_name VARCHAR(100) NOT NULL,
                              progress INT DEFAULT 0,
                              max_progress INT DEFAULT 100,
                              FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
                              FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE
);

CREATE TABLE game_sessions (
                               session_id SERIAL PRIMARY KEY,
                               profile_id INT NOT NULL,
                               game_id INT NOT NULL,
                               score INT NOT NULL,
                               session_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
                               FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE
);