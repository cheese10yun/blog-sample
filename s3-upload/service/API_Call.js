/**
 * Created by cheese on 2017. 2. 5..
 */
/**
 * Created by yijaejun on 27/12/2016.
 */
var request = require('request');

module.exports = function (callee) {
    
    function API_Call(callee) {
        var OPTIONS = {
            headers: {'Content-Type': 'application/json'},
            url: null,
            body: null
        };
        
        const PORT = '3500';
        const BASE_PATH = '/api/v1';
        var HOST = null;
        
        (function () {
            switch (callee) {
                case 'dev':
                    HOST = 'https://dev-api.com';
                    break;
                case 'prod':
                    HOST = 'https://prod-api.com';
                    break;
                case 'another':
                    HOST = 'http://localhost';
                    break;
                default:
                    HOST = 'http://localhost';
            }
        })(callee);
        
        return {
            
            login : function (user_id, password, callback) {
                OPTIONS.url = HOST + ':' + PORT + BASE_PATH + '/login';
                OPTIONS.body = JSON.stringify({
                    "user_id": user_id,
                    "password": password
                });

                request.post(OPTIONS, function (err, res, result) {
                    statusCodeErrorHandler(res.statusCode, callback, result);
                });
            }
        };
    }
    
    function statusCodeErrorHandler(statusCode, callback , data) {
        switch (statusCode) {
            case 200:
                callback(null, JSON.parse(data));
                break;
            default:
                callback('error', JSON.parse(data));
                break;
        }
    }
    
    var INSTANCE;
    if (INSTANCE === undefined) {
        INSTANCE = new API_Call(callee);
    }
    return INSTANCE;
};