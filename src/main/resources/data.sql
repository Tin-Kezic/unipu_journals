INSERT INTO account (name, surname, title, email, password, affiliation, job_type, country, city, address, zip_code, is_admin)
VALUES
('Alice', 'Smith', 'Dr.', 'alice.smith@example.com', 'hashedpassword1', 'University A', 'Professor', 'USA', 'New York', '123 Main St', '10001', TRUE),
('Bob', 'Johnson', 'Prof.', 'bob.johnson@example.com', 'hashedpassword2', 'University B', 'Editor', 'USA', 'Los Angeles', '456 Elm St', '90001', FALSE),
('Charlie', 'Williams', 'Dr.', 'charlie.williams@example.com', 'hashedpassword3', 'Institute C', 'Reviewer', 'UK', 'London', '789 Oak St', 'SW1A 1AA', FALSE),
('Diana', 'Brown', 'Ms.', 'diana.brown@example.com', 'hashedpassword4', 'University D', 'Author', 'Canada', 'Toronto', '101 Maple St', 'M5H 2N2', FALSE),
('Edward', 'Miller', 'Dr.', 'edward.miller@example.com', 'hashedpassword5', 'University E', 'Author', 'Germany', 'Berlin', '15 Hauptstrasse', '10115', FALSE),
('Fiona', 'Davis', 'Dr.', 'fiona.davis@example.com', 'hashedpassword6', 'University F', 'Reviewer', 'France', 'Paris', '25 Rue Lafayette', '75009', FALSE),
('George', 'Clark', 'Prof.', 'george.clark@example.com', 'hashedpassword7', 'University G', 'Editor', 'Italy', 'Rome', '10 Via Roma', '00100', FALSE),
('Hannah', 'Lopez', 'Dr.', 'hannah.lopez@example.com', 'hashedpassword8', 'University H', 'Professor', 'Spain', 'Madrid', '20 Calle Mayor', '28013', FALSE),
('Ian', 'Walker', 'Mr.', 'ian.walker@example.com', 'hashedpassword9', 'University I', 'Reviewer', 'Australia', 'Sydney', '5 George St', '2000', FALSE),
('Julia', 'Lee', 'Ms.', 'julia.lee@example.com', 'hashedpassword10', 'University J', 'Author', 'Japan', 'Tokyo', '8 Chiyoda', '100-0001', FALSE);

INSERT INTO category (name)
VALUES
('Computer Science'),
('Biology'),
('Physics'),
('Mathematics'),
('Chemistry');

INSERT INTO publication (title, is_hidden)
VALUES
('Journal of AI Research', FALSE),
('Nature of Biology', FALSE),
('Physics Letters', FALSE),
('Advanced Mathematics Review', FALSE),
('Chemical Discoveries', FALSE);

INSERT INTO eic_on_publication (publication_id, eic_id)
VALUES
(1, 1),
(2, 8),
(3, 1),
(4, 8),
(5, 1);

INSERT INTO publication_section (title, description, publication_id, is_hidden)
VALUES
('Machine Learning', 'ML research and techniques', 1, FALSE),
('Neuroscience', 'Brain and behavior research', 2, FALSE),
('Quantum Physics', 'Quantum mechanics and related fields', 3, FALSE),
('Algebra', 'Algebraic structures and systems', 4, FALSE),
('Organic Chemistry', 'Organic molecules and reactions', 5, FALSE);

INSERT INTO section_editor_on_section (publication_section_id, section_editor_id)
VALUES
(1, 2),
(2, 7),
(3, 2),
(4, 7),
(5, 2);

