-- noinspection SqlResolveForFile
INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (1001, 'nobills@inthisemail.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '5417894561', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (1000, 'test@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (2000, 'userdetails@service.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
                    'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (3000, 'user@user.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', 'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (4000, 'paymentowed@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (5000, 'user@hasbills.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
                    'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status, created, updated, location_id)
             VALUES (6000, 'user@withABill.com', '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
                     'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED',
                     current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (7000, 'associateitem@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789',
        current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (7001, 'associateitem2@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789',
        current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (7002, 'associateitem3@test.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789',
        current_date, 'REGISTERED', current_timestamp, current_timestamp, null);

INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (8000, 'paymentOwedToMe@email.com', 'notEncrypted', 'firstTest', 'middleTest', 'lastTest', 'MALE', '123456789',
        current_date, 'REGISTERED', current_timestamp, current_timestamp, null);


INSERT INTO account (id, email, password, first_name, middle_name, last_name, gender, phone_number, birth_date, status,
                     created, updated, location_id)
VALUES (9000, 'user@withADeclinedBill.com',
        '$2a$04$IV55Yhr.ICvWxGm/6hj8iua3gium/Yzyg0XBE8Nb2q1BvEzG21RiK', -- bcrypted 'somepass' 4 rounds
        'firstName', 'middleName', 'lastName', 'MALE', '123456789', current_date, 'REGISTERED',
        current_timestamp, current_timestamp, null);


INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1000, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1001, 'test', 1000, 1000, 'RESOLVED', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM',
        null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1002, 'test', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1003, 'multipleUsersAndItems', 6000, 6000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15,
        null, 'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1004, 'billForPaymentOwed', 1000, 1000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null,
        'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1005, 'test', 6000, 6000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1250, 'associateItemsBill', 7000, 7000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, null, 15,
        'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1251, 'associateItemsBill2', 7001, 7001, 'OPEN', current_timestamp, current_timestamp, null, null, 0, null, 15,
        'ITEM', null, 1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1006, 'test', 8000, 8000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1100, 'test', 5000, 5000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (2000, 'test', 5000, 5000, 'OPEN', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bill (id, name, responsible, creator, status, created, updated, category, company, occurrence, tip_percent,
                  tip_amount, split_by, location_id, active)
VALUES (1101, 'test', 5000, 5000, 'IN_PROGRESS', current_timestamp, current_timestamp, null, null, 0, 15, null, 'ITEM', null,
        1);

INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1100, 5000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1001, 1000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1000, 1000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1002, 4000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1003, 5000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (2000, 5000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (2000, 9000, 100, 'DECLINED');

INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1004, 4000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1002, 2000, 0, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1003, 6000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1005, 6000, 0, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1005, 5000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1006, 8000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1250, 7000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1251, 7001, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1100, 2000, 100, 'ACCEPTED');
INSERT INTO bills_vs_accounts (bill_id, account_id, percentage, status)
VALUES (1101, 5000, 100, 'ACCEPTED');

INSERT INTO item (id, bill_id, name, cost)
VALUES (1000, 1004, 'potatoes', 69.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1001, 1004, 'Northern Lights Canabis Indica', 420.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1002, 1004, 'hot dogs', 3.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1003, 1004, 'knife', 130.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1004, 1003, 'test', 450);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1005, 1003, 'anotherTest', 278);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1006, 1005, 'sashimi1', 20);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1007, 1005, 'sashimi2', 20);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1008, 1250, 'ItemAssociateItem', 999);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1009, 1251, 'ItemAssociateItem2', 100);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1010, 1251, 'ItemAssociateItem3', 100);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1011, 1006, 'shoes', 69.00);
INSERT INTO item (id, bill_id, name, cost)
VALUES (1012, 2000, 'shoes', 69.00);

INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1000, 2000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1001, 2000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1002, 3000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1003, 3000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1005, 6000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1004, 5000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1006, 5000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1007, 5000, 100.0);
INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1009, 7002, 100.0);

INSERT INTO items_vs_accounts (item_id, account_id, percentage)
VALUES (1011, 8000, 100.0);