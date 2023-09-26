const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const fs = require('fs');
const { createProxyMiddleware } = require('http-proxy-middleware');
const { spawn } = require('child_process');
const openapiDir = path.join(__dirname, 'openapi');
const files = fs.readdirSync(openapiDir);
const app = express();
const openapiFiles = files.filter(file => file.startsWith('openapi3') && file.endsWith('.yaml'));
const multer = require('multer');

openapiFiles.forEach(file => {
    const filePath = path.join(openapiDir, file);
    const document = YAML.load(filePath);

    document.components = document.components || {};

    if (!document.components.securitySchemes || !document.components.securitySchemes.ApiKeyAuth) {
        document.components.securitySchemes = {
            ApiKeyAuth: {
                type: 'apiKey',
                in: 'header',
                name: 'x-request-service'
            }
        };
    }
    fs.writeFileSync(filePath, YAML.stringify(document, 8, 2));
});

const swaggerOptions = {
    urls: openapiFiles.map(file => {
        return {
            url: `/openapi/${file}`,
            name: file.replace('openapi3-', '').replace('.yaml', '')
        }
    })
};

const options = {
    explorer: true,
    swaggerOptions: swaggerOptions
};

/**
 * 프록시 설정
 * Swagger UI는 XHR 기반으로 HTTP 통신을 진행하기 때문에 CORS 문제가 발생한다.
 * CORS를 우회하기 위해서 Service API 호출은 Node 서버를 기반으로 Proxy를 기반으로 통신하여 CORS를 우회한다.
 */
const serviceProxy = createProxyMiddleware({
    target: 'http://localhost:8080', // 실제 API 서버 주소 입력
    changeOrigin: true,
    context: (pathname) => pathname.includes('/api/')// 'member'가 포함되어 있는 경우만 프록시
});

app.use((req, res, next) => {
    if (req.path.includes('/api/')) {
        serviceProxy(req, res, next);
    } else {
        next();
    }
});


app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(null, options));
app.use('/openapi', express.static(openapiDir));

// openapi 파일이 변경돼었을 경우 리스타트를 위한 서버
app.get('/restart', (req, res) => {
    res.contentType('application/json');
    res.json({ message: "Restarting server..." });

    process.on('exit', function() {
        spawn(process.argv.shift(), process.argv, {
            cwd: process.cwd(),
            detached : true,
            stdio: "inherit"
        });
    });
    console.log('Server Restarting')
    process.exit();
});

// multer 설정: 저장 위치와 파일 이름 지정
const storage = multer.diskStorage({
    destination: function(req, file, cb) {
        cb(null, openapiDir);
    },
    filename: function(req, file, cb) {
        cb(null, file.originalname);
    }
});

const upload = multer({ storage: storage });

// 파일 업로드 API 엔드포인트
app.post('/upload', upload.single('file'), (req, res) => {

    // console.log(req.file())

    if (!req.file) {
        return res.status(400).json({ message: 'No file uploaded.' });
    }

    const uploadedFilePath = path.join(openapiDir, req.file.filename);
    console.log(`File uploaded to: ${uploadedFilePath}`);


    res.json({ message: 'File uploaded successfully.', path: uploadedFilePath });
});

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
