-- We only support Postgresql at present

-- Create platformlayer user
CREATE DATABASE platformlayer;
CREATE ROLE platformlayer_ops PASSWORD 'platformlayer-password' INHERIT LOGIN;
GRANT ALL ON database platformlayer TO platformlayer_ops;


