/**
 * Created by cheese on 2017. 1. 6..
 */

var
    express = require('express'),
    router = express.Router(),
    mysql_dbc = require('../commons/db_con')(),
    connection = mysql_dbc.init(),
    bcrypt = require('bcrypt'),
    async = require('async'),
    Upload = require('../service/UploadService'),
    API_Call = require('../service/API_Call')('another');


router.post('/login', function (req, res, next) {
    
    var
        user_id = req.body.user_id,
        password = req.body.password;
    
    connection.query('select *from `user` where `user_id` = ?;', user_id, function (err, result) {
        if (err) {
            console.log('err :' + err);
        } else {
            console.log(result);
            if (result.length === 0) {
                res.json({success: false, msg: '해당 유저가 존재하지 않습니다.'})
            } else {
                if (!bcrypt.compareSync(password, result[0].password)) {
                    res.json({success: false, msg: '비밀번호가 일치하지 않습니다.'});
                } else {
                    res.json({success: true});
                }
            }
        }
    });
});


router.post('/login/another/api', function (req, res) {
    var
        user_id = req.body.user_id,
        password = req.body.password;
    
    API_Call.login(user_id, password, function (err, result) {
        if (!err) {
            res.json(result);
        } else {
            res.json(err);
        }
    });
});

router.delete('/crontab', function (req, res) {
    var sql = req.body.sql;
    connection.query(sql, function (err, result) {
        if (err) {
            res.json({
                success: false,
                err: err
            });
        } else {
            console.log('Delete Success');
            res.json({
                success: true,
                msg: 'Delete Success'
            })
        }
    });
});

router.post('/upload', function (req, res) {
    var tasks = [
        function (callback) {
            Upload.formidable(req, function (err, files, field) {
                console.log(err, files);
                callback(err, files);
            })
        },
        function (files, callback) {
            Upload.s3(files, function (err, result) {
                callback(err, files);
            });
        }
    ];
    async.waterfall(tasks, function (err, result) {
        // console.log(err);
        if(!err){
            res.json({success:true, msg:'업로드 성공'})
        }else{
            res.json({success:false, msg:'실패', err:err})
        }
    });
});
module.exports = router;