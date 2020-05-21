-- this file has the needed scripts to use on the databases from version to version to make sure the code works. Simply annotate by version + the scripts needed on the db

-- sprint 11

alter table bills_vs_accounts
    add amount_paid numeric(14, 2);

create type payment_status_type as enum ('PAID', 'IN_PROGRESS');

alter table bills_vs_accounts
    add payment_status payment_status_type;

ALTER TYPE bill_status ADD VALUE 'IN_PROGRESS';