// server.js
const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcrypt'); // untuk hash password
const app = express();
const port = 3000;
const saltRounds = 10;

// Middleware parsing JSON
app.use(express.json());

// Koneksi Database
const db = new sqlite3.Database('./database.db', (err) => {
  if (err) {
    console.error('❌ Gagal konek ke database:', err.message);
  } else {
    console.log('✅ Terkoneksi ke SQLite database.');
  }
});

// Buat tabel users jika belum ada
db.run(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE,
    password TEXT
  )
`, () => {
  // Setelah tabel dibuat, buat user default
  const defaultUsername = "pengguna";
  const defaultPassword = "pengguna123";

  bcrypt.hash(defaultPassword, saltRounds, (err, hash) => {
    if (err) return console.error('❌ Gagal hash password default:', err.message);

    // INSERT OR IGNORE → kalau sudah ada username yang sama, tidak diubah
    db.run(
      'INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)',
      [defaultUsername, hash],
      function (err) {
        if (err) {
          console.error('❌ Gagal insert user default:', err.message);
        } else {
          console.log('✅ User default (pengguna/pengguna123) siap digunakan.');
        }
      }
    );
  });
});

// 🔑 REGISTER
app.post('/register', (req, res) => {
  const { username, password } = req.body;
  if (!username || !password)
    return res.status(400).json({ message: 'Isi username dan password.' });

  bcrypt.hash(password, saltRounds, (err, hash) => {
    if (err) return res.status(500).json({ message: '❌ Gagal mengenkripsi password.' });

    db.run(
      'INSERT INTO users (username, password) VALUES (?, ?)',
      [username, hash],
      function (err) {
        if (err)
          return res
            .status(400)
            .json({ message: '❌ Username sudah terdaftar atau gagal register.' });
        res.json({ message: '✅ User berhasil didaftarkan.', userId: this.lastID });
      }
    );
  });
});

// 🔒 LOGIN
app.post('/login', (req, res) => {
  const { username, password } = req.body;
  if (!username || !password)
    return res.status(400).json({ message: 'Isi username dan password.' });

  db.get('SELECT * FROM users WHERE username = ?', [username], (err, user) => {
    if (err) return res.status(500).json({ message: '❌ Error pada server.' });
    if (!user)
      return res.status(401).json({ message: '❌ Username atau password salah.' });

    bcrypt.compare(password, user.password, (err, result) => {
      if (err)
        return res.status(500).json({ message: '❌ Error saat membandingkan password.' });

      if (result) {
        res.json({ message: '✅ Login berhasil', user: user.username });
      } else {
        res.status(401).json({ message: '❌ Username atau password salah.' });
      }
    });
  });
});

// Jalankan server agar bisa diakses dari Android
app.listen(port, '0.0.0.0', () => {
  console.log(`🚀 Server berjalan di http://0.0.0.0:${port}`);
  console.log('📱 Akses dari Android via: http://<IP-Laptop>:3000');
});