INSERT INTO manuscript (author_id, category_id, current_state, section_id, file_url, submission_date, publication_date, views, downloads)
VALUES
(4, 1, 'AWAITING_INITIAL_EIC_REVIEW', 1, 'http://example.com/ms1.pdf', CURRENT_TIMESTAMP, NULL, 12, 3),
(4, 2, 'MINOR_FIXES', 2, 'http://example.com/ms2.pdf', CURRENT_TIMESTAMP, NULL, 5, 1),
(5, 3, 'AWAITING_REVIEWER_REVIEW', 3, 'http://example.com/ms3.pdf', CURRENT_TIMESTAMP, NULL, 22, 5),
(10, 4, 'MAJOR_FIXES', 4, 'http://example.com/ms4.pdf', CURRENT_TIMESTAMP, NULL, 7, 2),
(10, 5, 'PUBLISHED', 5, 'http://example.com/ms5.pdf', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 110, 90),
(4, 1, 'REJECTED', 1, 'http://example.com/ms6.pdf', CURRENT_TIMESTAMP, NULL, 4, 0),
(5, 2, 'DRAFT', 2, 'http://example.com/ms7.pdf', CURRENT_TIMESTAMP, NULL, 0, 0),
(10, 3, 'HIDDEN', 3, 'http://example.com/ms8.pdf', CURRENT_TIMESTAMP, NULL, 3, 1),
(5, 4, 'AWAITING_INITIAL_EDITOR_REVIEW', 4, 'http://example.com/ms9.pdf', CURRENT_TIMESTAMP, NULL, 8, 2),
(10, 5, 'AWAITING_INITIAL_EIC_REVIEW', 5, 'http://example.com/ms10.pdf', CURRENT_TIMESTAMP, NULL, 6, 1);


INSERT INTO account_role_on_manuscript (manuscript_id, account_id, account_role)
VALUES

(1, 4, 'AUTHOR'),
(1, 4, 'CORRESPONDING_AUTHOR'),
(1, 1, 'EIC'),
(1, 2, 'EDITOR'),
(1, 3, 'REVIEWER'),
(2, 4, 'AUTHOR'),
(2, 2, 'EDITOR'),
(2, 6, 'REVIEWER'),
(3, 5, 'AUTHOR'),
(3, 1, 'EIC'),
(3, 7, 'EDITOR'),
(3, 6, 'REVIEWER'),
(3, 9, 'REVIEWER'),
(4, 10, 'AUTHOR'),
(4, 8, 'EIC'),
(4, 7, 'EDITOR'),
(4, 9, 'REVIEWER'),
(5, 10, 'AUTHOR'),
(5, 1, 'EIC'),
(5, 2, 'EDITOR'),
(5, 6, 'REVIEWER'),
(6, 4, 'AUTHOR'),
(6, 2, 'EDITOR'),
(6, 3, 'REVIEWER'),
(7, 5, 'AUTHOR'),
(7, 1, 'EIC'),
(7, 2, 'EDITOR'),
(8, 10, 'AUTHOR'),
(8, 7, 'EDITOR'),
(9, 5, 'AUTHOR'),
(9, 8, 'EIC'),
(9, 7, 'EDITOR'),
(9, 9, 'REVIEWER'),
(10, 10, 'AUTHOR'),
(10, 1, 'EIC'),
(10, 2, 'EDITOR'),
(10, 3, 'REVIEWER');

INSERT INTO manuscript_review (
    manuscript_id, reviewer_id, round, reviewer_comment, reviewer_comment_file_url,
    author_response_file_url, author_comment, review_date, author_response_date
)
VALUES
(1, 3, 1, 'Excellent study with clear methodology.', 'http://example.com/rev1.pdf', 'http://example.com/res1.pdf', 'Thank you!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 6, 1, 'Needs clarification in methods.', 'http://example.com/rev2.pdf', 'http://example.com/res2.pdf', 'Clarified section 2.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 6, 1, 'Interesting results.', 'http://example.com/rev3.pdf', 'http://example.com/res3.pdf', 'Added extra details.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 9, 1, 'Check figures for accuracy.', 'http://example.com/rev4.pdf', 'http://example.com/res4.pdf', 'Updated figures.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 9, 1, 'Needs major restructuring.', 'http://example.com/rev5.pdf', 'http://example.com/res5.pdf', 'Will restructure.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 6, 1, 'Solid work, minor typos.', 'http://example.com/rev6.pdf', 'http://example.com/res6.pdf', 'Fixed typos.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);