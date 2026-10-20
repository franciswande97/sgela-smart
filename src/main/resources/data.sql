INSERT INTO users (email, password, name) 
VALUES 
    ('rinavho@sgelasmart.com', '$2a$10$F.XiSixk4SCd5XzMfKj7nuXtxRKmz8bXbIqbqjtJkbZ2sEaC1EC1W', 'Rinavho'),
    ('student2@sgelasmart.com', '$2a$10$F.XiSixk4SCd5XzMfKj7nuXtxRKmz8bXbIqbqjtJkbZ2sEaC1EC1W', 'Thabo')
ON CONFLICT (email) DO UPDATE 
SET password = EXCLUDED.password;

-- Seed Goals for Rinavho
INSERT INTO goals (user_id, title, deadline, status, created_at)
SELECT u.id, 'Complete Spring Boot Security Module', CURRENT_DATE + INTERVAL '7 days', 'IN_PROGRESS', NOW()
FROM users u WHERE u.email = 'rinavho@sgelasmart.com'
AND NOT EXISTS (
    SELECT 1 FROM goals g WHERE g.user_id = u.id AND g.title = 'Complete Spring Boot Security Module'
);

INSERT INTO goals (user_id, title, deadline, status, created_at)
SELECT u.id, 'Implement Swagger Documentation', CURRENT_DATE + INTERVAL '2 days', 'COMPLETED', NOW()
FROM users u WHERE u.email = 'rinavho@sgelasmart.com'
AND NOT EXISTS (
    SELECT 1 FROM goals g WHERE g.user_id = u.id AND g.title = 'Implement Swagger Documentation'
);

-- Seed Goals for Student 2 (Thabo)
INSERT INTO goals (user_id, title, deadline, status, created_at)
SELECT u.id, 'Study Discrete Mathematics Chapter 4', CURRENT_DATE + INTERVAL '14 days', 'NOT_STARTED', NOW()
FROM users u WHERE u.email = 'student2@sgelasmart.com'
AND NOT EXISTS (
    SELECT 1 FROM goals g WHERE g.user_id = u.id AND g.title = 'Study Discrete Mathematics Chapter 4'
);
