const apiBase = 'http://34.21.179.15:30080/api';

async function seed() {
  try {
    console.log('Registering admin...');
    try {
      await fetch(`${apiBase}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: "admin1",
          password: "password123",
          email: "admin1@example.com",
          fullName: "Admin User"
        })
      });
    } catch (e) {
      console.log('User might exist, trying to login...');
    }

    const loginRes = await fetch(`${apiBase}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: "admin1", password: "password123" })
    });
    
    if (!loginRes.ok) {
        const text = await loginRes.text();
        throw new Error(`Login failed: ${text}`);
    }
    
    const loginData = await loginRes.json();
    const token = loginData.data.token;
    console.log('Logged in successfully, token received.');

    const authHeaders = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };

    console.log('Creating Category...');
    const catRes = await fetch(`${apiBase}/categories`, {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify({ name: "Laptops", description: "High-end Laptops" })
    });
    
    let catId = 1;
    if (catRes.ok) {
      const catData = await catRes.json();
      catId = catData.data.id;
      console.log('Category created:', catId);
    } else {
        console.log('Category creation failed or exists, assuming ID 1');
    }

    const products = [
      { name: "MacBook Pro 16", price: 850, categoryId: catId, imageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8" },
      { name: "Dell XPS 15", price: 550, categoryId: catId, imageUrl: "https://images.unsplash.com/photo-1593642632823-8f785ba67e45" },
      { name: "ThinkPad X1 Carbon", price: 450, categoryId: catId, imageUrl: "https://images.unsplash.com/photo-1603302576837-37561b2e2302" }
    ];

    for (const p of products) {
      console.log(`Creating Product: ${p.name}`);
      const prodRes = await fetch(`${apiBase}/products`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
          name: p.name,
          description: p.name + " description",
          price: p.price,
          categoryId: p.categoryId,
          imageUrl: p.imageUrl
        })
      });

      if (prodRes.ok) {
        const pData = await prodRes.json();
        const pid = pData.data.id;
        console.log(`Product created with ID: ${pid}`);

        console.log(`Adding inventory for product ${pid}`);
        await fetch(`${apiBase}/inventory/${pid}`, {
          method: 'PUT',
          headers: authHeaders,
          body: JSON.stringify({ quantity: 50 })
        });
      } else {
          const t = await prodRes.text();
          console.error(`Failed to create product ${p.name}: ${t}`);
      }
    }
    console.log('Seeding completed!');
  } catch (error) {
    console.error('Seeding error:', error);
  }
}

seed();
