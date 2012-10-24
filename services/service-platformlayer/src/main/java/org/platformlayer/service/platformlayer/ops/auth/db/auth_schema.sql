create table users (id serial, key text, password bytea, secret bytea, public_key bytea, private_key bytea, primary key (id));
create unique index users_index_key on users (key);
grant all on users to platformlayer_auth;
grant all on users_id_seq to platformlayer_auth;

create table projects (id serial, key text, secret bytea, metadata bytea, public_key bytea, private_key bytea, pki_private bytea, pki_cert bytea, primary key (id));
//alter table projects add column pki_private bytea, add column pki_cert bytea;
create unique index projects_index_key on projects (key);
grant all on projects to platformlayer_auth;
grant all on projects_id_seq to platformlayer_auth;


create table user_projects (user_id int, project_id int, roles varchar);
// alter table user_projects add column roles varchar;
create unique index user_projects_index on user_projects (user_id, project_id);
grant all on user_projects to platformlayer_auth;

create table service_accounts (subject varchar, public_key bytea);
create index service_accounts_index on service_accounts (public_key);
grant all on service_accounts to platformlayer_auth;

create table user_cert (id int, user_id int, public_key_hash bytea, primary key (id));
create index user_cert_index_public_key_hash on user_cert (public_key_hash);
grant all on user_cert to platformlayer_auth;
