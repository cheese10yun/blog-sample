# node-yun

* PM2 Module...
* Nginx...
* Passport Login...
* Mysql...


##Proejct 실행 방법

```
npm install
npm start
```
## Databases 설정
```
$ mysql -u root -p
password input...
mysql> create database node
mysql> use node
exit
$ cd node_yun
$ mysql -u root -p node < node_yun.sql
```

###AWS EC2 Node + Nginx Setting
* AWS EC2 Node.js 설치 및 Nginx 연동은 [블로그 AWS EC2 Nginx Node.js 설정](http://engineeryun.tistory.com/entry/AWS-EC2-Nginx-Nodejs-%EC%84%A4%EC%A0%95)을 참조해주세요

###PM2 이용한 Node 프로세스 관리
* PM2 사용법, 명령어 및 start , strop, restart 쉘 스크립트를 통한 PM2제어는 [블로그 PM2 이용한 Node 프로세스 관리](http://engineeryun.tistory.com/entry/test)을 참조해주세요

###develop-mysql
* 본 branch는 mysql 연동 예제입니다. 예제에 대한소개는 [Node.JS + Mysql 연동](http://engineeryun.tistory.com/entry/NodeJS-Mysql-%EC%97%B0%EB%8F%99)설명을 참고하세요

###develop-passport
* 본 branch는 passport를 통한 로그인 인증 예제입니다. 예제에 대한소개는 [블로그 Node Passport를 이용한 Login](http://engineeryun.tistory.com/entry/Node-Passport%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-Login)설명을 참고하세요

###develop-passport-mysql
* 본 branch는 passport를 통한 로그인과 데이터베이스(mysql)예제입니다. 예제애 대한소개는 [블로그 Node Passport를 이용한 Login(2)](http://engineeryun.tistory.com/entry/Node-Passport%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-Login2)설명을 참고하세요

###develop-crontab-api
* 본 branch는 Crontab을 이용해서 특정 시점에Node API를 호출하는 내용입니다. 에제에대한 설명은 [블로그 Crontab을 이용한 노드 API 호출](http://engineeryun.tistory.com/entry/Crontab%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%85%B8%EB%93%9C-API-%ED%98%B8%EC%B6%9C)을 참고해주세요

###develop-passport-social-login
* 본 branch는 소셜로그인 기능으로 페이스북, 카카오, 네이버 아이드를 통한 회원 가입 및 로그인 처리에 대한 예제입니다. [블로그 Passport를 이용한 네이버, 카카오, 페이스북 로그인](http://engineeryun.tistory.com/entry/Passport%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%84%A4%EC%9D%B4%EB%B2%84-%EC%B9%B4%EC%B9%B4%EC%98%A4-%ED%8E%98%EC%9D%B4%EC%8A%A4%EB%B6%81-%EB%A1%9C%EA%B7%B8%EC%9D%B8-1)을 참고해주세요

###develop-passport-mysql-multiple-insert
* 본 bracnh는 반복문을 사용하지 않고 여러개를 insert하는 간
단한 예제입니다. [블로그 Node Mysql Multiple Insert](https://cheese10yun.github.io/mysql-multiple-insert)설명을 참고하세요

###develop-api-call
* 본 branch는 노드 서버에서 다른 API를 호출 예제입니다. [블로그](https://cheese10yun.github.io/API-CALL)설명을 참고하세요

###develop-s3-upload
* 본 branch는 노드서버에서 AWS S3 업로드 예제 입니다.[블로그 Node AWS S3 업로드](https://cheese10yun.github.io/Node-AWS-S3-Upload)설명을 참고허세요 
