MASTER_NODE='master'
SLAVE01_NODE='slave'

EXEC_MASTER="docker exec ${MASTER_NODE} mysql -uroot -proot -N -e "
EXEC_SLAVE01="docker exec ${SLAVE01_NODE} mysql -uroot -proot -e "

## For Replication
${EXEC_MASTER} "CREATE USER 'repl'@'%' IDENTIFIED BY 'repl'" 2>&1 | grep -v "Using a password"
${EXEC_MASTER} "GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%'" 2>&1 | grep -v "Using a password"

${EXEC_SLAVE01} "reset master" 2>&1 | grep -v "Using a password"
${EXEC_SLAVE01} "CHANGE MASTER TO MASTER_HOST='${MASTER_NODE}', \
MASTER_USER='repl', MASTER_PASSWORD='repl', \
MASTER_AUTO_POSITION=1" 2>&1 | grep -v "Using a password"
${EXEC_SLAVE01} "START SLAVE" 2>&1 | grep -v "Using a password"