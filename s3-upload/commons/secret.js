module.exports = {
  'secret' :  '',
  'db_info': {
    local: { // localhost
      host: 'localhost',
      port: '3306',
      user: 'root',
      password: '',
      database: 'node'
    },
    real: { // real
      host: '',
      port: '',
      user: '',
      password: '',
      database: ''
    },
    dev: { // dev
      host: '',
      port: '',
      user: '',
      password: '',
      database: ''
    }
  },
  'federation' : {
    'naver' : {
      'client_id' : '11',
      'secret_id' : '11',
      'callback_url' : '/auth/login/naver/callback'
    },
    'facebook' : {
      'client_id' : '11',
      'secret_id' : '11',
      'callback_url' : '/auth/login/facebook/callback'
    },
    'kakao' : {
      'client_id' : '11',
      'callback_url' : '/auth/login/kakao/callback'
    }
  }
};