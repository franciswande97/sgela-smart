INSERT INTO users (email, password, name) 
VALUES ('rinavho@sgelasmart.com', '$2a$10$wKqKgzQk35K4r9gVlZ5b5OKg/d9mD0o4nU9mD6S5W6Zt5L8YQdCyy', 'Rinavho')
ON CONFLICT (email) DO NOTHING;
