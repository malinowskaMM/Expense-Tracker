-- Insert sample data for 'account' table
INSERT INTO account (id, is_active, is_archived, is_enabled, register_date, language_, role, email, password, version)
VALUES
    (-1, true, false, true, '2023-01-01 00:00:00', 'en', 'USER', 'user1@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0),
    (-2, true, false, true, '2023-01-01 00:00:00', 'en', 'USER', 'user2@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0),
    (-3, true, false, true, '2023-01-01 00:00:00', 'pl', 'USER', 'user3@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0),
    (-4, true, false, true, '2023-01-01 00:00:00', 'pl', 'USER', 'user4@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0),
    (-5, true, false, true, '2023-01-01 00:00:00', 'en', 'USER', 'user5@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0),
    (-6, true, false, true, '2023-01-02 00:00:00', 'pl', 'ADMIN', 'admin@example.com', '$2a$12$/CffB5hFug58q9lJM3elKOZ8LWCc6L4ep/QwZJu.4RsG7XbnsVwVa', 0);

-- Insert sample data for 'group_' table
INSERT INTO group_ (id, version, name)
VALUES
    (-1, 0, 'Group testowa 1'),
    (-2, 0, 'Group testowa 2'),
    (-3, 0, 'Group testowa 3');

-- Insert sample data for 'category' table
INSERT INTO category (id, version, name, color, description, is_default, group_id)
VALUES
    (-1, 0, 'Default', '#7BD3EA', 'Default Category', true, -1),
    (-2, 0, 'Default', '#A1EEBD', 'Default Category', true, -2),
    (-3, 0, 'Kategoria testowa 2', '#F6F7C4', 'Opis kategorii testowej 2', false,  -1),
    (-4, 0, 'Kategoria testowa 3', '#F6D6D6', 'Opis kategorii testowej 3', false, -2),
    (-5, 0, 'Default', '#89B9AD', 'Default Category', true, -3);

-- Insert sample data for 'account_group_role' table
INSERT INTO account_group_role (id, access_level, account_id, group_id, role)
VALUES
    (-1, 'USER', -1, -1, 'USER'),
    (-2, 'ADMIN', -2, -1, 'ADMIN'),
    (-5, 'USER', -3, -1, 'USER'),
    (-6, 'USER', -4, -1, 'USER'),
    (-3, 'USER', -3, -2, 'ADMIN'),
    (-7, 'USER', -5, -2, 'ADMIN'),
    (-4, 'ADMIN', -1, -2, 'ADMIN');

-- Insert sample data for 'token' table
INSERT INTO token (id, token, type, revoked, expired, account_id)
VALUES
    (-1, 'token1', 'ACCESS_TOKEN', false, false, -1),
    (-2, 'token2', 'REFRESH_TOKEN', false, false, -2);

-- Insert sample data for 'transaction' table
INSERT INTO transaction (id, transaction_type, name, category_id, is_cyclic, period, period_unit, start_date, end_date, amount, account_id, version)
VALUES
    (-1, 'INCOME', 'Przychód cykliczny testowy co 14 dni', -1, true, 14, 'DAY', '2024-03-02', null, 10.0, -1, 0),
    (-2, 'EXPENSE', 'Wydatek cykliczny testowy co 2 miesiace', -3, true, 2, 'MONTH', '2024-03-15', null, 1200.0, -1, 0),
    (-3, 'INCOME', 'Przychód jednorazowy testowy 1', -1, false, null, null, '2024-04-05', null, 200.0, -1, 0),
    (-4, 'EXPENSE', 'Wydatek jednorazowy testowy 1', -1, false, null, null, '2024-05-13', null, 500.0, -2, 0),
    (-5, 'INCOME', 'Przychód cykliczny testowy co 1 rok', -2, true, 1, 'YEAR', '2024-04-01', null, 100.0, -3, 0),
    (-6, 'EXPENSE', 'Wydatek cykliczny testowy co 5 dni', -2, true, 5, 'DAY', '2024-03-02', '2024-03-22', 20.0, -5, 0),
    (-7, 'INCOME', 'Przychód cykliczny testowy co 20 dni', -3, true, 20, 'DAY', '2024-03-24', '2024-04-24', 150.0, -1, 0),
    (-8, 'EXPENSE', 'Wydatek cykliczny testowy co 1 miesiac', -3, true, 1, 'MONTH', '2024-03-10', null, 2000.0, -1, 0),
    (-9, 'INCOME', 'Przychód jednorazowy testowy 2', -3, false, null, null, '2024-04-25', null, 500.0, -1, 0),
    (-10, 'EXPENSE', 'Wydatek jednorazowy testowy 2', -1, false, null, null, '2024-05-29', null, 1200.0, -1, 0);

INSERT INTO login_entity (invalid_login_counter, last_invalid_login_date, last_valid_login_date, id)
VALUES
    (1, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -1),
    (2, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -2),
    (2, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -3),
    (1, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -4),
    (2, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -5),
    (2, '2023-01-01 00:00:00', '2023-01-01 00:00:00', -6);