create table services (id serial, key text, primary key (key));
grant all on services to platformlayer_ops;
grant all on services_id_seq to platformlayer_ops;

create table project_codes (id serial, key text, primary key (key));
grant all on project_codes to platformlayer_ops;
grant all on project_codes_id_seq to platformlayer_ops;

create table metadata_keys (id serial, key text, primary key (key));
grant all on metadata_keys to platformlayer_ops;
grant all on metadata_keys_id_seq to platformlayer_ops;

create table item_types (id serial, key text, primary key (key));
grant all on item_types to platformlayer_ops;
grant all on item_types_id_seq to platformlayer_ops;

create table service_authorizations (service int, project int, data text, primary key (service, project));
grant all on service_authorizations to platformlayer_ops;

create table service_metadata (service int, project int, metadata_key int, data bytea, secret bytea, primary key (service, project, metadata_key));
grant all on service_metadata to platformlayer_ops;

create table project_metadata (project int, metadata_key int, data bytea, secret bytea, primary key (project, metadata_key));
grant all on project_metadata to platformlayer_ops;

create table items (service int, model int, project int, id serial, key varchar(512), state int, data bytea, secret bytea, primary key (service, model, project, id));
grant all on items_id_seq to platformlayer_ops;
grant all on items to platformlayer_ops;

create table item_tags (id serial, service int, model int, project int, item int, key text, data text);
create index item_tags_index on item_tags (project, service, model, item);
grant all on item_tags to platformlayer_ops;
grant all on item_tags_id_seq to platformlayer_ops;

-- item is type text so that (1) we can see logs for deleted items and (2) so that we can split this db
create table job_logs (service int, model int, account int, item text, jobstate int, data text);
create index job_logs_index on job_logs (account, service, model, item);
grant all on job_logs to platformlayer_ops;
