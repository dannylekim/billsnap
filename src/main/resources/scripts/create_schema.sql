CREATE TABLE IF NOT EXISTS example
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(250) NOT NULL,
    last_name  VARCHAR(250) NOT NULL,
    career     VARCHAR(250) DEFAULT NULL
);

-- Please note that in H2 we do not have functions except for java defined ones. Therefore, if we want this test db to be exactly
-- the same as the production db, there must be some extra written code here in java notation.

create type if not exists account_status as enum ('UNREGISTERED', 'REGISTERED');

create type if not exists split_type as enum ('BALANCE', 'ITEM');

create type if not exists bill_status as enum ('RESOLVED', 'OPEN');

create type if not exists group_role as enum ('ADMIN', 'MEMBER');

create type if not exists gender as enum ('M', 'F');

create table if not exists location
(
    id          int auto_increment primary key,
    name        varchar(50)  null,
    description varchar(100) null,
    address     varchar(50)  null,
    city        varchar(20)  null,
    state       varchar(20)  null,
    country     varchar(20)  null,
    postal_code varchar(10)  null,
);

create table if not exists account
(
    id           int auto_increment primary key,
    email        varchar(50)    null,
    password     varchar(20)    null,
    title        varchar(3)     null,
    first_name   varchar(30)    not null,
    middle_name  varchar(20)    null,
    last_name    varchar(30)    not null,
    gender       gender         null,
    phone_number varchar(20)    null,
    birth_date   date           null,
    status       account_status not null,
    location_id  integer
        constraint "USER_location_id_fkey"
            references location,
    constraint registered_not_null_email_password
        check (((status = 'REGISTERED') AND (email IS NOT NULL) AND (password IS NOT NULL)) OR
               ((status = 'UNREGISTERED') AND (password IS NULL)))
);

create table if not exists bill
(
    id          int auto_increment primary key,
    name        varchar(30)    null,
    responsible integer        not null,
    creator     integer        not null,
    status      varchar(15)    not null,
    created     timestamp with time zone default current_timestamp,
    updated     timestamp with time zone default current_timestamp,
    category    varchar(20)    null,
    company     varchar(20)    null,
    occurrence  integer        null,
    tip_percent numeric(6, 4)  null,
    tip_amount  numeric(14, 2) null,
    split_by    split_type   not null,
    location_id integer
        constraint "BILLS_location_id_fkey"
            references location,
    active      boolean        not null,
    constraint only_one_tip_method
        check ((tip_percent IS NULL) <> (tip_amount IS NULL))
);

comment on column bill.occurrence is 'repeat every x days where x = occurrence';

create table if not exists tax
(
    bill_id    integer        not null
        constraint "TAX_bill_id_fkey"
            references bill,
    "order"    integer        not null,
    amount     numeric(14, 2) null,
    percentage numeric(6, 4)  null,
    constraint unique_orders_to_bill
        primary key (bill_id, "order"),
    constraint one_of_amount_or_percentage
        check ((amount IS NULL) <> (percentage IS NULL))
);

create table if not exists "group"
(
    id              int auto_increment primary key,
    name            varchar(30) not null,
    created         timestamp with time zone default current_timestamp,
    updated         timestamp with time zone default current_timestamp,
    approval_option boolean     not null
);

create table if not exists groups_vs_accounts
(
    group_id   integer     not null
        constraint "GROUPS_VS_USERS_group_id_fkey"
            references "group",
    account_id integer     not null
        constraint "GROUPS_VS_USERS_user_id_fkey"
            references account,
    role       group_role not null,
    constraint "GROUPS_VS_USERS_pkey"
        primary key (group_id, account_id)
);

create table if not exists bills_vs_groups
(
    group_id integer not null
        constraint "BILLS_VS_GROUPS_group_id_fkey"
            references "group",
    bill_id  integer not null
        constraint "BILLS_VS_GROUPS_bill_id_fkey"
            references bill,
    constraint "BILLS_VS_GROUPS_pkey"
        primary key (group_id, bill_id)
);

create table if not exists items
(
    id      int auto_increment primary key,
    bill_id integer        not null
        constraint "ITEMS_bill_id_fkey"
            references bill,
    name    varchar(30)    not null,
    cost    numeric(14, 2) not null
);

create table if not exists items_vs_accounts
(
    item_id    integer       not null
        constraint "ITEM_VS_USERS_item_id_fkey"
            references items,
    account_id integer       not null
        constraint "ITEM_VS_USERS_user_id_fkey"
            references account,
    percentage numeric(4, 4) not null,
    constraint "ITEM_VS_USERS_pkey"
        primary key (item_id, account_id)
);

create table if not exists bills_vs_accounts
(
    bill_id    integer       not null
        constraint "BILLS_VS_USERS_bill_id_fkey"
            references bill,
    account_id integer       not null
        constraint "BILLS_VS_USERS_user_id_fkey"
            references account,
    percentage numeric(4, 4) null,
    constraint "BILLS_VS_USERS_pkey"
        primary key (bill_id, account_id),

);