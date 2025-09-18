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
CREATE TYPE invitation_target AS ENUM (
    'ADMIN',
    'EIC_ON_PUBLICATION',
    'EIC_ON_MANUSCRIPT',
    'SECTION_EDITOR_ON_SECTION',
    'EDITOR_ON_MANUSCRIPT',
    'REVIEWER_ON_MANUSCRIPT'
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
CREATE TYPE manuscript_state AS ENUM (
    'AWAITING_INITIAL_EIC_REVIEW',
    'AWAITING_INITIAL_EDITOR_REVIEW',
    'AWAITING_REVIEWER_REVIEW',
    'MINOR',
    'MAJOR',
    'PUBLISHED',
    'REJECTED',
    'ARCHIVED',
    'HIDDEN',
    'DRAFT'
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
    FOREIGN KEY (author_id) REFERENCES account(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    FOREIGN KEY (section_id) REFERENCES publication_section(id) ON DELETE CASCADE
);
CREATE TYPE manuscript_role AS ENUM (
    'EIC',
    'EDITOR',
    'REVIEWER',
    'CORRESPONDING_AUTHOR',
    'AUTHOR'
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
CREATE TYPE one_to_five AS ENUM ('ONE', 'TWO', 'THREE', 'FOUR', 'FIVE');
CREATE TYPE review_question AS ENUM ('YES', 'CAN_BE_IMPROVED', 'MUST_BE_IMPROVED');
CREATE TYPE review_recommendation AS ENUM ('ACCEPT', 'MINOR', 'MAJOR', 'REJECT');
CREATE TABLE manuscript_review (
    id SERIAL PRIMARY KEY,
    manuscript_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    round INT NOT NULL,
    novelty one_to_five NOT NULL,
    significance one_to_five NOT NULL,
    technical_quality one_to_five NOT NULL,
    clarity one_to_five NOT NULL,
    methodology one_to_five NOT NULL,
    relevance_to_the_publication one_to_five NOT NULL,
    language_quality one_to_five NOT NULL,
    overall_mark one_to_five NOT NULL,
    sufficient_background review_question NOT NULL,
    appropriate_research_design review_question NOT NULL,
    adequately_described review_question NOT NULL,
    clearly_presented review_question NOT NULL,
    supported_conclusions review_question NOT NULL,
    conflict BOOL NOT NULL,
    plagiarism BOOL NOT NULL,
    llm BOOL NOT NULL,
    self_citation BOOL NOT NULL,
    appropriate_references BOOL NOT NULL,
    ethical_concerns BOOL NOT NULL,
    reviewer_comment TEXT,
    reviewer_comment_file_url TEXT,
    author_response TEXT,
    author_response_file_url TEXT,
    recommendation review_recommendation NOT NULL,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    author_response_date TIMESTAMP NOT NULL,
    FOREIGN KEY (reviewer_id) REFERENCES account(id) ON DELETE SET NULL,
    FOREIGN KEY (manuscript_id) REFERENCES manuscript(id) ON DELETE CASCADE
);