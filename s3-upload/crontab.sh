#!/bin/bash

echo 'start delete table info...';

curl -H "Content-Type: application/json" -X DELETE -d '

  {"sql":"delete from `user` where  `user_id` is not null;"}

  ' http://localhost:3500/api/v1/crontab

echo 'success...';