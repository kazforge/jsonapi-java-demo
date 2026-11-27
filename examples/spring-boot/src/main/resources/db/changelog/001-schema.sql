--liquibase formatted sql

--changeset demo:001-schema
CREATE TABLE people (
    id uuid PRIMARY KEY,
    name varchar(200) NOT NULL
);

CREATE TABLE articles (
    id uuid PRIMARY KEY,
    title varchar(200) NOT NULL,
    body text,
    status varchar(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED')),
    author_id uuid REFERENCES people (id),
    CONSTRAINT published_article_has_author CHECK (status <> 'PUBLISHED' OR author_id IS NOT NULL)
);

CREATE TABLE article_reviewers (
    article_id uuid NOT NULL REFERENCES articles (id) ON DELETE CASCADE,
    person_id uuid NOT NULL REFERENCES people (id),
    PRIMARY KEY (article_id, person_id)
);

CREATE INDEX articles_status_idx ON articles (status);
