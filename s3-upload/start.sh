#!/bin/bash

pm2 delete node_yun;
pm2 bin/www -i 0 --name node_yun;
echo 'start node_yun by pm2';
sleep 1;
sudo service nginx start;
echo 'Start nginx server...';
echo 'All Done!';
exit;