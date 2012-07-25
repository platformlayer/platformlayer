create table scheduler_job (id serial,
							key varchar,
							schedule_interval varchar, 
							schedule_base varchar,
							task_target varchar,
							task_endpoint_url varchar,
							task_endpoint_project varchar,
							task_endpoint_secret bytea,
							task_endpoint_token varchar,
							task_endpoint_keys varchar,
							task_action_name varchar,
							primary key (key));
grant all on scheduler_job to platformlayer_ops;
grant all on scheduler_job_id_seq to platformlayer_ops;
