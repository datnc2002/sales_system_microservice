import React, { useState, useEffect } from 'react';
import api from './api';

export default function Orders() {
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      // Assuming GET /orders exists, if not we might not have list endpoint in api-tests, but standard is GET /orders
      const res = await api.get('/orders');
      const data = res.data.data;
      setOrders(data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCancel = async (orderId) => {
    try {
      await api.delete(`/orders/${orderId}`);
      alert('Order cancelled');
      fetchOrders();
      // Dispatch an event to update balance in App.jsx (optional, user can refresh)
      window.location.reload(); 
    } catch (err) {
      alert('Error cancelling order');
    }
  };

  const handlePay = async (orderId) => {
    try {
      await api.post(`/orders/${orderId}/pay`);
      alert('Order paid successfully!');
      fetchOrders();
      window.location.reload(); // Reload to update balance in App.jsx easily
    } catch (err) {
      alert(err.response?.data?.message || 'Error paying order');
    }
  };

  return (
    <div className="container">
      <h2 className="title">My Orders</h2>
      <div className="grid">
        {orders.map(o => (
          <div key={o.id} className="card">
            <h3>Order #{o.id}</h3>
            <p>
              Status: <span style={{
                padding: '2px 6px', borderRadius: '4px', fontSize: '0.8rem',
                background: o.status === 'PAID' ? '#22c55e' : o.status === 'CANCELLED' ? '#ef4444' : '#eab308'
              }}>{o.status}</span>
            </p>
            {o.paidAt && <p style={{ fontSize: '0.8rem', color: '#666' }}>Paid at: {new Date(o.paidAt).toLocaleString()}</p>}
            <p>Total: {Number(o.totalAmount * 1000).toLocaleString('vi-VN')} ₫</p>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
              {o.status === 'CREATED' && (
                <button onClick={() => handlePay(o.id)} style={{ background: '#22c55e', flex: 1 }}>Thanh toán</button>
              )}
              {o.status !== 'CANCELLED' && (
                <button onClick={() => handleCancel(o.id)} style={{ background: '#ef4444', flex: 1 }}>Hủy</button>
              )}
            </div>
          </div>
        ))}
      </div>
      {orders.length === 0 && <p>No orders yet.</p>}
    </div>
  );
}
