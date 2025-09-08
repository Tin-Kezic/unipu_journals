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
    'PUBLISHED',
    'REJECTED',
    'ARCHIVED',
    'HIDDEN',
    'DRAFT'
);
CREATE TYPE invitation_target AS ENUM (
    'ADMIN',
    'EIC_ON_PUBLICATION',
    'EIC_ON_MANUSCRIPT',
    'SECTION_EDITOR_ON_SECTION',
    'EDITOR_ON_MANUSCRIPT',
    'REVIEWER_ON_MANUSCRIPT'
);
CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    full_name TEXT NOT NULL,
    title TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    affiliation TEXT NOT NULL,
    job_type TEXT NOT NULL,
    country TEXT NOT NULL,
    city TEXT NOT NULL,
    address TEXT NOT NULL,
    zip_code TEXT NOT NULL,
    is_admin BOOL NOT NULL DEFAULT FALSE
);
CREATE TABLE invite(
    id SERIAL PRIMARY KEY,
    email TEXT NOT NULL,
    target invitation_target NOT NULL,
    target_id INT NOT NULL
);
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);
CREATE TABLE publication (
    id SERIAL PRIMARY KEY,
    title TEXT UNIQUE NOT NULL,
    is_hidden BOOL NOT NULL DEFAULT FALSE
);
CREATE TABLE eic_on_publication (
    id SERIAL PRIMARY KEY,
    publication_id INT NOT NULL,
    eic_id INT NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (eic_id) REFERENCES account(id) ON DELETE CASCADE
);
CREATE TABLE publication_section (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    publication_id INT NOT NULL,
    is_hidden BOOL NOT NULL DEFAULT FALSE,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE
);
CREATE TABLE section_editor_on_section(
    id SERIAL PRIMARY KEY,
    publication_section_id INT NOT NULL,
    section_editor_id INT NOT NULL,
    FOREIGN KEY (publication_section_id) REFERENCES publication_section(id) ON DELETE CASCADE,
    FOREIGN KEY (section_editor_id) REFERENCES account(id) ON DELETE CASCADE
);
CREATE TABLE manuscript (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    author_id INT NOT NULL,
    category_id INT NOT NULL,
    current_state manuscript_state NOT NULL DEFAULT 'AWAITING_INITIAL_EIC_REVIEW',
    section_id INT NOT NULL,
    file_url TEXT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    publication_date TIMESTAMP,
    views INT NOT NULL DEFAULT 0,
    downloads INT NOT NULL DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES account(id) ON DELETE SET DEFAULT,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET DEFAULT,
    FOREIGN KEY (section_id) REFERENCES publication_section(id) ON DELETE CASCADE
);
CREATE TABLE account_role_on_manuscript (
    id SERIAL PRIMARY KEY,
    manuscript_id INT NOT NULL,
    account_id INT NOT NULL,
    account_role manuscript_role NOT NULL,
    FOREIGN KEY (manuscript_id) REFERENCES manuscript(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    UNIQUE (manuscript_id, account_id, account_role)
);
CREATE TABLE manuscript_review (
    id SERIAL PRIMARY KEY,
    manuscript_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    round INT NOT NULL DEFAULT 1,
    reviewer_comment TEXT,
    reviewer_comment_file_url TEXT,
    author_response TEXT,
    author_response_file_url TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    author_response_date TIMESTAMP NOT NULL,
    FOREIGN KEY (reviewer_id) REFERENCES account(id) ON DELETE SET DEFAULT,
    FOREIGN KEY (manuscript_id) REFERENCES manuscript(id) ON DELETE CASCADE
);