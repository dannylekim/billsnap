-- noinspection SqlResolveForFile
INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (1000, 'test@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (2000, 'userdetails@service.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
                    'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (3000, 'user@user.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', 'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (4000, 'paymentowed@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (5000, 'paymentOwedToMe@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789',
        current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent, tip_amount, split_by, location_id, active)
          VALUES (1000, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent, tip_amount, split_by, location_id, active)
          VALUES (1001, 'test', 1000, 1000, 'RESOLVED', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent, tip_amount, split_by, location_id, active)
          VALUES (1002, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1003, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bills_vs_accounts (bill_id, account_id, percentage) VALUES (1001, 1000, 100);
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage) VALUES (1000, 1000, 100);
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage) VALUES (1002, 4000, 100);
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage)
VALUES (1003, 5000, 100);


INSERT INTO item (id, bill_id, name, cost) VALUES (1000, 1002, 'potatoes', 69.00);
INSERT INTO item (id, bill_id, name, cost) VALUES (1001, 1002, 'Northern Lights Canabis Indica', 420.00);
INSERT INTO item (id, bill_id, name, cost) VALUES (1002, 1002, 'hot dogs', 3.00);
INSERT INTO item (id, bill_id, name, cost) VALUES (1003, 1002, 'knife', 130.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1004, 1003, 'culeros', 69.00);

INSERT INTO items_vs_accounts (item_id, account_id, percentage) VALUES (1000, 2000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage) VALUES (1001, 2000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage) VALUES (1002, 3000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage) VALUES (1003, 3000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1004, 5000, 100.0);


