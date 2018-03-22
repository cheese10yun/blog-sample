#!/bin/bash

pm2 reload loopback-board;
echo 'Reload pm2 demon...';
sleep 1;
echo 'All Done!'
exit;
