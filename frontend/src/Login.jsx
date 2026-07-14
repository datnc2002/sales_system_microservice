import React, { useState } from 'react';
import api from './api';

export default function Login({ setToken }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/auth/login', { username, password });
      const token = res.data.data.token;
      localStorage.setItem('token', token);
      setToken(token);
    } catch (err) {
      alert('Login failed');
    }
  };

  const handleRegister = async () => {
    try {
      await api.post('/auth/register', {
        username,
        password,
        email: `${username}@example.com`,
        fullName: username
      });
      alert('Registered! Now login.');
    } catch (err) {
      alert('Register failed');
    }
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '100px' }}>
      <div className="card">
        <h2 className="title">Login / Register</h2>
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Username</label>
            <input value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button type="submit">Login</button>
            <button type="button" onClick={handleRegister} style={{ background: '#6b7280' }}>Register</button>
          </div>
        </form>
      </div>
    </div>
  );
}
