create table users (id serial, key text, password bytea, secret bytea, public_key bytea, private_key bytea, primary key (id));
create unique index users_index_key on users (key);
grant all on users to platformlayer_auth;
grant all on users_id_seq to platformlayer_auth;

create table projects (id serial, key text, secret bytea, metadata bytea, public_key bytea, private_key bytea, primary key (id));
create unique index projects_index_key on projects (key);
grant all on projects to platformlayer_auth;
grant all on projects_id_seq to platformlayer_auth;

create table user_projects (user_id int, project_id int);
create unique index user_projects_index on user_projects (user_id, project_id);
grant all on user_projects to platformlayer_auth;

create table service_accounts (subject varchar, public_key bytea);
create index service_accounts_index on service_accounts (public_key);
grant all on service_accounts to platformlayer_auth;

