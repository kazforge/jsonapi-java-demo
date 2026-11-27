--liquibase formatted sql

--changeset demo:002-seed
INSERT INTO people (id, name) VALUES
    ('11111111-1111-4111-8111-111111111111', 'Ada Lovelace'),
    ('22222222-2222-4222-8222-222222222222', 'Grace Hopper'),
    ('33333333-3333-4333-8333-333333333333', 'Ursula K. Le Guin');

INSERT INTO articles (id, title, body, status, author_id) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', 'Draft notes', NULL, 'DRAFT', NULL),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', 'API guide',
        'A guide to the publishing API.', 'PUBLISHED', '11111111-1111-4111-8111-111111111111'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3', 'Editorial guide',
        'A guide for editors.', 'PUBLISHED', '22222222-2222-4222-8222-222222222222');

INSERT INTO article_reviewers (article_id, person_id) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', '22222222-2222-4222-8222-222222222222'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', '33333333-3333-4333-8333-333333333333'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3', '11111111-1111-4111-8111-111111111111');
