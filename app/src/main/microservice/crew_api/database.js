const Database = require('better-sqlite3');
const db = new Database('crew.db');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    authorized INTEGER DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )
`);

// admin@food.my is ALWAYS authorized per requirements
db.prepare(`INSERT OR IGNORE INTO users (email, authorized) VALUES ('admin@food.my', 1)`).run();

module.exports = db;


