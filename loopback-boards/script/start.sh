#!/bin/bash

pm2 delete loopback-board;
pm2 start ../server/server.js -i 0 --name loopback-board;
echo 'start loopback-board by pm2';
sleep 1;
echo 'All Done!';
exit;
