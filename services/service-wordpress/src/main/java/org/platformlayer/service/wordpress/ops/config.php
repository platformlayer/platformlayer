<?php
define('DB_NAME', '${databaseName}');
define('DB_USER', '${databaseUser}');
define('DB_PASSWORD', '${databasePassword}');
define('DB_HOST', '${databaseHost}');
define('SECRET_KEY', '${secretKey}');

#This will disable the update notification.
define('WP_CORE_UPDATE', false);

$table_prefix  = 'wp_';
$server = DB_HOST;
$loginsql = DB_USER;
$passsql = DB_PASSWORD;
$base = DB_NAME;
$upload_path = "${uploadPath}";
$upload_url_path = "http://${domainName}/wp-uploads";
?>

