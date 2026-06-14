const express = require('express');
const cors = require('cors');
const app = express();
const PORT = 8080;

app.use(cors());
app.use(express.json());

app.use('/api', require('./routes/auth'));
app.use('/api', require('./routes/food'));
app.use('/api', require('./routes/ingredients'));
app.use('/api', require('./routes/status'));
app.use('/api', require('./routes/packaging'));

// Pass-through token
const axios = require('axios');
app.get('/api/token', async (req, res) => {
  try {
    const r = await axios.get('http://localhost:1234/api/token');
    res.json(r.data);
  } catch { res.status(500).json({ message: 'Token fetch failed' }); }
});

app.get('/api/hello', (req, res) => {
  res.json({
    service: 'CrewAPI',
    base_url: `http://localhost:${PORT}/api`,
    endpoints: [
      'POST /api/login              - body: { email }',
      'GET  /api/credentials        - list all authorized users',
      'GET  /api/food               - query: month, year',
      'GET  /api/ingredients/:id    - ingredients filtered by food id',
      'GET  /api/status             - packaging status messages',
      'GET  /api/packaging          - query: option',
      'GET  /api/token              - remember token pass-through'
    ]
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`CrewAPI running → http://localhost:${PORT}`);
  console.log(`Endpoint docs  → http://localhost:${PORT}/api/hello`);
});