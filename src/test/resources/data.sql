-- Очистка таблиц (важно для create-drop)
DELETE FROM auth_tokens;
DELETE FROM user_files;
DELETE FROM users;

-- Тестовые пользователи
INSERT INTO users (login, password, created_at) VALUES
                                                    ('user1', '$2a$12$K8L72k9ZjQR2.OB.8wH.ue3H.7tNY1WY8YkZ6Q7qQ8Q5Q2Y8Q5Q2Y', NOW()),
                                                    ('user2', '$2a$12$K8L72k9ZjQR2.OB.8wH.ue3H.7tNY1WY8YkZ6Q7qQ8Q5Q2Y8Q5Q2Y', NOW());