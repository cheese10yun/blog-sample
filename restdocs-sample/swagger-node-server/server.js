const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const fs = require('fs');

const app = express();

const openapiDir = path.join(__dirname, 'openapi');
const files = fs.readdirSync(openapiDir);

// 'openapi3'로 시작하는 yaml 파일만 필터링
const openapiFiles = files.filter(file => file.startsWith('openapi3') && file.endsWith('.yaml'));

// 파일 이름을 가공하여 options에 추가
const swaggerOptions = {
    urls: openapiFiles.map(file => {
        const name = file.replace('openapi3-', '').replace('.yaml', '');
        return { url: `/openapi/${file}`, name: name }
    })
};

const options = {
    explorer: true,
    swaggerOptions: swaggerOptions
};

app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(null, options));
app.use('/openapi', express.static(openapiDir));

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});