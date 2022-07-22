username="root"
if ! test -z $1;then
    username=$1
fi
mysql -u$username -p  -e "DROP database IF EXISTS fusion;create database fusion;source init.sql;"
