CREATE TABLE IF NOT EXISTS book (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    isbn_id VARCHAR(255),
    author_name VARCHAR(255),
    name VARCHAR(255) UNIQUE,
    folder_name VARCHAR(255) NOT NULL,
    published_year INT,
    created_time TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS member (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      login_id VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    name VARCHAR(255),
    introduction TEXT,
    rating INT,
    roles JSON
    );

CREATE TABLE IF NOT EXISTS quiz (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    question LONGTEXT,
                                    option_list JSON,
                                    answer INT,
                                    comment LONGTEXT,
                                    quiz_list_id BIGINT,
                                    submit_count INT,
                                    correct_count INT,
                                    FOREIGN KEY (quiz_list_id) REFERENCES quiz_list(id)
    );

CREATE TABLE IF NOT EXISTS quiz_list (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         book_id BIGINT,
                                         FOREIGN KEY (book_id) REFERENCES book(id)
    );

CREATE TABLE IF NOT EXISTS quiz_submit (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           member_id BIGINT,
                                           quiz_list_id BIGINT,
                                           submit_answer_list JSON,
                                           score INT,
                                           FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (quiz_list_id) REFERENCES quiz_list(id)
    );

CREATE TABLE IF NOT EXISTS scene (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     book_id BIGINT,
                                     photo_id VARCHAR(255),
    description LONGTEXT,
    plot_summary LONGTEXT,
    FOREIGN KEY (book_id) REFERENCES book(id)
    );