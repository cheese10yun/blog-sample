#!/bin/bash

pm2 reload node_yun;
echo 'Reload pm2 demon...';
sleep 1;
sudo service nginx restart;
echo 'Restart nginx server...';
echo 'All Done!'
exit;