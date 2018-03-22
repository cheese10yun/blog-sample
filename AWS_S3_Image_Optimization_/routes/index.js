const express = require('express');
const router = express.Router();
const async = require('async');
const Upload = require('../service/UploadService');


/* GET home page. */
router.get('/', (req, res) => {
  res.render('index', {title: 'Express'});
});

router.post('/upload', (req, res) => {
  const tasks = [
    (callback) => {
      Upload.formidable(req, (err, files, fields) => {
        callback(err, files, fields);
      });
    },
    (files, fields, callback) => {
      Upload.optimize(files, (err) => {
        callback(err, files, fields);
      });
    },
    (files, fields, callback) => {
      Upload.s3(files, 'AWS S3 Key ...', (err, result) => {
        callback(err, result)
      });
    }
  ];
  async.waterfall(tasks, (err) => {
    if (!err) {
      res.json({success: true, msg: '업로드 성공'})
    } else {
      res.json({success: false, msg: '업로드 실패'})
    }
  });
});


module.exports = router;

