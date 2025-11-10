INSERT INTO users (login, password, created_at)
SELECT 'user1', '$2a$12$K8L72k9ZjQR2.OB.8wH.ue3H.7tNY1WY8YkZ6Q7qQ8Q5Q2Y8Q5Q2Y', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'user1');

INSERT INTO users (login, password, created_at)
SELECT 'user2', '$2a$12$K8L72k9ZjQR2.OB.8wH.ue3H.7tNY1WY8YkZ6Q7qQ8Q5Q2Y8Q5Q2Y', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'user2');

INSERT INTO users (login, password, created_at)
SELECT 'admin', '$2a$12$K8L72k9ZjQR2.OB.8wH.ue3H.7tNY1WY8YkZ6Q7qQ8Q5Q2Y8Q5Q2Y', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'admin');