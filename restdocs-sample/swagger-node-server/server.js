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

// 프록시 설정
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
    process.exit();
});

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
