-- noinspection SqlResolveForFile
INSERT INTO account VALUES (1000, 'test@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE',
 '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account VALUES (2000, 'userdetails@service.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
 'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account VALUES (3000, 'user@user.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK',
 'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account VALUES (4000, 'paymentowed@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE',
 '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO bill
VALUES (1000, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bill
VALUES (1001, 'test', 1000, 1000, 'RESOLVED', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bill
VALUES (1002, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bills_vs_accounts VALUES (1001, 1000, 100);
INSERT INTO bills_vs_accounts VALUES (1000, 1000, 100);
INSERT INTO bills_vs_accounts VALUES (1002, 4000, 100);

INSERT INTO item VALUES (1000, 1002, 'potatoes', 69.00);
INSERT INTO item VALUES (1001, 1002, 'Northern Lights Canabis Indica', 420.00);
INSERT INTO item VALUES (1002, 1002, 'hot dogs', 3.00);
INSERT INTO item VALUES (1003, 1002, 'knife', 130.00);

INSERT INTO items_vs_accounts VALUES (1000, 2000, 100.0);
INSERT INTO items_vs_accounts VALUES (1001, 2000, 100.0);
INSERT INTO items_vs_accounts VALUES (1002, 3000, 100.0);
INSERT INTO items_vs_accounts VALUES (1003, 3000, 100.0);

