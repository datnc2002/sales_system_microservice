import React, { useState, useEffect } from 'react';
import api from './api';

export default function Products() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await api.get('/products');
      // Product listing response structure might be { data: { content: [...] } } or { data: [...] }
      const data = res.data.data;
      setProducts(data.content || data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleBuy = async (productId) => {
    try {
      await api.post('/orders', {
        items: [{ productId, quantity: 1 }]
      });
      alert('Order placed successfully!');
    } catch (err) {
      alert('Error placing order');
    }
  };

  return (
    <div className="container">
      <div className="flex-between title">
        <h2>Products</h2>
      </div>
      <div className="grid">
        {products.map(p => (
          <div key={p.id} className="card product-card">
            <img src={p.imageUrl || 'https://via.placeholder.com/200'} alt={p.name} />
            <h3>{p.name}</h3>
            <p className="price">{Number(p.price * 1000).toLocaleString('vi-VN')} ₫</p>
            <button onClick={() => handleBuy(p.id)} style={{ width: '100%' }}>Buy Now</button>
          </div>
        ))}
      </div>
      {products.length === 0 && <p>No products available. Add some via API or DB!</p>}
    </div>
  );
}
