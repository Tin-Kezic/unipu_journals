INSERT INTO account (id, name, surname, title, email, password, affiliation, job_type, country, city, address, zip_code) VALUES
(1, 'Alice', 'Smith', 'Dr.', 'alice@example.com', 'hashed_password_1', 'University A', 'Professor', 'USA', 'New York', '123 Main St', '10001'),
(2, 'Bob', 'Johnson', 'Mr.', 'bob@example.com', 'hashed_password_2', 'Institute B', 'Researcher', 'UK', 'London', '456 Baker St', 'SW1A'),
(3, 'Carol', 'Williams', NULL, 'carol@example.com', 'hashed_password_3', 'College C', 'Editor', 'Canada', 'Toronto', '789 Queen St', 'M5H'),
(4, 'David', 'Miller', 'Prof.', 'david@example.com', 'hashed_password_4', 'University D', 'Professor', 'USA', 'Chicago', '321 West Ave', '60601'),
(5, 'Eva', 'Davis', 'Ms.', 'eva@example.com', 'hashed_password_5', 'Lab E', 'Reviewer', 'Germany', 'Berlin', '99 Einstein Str', '10115'),
(6, 'Frank', 'Taylor', NULL, 'frank@example.com', 'hashed_password_6', 'Institute F', 'Editor', 'France', 'Paris', '11 Rue Cler', '75007');

INSERT INTO publication (id, title, is_hidden) VALUES
(1, 'Science Journal', false),
(2, 'Tech Review', true),
(3, 'Medical Innovations', false);

INSERT INTO category (id, name) VALUES
(1, 'Physics'),
(2, 'Computer Science'),
(3, 'Biomedicine');

INSERT INTO publication_section (id, title, description, publication_id, is_hidden) VALUES
(1, 'Quantum Mechanics', 'Advanced physics topics', 1, false),
(2, 'AI and Machine Learning', NULL, 2, false),
(3, 'Genetics & Molecular Biology', 'Cutting-edge biotech research', 3, false);

INSERT INTO manuscript (id, author_id, category_id, current_state, publication_section_id, file_url, submission_date, publication_date, downloads, views) VALUES
(1, 1, 1, 'AWAITING_INITIAL_EIC_REVIEW', 1, 'https://example.com/files/manuscript1.pdf', NOW(), NOW(), 10, 50),
(2, 2, 2, 'AWAITING_INITIAL_EDITOR_REVIEW', 2, 'https://example.com/files/manuscript2.pdf', NOW(), NOW(), 25, 100),
(3, 4, 3, 'AWAITING_REVIEWER_REVIEW', 3, 'https://example.com/files/manuscript3.pdf', NOW(), NOW(), 5, 30),
(4, 5, 1, 'MAJOR_FIXES', 1, 'https://example.com/files/manuscript4.pdf', NOW(), NOW(), 8, 40),
(5, 3, 2, 'MINOR_FIXES', 2, 'https://example.com/files/manuscript5.pdf', NOW(), NOW(), 12, 60),
(6, 6, 3, 'REJECTED', 3, 'https://example.com/files/manuscript6.pdf', NOW(), NOW(), 20, 70);

INSERT INTO account_role_on_manuscript (id, manuscript_id, account_id, account_role) VALUES
(1, 1, 1, 'AUTHOR'),
(2, 1, 3, 'EDITOR'),
(3, 2, 2, 'REVIEWER'),
(4, 3, 4, 'AUTHOR'),
(5, 3, 5, 'REVIEWER'),
(6, 4, 5, 'AUTHOR'),
(7, 4, 6, 'EDITOR'),
(8, 5, 3, 'AUTHOR'),
(9, 6, 6, 'AUTHOR');

INSERT INTO manuscript_review (
    id, manuscript_id, reviewer_id, reviewer_comment, reviewer_comment_file_url,
    author_comment, author_response_file_url, review_date, author_response_date
) VALUES
(1, 1, 2, 'Needs minor revision.', 'https://example.com/reviews/review1.pdf', NULL, NULL, NOW(), NULL),
(2, 2, 3, NULL, NULL, 'Thank you for the review.', 'https://example.com/responses/response1.pdf', NOW(), NOW()),
(3, 3, 5, 'Good work, but clarify section 3.', 'https://example.com/reviews/review2.pdf', NULL, NULL, NOW(), NULL),
(4, 4, 6, 'Formatting issues.', NULL, NULL, NULL, NOW(), NULL),
(5, 5, 2, 'Acceptable with minor edits.', 'https://example.com/reviews/review3.pdf', NULL, NULL, NOW(), NULL),
(6, 6, 3, NULL, NULL, 'Changes done as requested.', NULL, NOW(), NOW());

INSERT INTO section_editor_on_section (id, publication_section_id, section_editor_id) VALUES
(1, 1, 3),
(2, 2, 2),
(3, 3, 6);

INSERT INTO eic_on_publication (id, publication_id, eic_id) VALUES
(1, 1, 1),
(2, 2, 3),
(3, 3, 4);