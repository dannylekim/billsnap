-- noinspection SqlResolveForFile
INSERT INTO account VALUES (1000, 'test@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE',
 '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);
INSERT INTO account VALUES (2000, 'userdetails@service.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
 'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);