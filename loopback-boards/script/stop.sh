#!/bin/bash

pm2 stop loopback-board;
echo 'Stop loopback-board by pm2';
sleep 1;
sudo service nginx stop;
echo 'Stop nginx server...';
echo 'All Done!';
exit;
