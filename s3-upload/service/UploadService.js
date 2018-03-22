/**
 * Created by cheese on 2017. 2. 7..
 */

var
    formidable = require('formidable'),
    AWS = require('aws-sdk'),
    Upload = {};
AWS.config.region = 'ap-northeast-2'; //지역 서울 설정
var s3 = new AWS.S3();
var form = new formidable.IncomingForm({
    encoding: 'utf-8',
    multiples: true,
    keepExtensions: false //확장자 제거
});
/*S3 버킷 설정*/
var params = {
    Bucket: 'BucketName',
    Key: null,
    ACL: 'public-read',
    Body: null
};
Upload.formidable = function (req, callback) {
    
    form.parse(req, function (err, fields, files) {
    });
    
    form.on('error', function (err) {
        callback(err, null);
    });
    
    form.on('end', function () {
        callback(null, this.openedFiles);
    });
    
    form.on('aborted', function () {
        callback('form.on(aborted)', null);
    });
};
Upload.s3 = function (files, callback) {
    params.Key = 'test/'+files[0].name;
    params.Body = require('fs').createReadStream(files[0].path);

    s3.upload(params, function (err, result) {
        callback(err, result);
    });
};
module.exports = Upload;