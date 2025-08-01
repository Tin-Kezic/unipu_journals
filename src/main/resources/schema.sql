CREATE TYPE manuscript_role AS ENUM (
    'EIC',
    'EDITOR',
    'REVIEWER',
    'CORRESPONDING_AUTHOR',
    'AUTHOR'
);

CREATE TYPE manuscript_state AS ENUM (
    'AWAITING_INITIAL_EIC_REVIEW',
    'AWAITING_INITIAL_EDITOR_REVIEW',
    'AWAITING_REVIEWER_REVIEW',
    'MINOR_FIXES',
    'MAJOR_FIXES',
    'REJECTED',
    'PUBLISHED',
    'HIDDEN',
    'DRAFT'
);

CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    title TEXT,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    affiliated TEXT NOT NULL,
    job_type TEXT NOT NULL,
    country TEXT NOT NULL,
    city TEXT NOT NULL,
    address TEXT NOT NULL,
    zip_code TEXT NOT NULL
);

CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE publication (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    is_hidden bool NOT NULL DEFAULT FALSE
);

CREATE TABLE eic_on_publication (
    id SERIAL PRIMARY KEY,
    publication_id INT NOT NULL,
    eic_id INT NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (eic_id) REFERENCES account(id) ON DELETE NO ACTION
);

CREATE TABLE publication_section (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    publication_id INT NOT NULL,
    is_hidden bool NOT NULL DEFAULT FALSE,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE
);

CREATE TABLE section_editor_on_section(
    id SERIAL PRIMARY KEY,
    publication_section_id INT NOT NULL,
    section_editor_id INT NOT NULL,
    FOREIGN KEY (publication_section_id) REFERENCES publication_section(id) ON DELETE NO ACTION,
    FOREIGN KEY (section_editor_id) REFERENCES account(id) ON DELETE CASCADE
);

CREATE TABLE manuscript (
    id SERIAL PRIMARY KEY,
    author_id INT NOT NULL,
    category_id INT NOT NULL,
    current_state manuscript_state INT NOT NULL,
    publication_section_id INT NOT NULL,
    file_url TEXT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    round INT NOT NULL DEFAULT 1,
    views INT NOT NULL DEFAULT 0,
    downloads INT NOT NULL DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES account(id) ON DELETE NO ACTION,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE NO ACTION,
    FOREIGN KEY (publication_section_id) REFERENCES publication_section(id) ON DELETE NO ACTION
);

CREATE TABLE account_role_on_manuscript (
    id SERIAL PRIMARY KEY,
    manuscript_id INT NOT NULL,
    account_id INT NOT NULL,
    account_role manuscript_role NOT NULL,
    FOREIGN KEY (manuscript_id) REFERENCES manuscript(id) ON DELETE NO ACTION,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE NO ACTION,
    UNIQUE (manuscript_id, account_id, account_role)
);

CREATE TABLE manuscript_review (
    id SERIAL PRIMARY KEY,
    manuscript_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    reviewer_comment TEXT,
    reviewer_comment_file_url TEXT,
    author_response_file_url TEXT,
    author_comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    author_response_date TIMESTAMP,
    FOREIGN KEY (reviewer_id) REFERENCES account(id) ON DELETE NO ACTION,
    FOREIGN KEY (manuscript_id) REFERENCES manuscript(id) ON DELETE NO ACTION
);