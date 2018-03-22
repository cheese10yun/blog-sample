#!/bin/bash

pm2 stop node_yun;
echo 'Stop node_yun by pm2';
sleep 1;
sudo service nginx stop;
echo 'Stop nginx server...';
echo 'All Done!';
exit;