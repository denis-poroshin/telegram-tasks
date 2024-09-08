-- liquibase formatted sql

-- changeset dporoshin:1

CREATE TABLE notification_task
(
    id INTEGER PRIMARY KEY,
    id_chat INTEGER,
    text TEXT,
    data_time DATE
);