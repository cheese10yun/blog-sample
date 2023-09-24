const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const fs = require('fs');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const openapiDir = path.join(__dirname, 'openapi');
const files = fs.readdirSync(openapiDir);

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

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
