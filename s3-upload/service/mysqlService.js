/**
 * Created by cheese on 2017. 1. 31..
 */

var mysql_dbc = require('../commons/db_con')();
var connection = mysql_dbc.init();

var bcrypt = require('bcrypt');

var mysql = {};

mysql.multipleInsert = function () {
    var stmt_multiple_insert = 'insert into `user` (`user_id`, `password`, `nickname`, `email`, `signup_dt`) values ?;'; // 쿼리문
    
    var values = [
        ['user_001,', 'password_01', 'nickname_01', 'email_01@test.com', '2016-10-10 16:10:22'],
        ['user_002,', 'password_02', 'nickname_02', 'email_02@test.com', '2016-10-10 16:10:22'],
        ['user_003,', 'password_03', 'nickname_03', 'email_03@test.com', '2016-10-10 16:10:22'],
        ['user_004,', 'password_04', 'nickname_04', 'email_04@test.com', '2016-10-10 16:10:22'],
        ['user_005,', 'password_05', 'nickname_05', 'email_05@test.com', '2016-10-10 16:10:22']
    ];
    
    var str_query = connection.query(stmt_multiple_insert, [values], function (err, result) {
        if (err) {
            console.log(err);
        } else {
            console.log(str_query.sql);
        }
    });
};


module.exports = mysql;