-- this file has the needed scripts to use on the databases from version to version to make sure the code works. Simply annotate by version + the scripts needed on the db

alter table bills_vs_accounts
    add amount_paid numeric(14, 2);