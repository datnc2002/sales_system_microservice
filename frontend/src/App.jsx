import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import Login from './Login';
import Products from './Products';
import Orders from './Orders';

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [balance, setBalance] = useState(null);

  useEffect(() => {
    if (token) {
      fetchProfile();
    }
  }, [token]);

  const fetchProfile = async () => {
    try {
      // Import api locally or use existing fetch logic, we can just use our api client
      const { default: api } = await import('./api');
      const res = await api.get('/users/me');
      setBalance(res.data.data.balance);
    } catch (err) {
      console.error(err);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setBalance(null);
  };

  return (
    <BrowserRouter>
      {token && (
        <nav>
          <h2>E-Commerce</h2>
          <Link to="/">Products</Link>
          <Link to="/orders">Orders</Link>
          <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {balance !== null && <span>Ví: {Number(balance).toLocaleString('vi-VN')} ₫</span>}
            <button onClick={logout} style={{ background: '#ef4444' }}>Logout</button>
          </div>
        </nav>
      )}
      
      <Routes>
        {!token ? (
          <Route path="*" element={<Login setToken={setToken} />} />
        ) : (
          <>
            <Route path="/" element={<Products />} />
            <Route path="/orders" element={<Orders />} />
            <Route path="*" element={<Navigate to="/" />} />
          </>
        )}
      </Routes>
    </BrowserRouter>
  );
}
