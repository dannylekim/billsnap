-- Database Script used for Production Database

create type account_status as enum ('UNREGISTERED', 'REGISTERED');

create type split_type as enum ('BALANCE', 'ITEM');

create type bill_status as enum ('RESOLVED', 'OPEN');

create type group_role as enum ('ADMIN', 'MEMBER');

create type gender as enum ('Male', 'Female', 'Other');

create table if not exists location
(
    id          serial not null
        constraint "LOCATION_pkey"
            primary key,
    name        varchar(50),
    description varchar(100),
    address     varchar(50),
    city        varchar(20),
    state       varchar(20),
    country     varchar(20),
    postal_code varchar(10),
    constraint at_least_one_value
        check (count_not_nulls(ARRAY [name, description, address, city, state, country, postal_code]) >= 1)
);

create table if not exists account
(
    id           serial         not null
        constraint "USER_pkey"
            primary key,
    email        varchar(50),
    password     varchar(20),
    first_name   varchar(30)    not null,
    middle_name  varchar(20),
    last_name    varchar(30)    not null,
    gender       gender,
    phone_number varchar(20),
    birth_date   date,
    status       account_status not null,
    created      timestamptz not null default clock_timestamp(),
    updated      timestamptz not null default clock_timestamp(),
    location_id  integer
        constraint "USER_location_id_fkey"
            references location,
    constraint registered_not_null_email_password
        check (((status = 'REGISTERED'::account_status) AND (email IS NOT NULL) AND (password IS NOT NULL)) OR
               ((status = 'UNREGISTERED'::account_status) AND (password IS NULL)))
);

create table if not exists bill
(
    id          serial      not null
        constraint "BILLS_pkey"
            primary key,
    name        varchar(30),
    responsible integer     not null
        constraint bill_responsible_id_fk
            references account,
    creator     integer     not null
        constraint bill_creator_id_fk
            references account,
    status      varchar(15) not null,
    created     timestamp with time zone default clock_timestamp(),
    updated     timestamp with time zone default clock_timestamp(),
    category    varchar(20),
    company     varchar(20),
    occurrence  integer,
    tip_percent numeric(6, 4),
    tip_amount  numeric(14, 2),
    split_by    split_type not null,
    location_id integer
        constraint "BILLS_location_id_fkey"
            references location,
    active      boolean     not null,
    constraint only_one_tip_method
        check ((tip_percent IS NULL) <> (tip_amount IS NULL))
);

comment on column bill.occurrence is 'repeat every x days where x = occurrence';

create table if not exists tax
(
    bill_id    integer not null
        constraint "TAX_bill_id_fkey"
            references bill,
    "order"    integer not null,
    amount     numeric(14, 2),
    percentage numeric(6, 4),
    constraint unique_orders_to_bill
        primary key (bill_id, "order"),
    constraint one_of_amount_or_percentage
        check ((amount IS NULL) <> (percentage IS NULL))
);

create table if not exists "group"
(
    id              serial      not null
        constraint "GROUPS_pkey"
            primary key,
    name            varchar(30) not null,
    created         timestamp with time zone default clock_timestamp(),
    updated         timestamp with time zone default clock_timestamp(),
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

create table if not exists item
(
    id      serial         not null
        constraint "ITEMS_pkey"
            primary key,
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
            references item,
    account_id integer       not null
        constraint "ITEM_VS_USERS_user_id_fkey"
            references account,
    percentage numeric(4, 4) not null,
    constraint "ITEM_VS_USERS_pkey"
        primary key (item_id, account_id)
);

create table if not exists bills_vs_accounts
(
    bill_id    integer not null
        constraint "BILLS_VS_USERS_bill_id_fkey"
            references bill,
    account_id integer not null
        constraint "BILLS_VS_USERS_user_id_fkey"
            references account,
    percentage numeric(4, 4),
    constraint "BILLS_VS_USERS_pkey"
        primary key (bill_id, account_id),
    constraint not_null_if_balance
        check ((is_split_by_balance(bill_id) AND (percentage IS NOT NULL)) OR
               ((NOT is_split_by_balance(bill_id)) AND (percentage IS NULL)))
);

create function count_not_nulls(p_array anyarray) returns bigint
    immutable
    language sql
as
$$
SELECT COUNT(x)
from unnest($1) as x
$$;

create function is_split_by_balance(bill_id integer) returns boolean
    language plpgsql
as
$$
begin
    select b.status = 'BALANCE' from "bill" b where b.id = bill_id;
end;
$$;



