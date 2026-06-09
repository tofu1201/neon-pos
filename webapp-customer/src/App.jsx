import { useState, useEffect } from 'react'
import axios from 'axios'
import Barcode from 'react-barcode'
import './index.css'

const API_BASE = window.location.port === '5173' ? 'http://127.0.0.1:8080/api' : '/api'

function App() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [activeCategory, setActiveCategory] = useState('')
  const [cart, setCart] = useState([])
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [showCart, setShowCart] = useState(false)
  const [orderComplete, setOrderComplete] = useState(null)

  useEffect(() => {
    fetchProducts()
  }, [])

  const fetchProducts = async () => {
    try {
      const res = await axios.get(`${API_BASE}/products`)
      const prods = res.data
      setProducts(prods)
      const cats = [...new Set(prods.map(p => p.category))]
      setCategories(cats)
      if (cats.length > 0) setActiveCategory(cats[0])
    } catch (e) {
      console.error(e)
    }
  }

  const handleAddToCart = (item) => {
    setCart(prev => {
      const existing = prev.find(p => p.productId === item.productId && p.note === item.note)
      if (existing) {
        return prev.map(p => p === existing ? { ...p, quantity: p.quantity + item.quantity } : p)
      }
      return [...prev, item]
    })
    setSelectedProduct(null)
  }

  const submitOrder = async () => {
    if (cart.length === 0) return
    try {
      const res = await axios.post(`${API_BASE}/orders`, {
        orderType: '外帶',
        paymentMethod: 'CASH',
        lines: cart
      })
      if (res.data.success) {
        setCart([])
        setShowCart(false)
        setOrderComplete(res.data.orderNo || `WEB-${res.data.orderId}`)
      }
    } catch (e) {
      console.error("Order submission failed:", e.response?.data || e.message)
      alert("訂單送出失敗，請重試或洽詢櫃台人員\n錯誤：" + (e.response?.data || e.message))
    }
  }

  if (orderComplete) {
    // We directly use orderComplete since it's now the orderNo
    const barcodeValue = orderComplete

    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: '20px', textAlign: 'center' }}>
        <div style={{ fontSize: '64px', marginBottom: '24px' }}>🛒</div>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: '16px' }}>訂單已保留！</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '32px' }}>請至櫃檯出示此條碼，完成結帳後廚房才會開始製作您的餐點哦！</p>
        <div className="glass-panel" style={{ padding: '24px', width: '100%', maxWidth: '300px', background: '#fff' }}>
          <Barcode 
            value={barcodeValue} 
            width={2} 
            height={60} 
            displayValue={true} 
            background="#ffffff"
            lineColor="#000000"
          />
        </div>
        <button className="action-btn" style={{ marginTop: '40px', maxWidth: '300px' }} onClick={() => setOrderComplete(null)}>返回首頁</button>
      </div>
    )
  }

  const filteredProducts = products.filter(p => p.category === activeCategory)
  const cartTotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0)
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <>
      <header className="app-header glass-panel">
        <div className="app-title">NEON 線上點餐</div>
      </header>

      <div className="category-scroller">
        {categories.map(cat => (
          <div 
            key={cat} 
            className={`category-btn ${activeCategory === cat ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat)}
          >
            {cat}
          </div>
        ))}
      </div>

      <div className="product-grid">
        {filteredProducts.map(product => (
          <div key={product.id} className="product-card glass-panel" onClick={() => setSelectedProduct(product)}>
            <div className="product-name">{product.name}</div>
            <div className="product-price">${product.price}</div>
            <div className="add-btn">+ 新增</div>
          </div>
        ))}
      </div>

      {cartCount > 0 && !showCart && (
        <div className="floating-cart" onClick={() => setShowCart(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div className="badge">{cartCount}</div>
            <div>查看購物車</div>
          </div>
          <div className="total">${cartTotal}</div>
        </div>
      )}

      {selectedProduct && (
        <ProductModal 
          product={selectedProduct} 
          onClose={() => setSelectedProduct(null)} 
          onAdd={handleAddToCart} 
        />
      )}

      {showCart && (
        <CartModal 
          cart={cart} 
          setCart={setCart}
          total={cartTotal}
          onClose={() => setShowCart(false)} 
          onSubmit={submitOrder} 
        />
      )}
    </>
  )
}

function ProductModal({ product, onClose, onAdd }) {
  const [quantity, setQuantity] = useState(1)
  const [note, setNote] = useState('')

  const handleAdd = () => {
    onAdd({
      productId: product.id,
      productName: product.name,
      sku: product.sku,
      unitPrice: product.price,
      taxRate: 0.05,
      quantity,
      note,
      modifiers: []
    })
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content slide-up" onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
          <h2>{product.name}</h2>
          <div style={{ color: 'var(--accent-cyan)', fontSize: '24px', fontWeight: '700' }}>${product.price}</div>
        </div>
        
        <div style={{ marginBottom: '24px' }}>
          <div style={{ marginBottom: '8px', color: 'var(--text-secondary)' }}>備註說明</div>
          <input 
            type="text" 
            className="modern-input" 
            placeholder="例如：少冰無糖、不要蔥..." 
            value={note}
            onChange={e => setNote(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '24px', marginBottom: '32px' }}>
          <button 
            style={{ width: '40px', height: '40px', borderRadius: '20px', background: 'rgba(255,255,255,0.1)', border: 'none', color: '#fff', fontSize: '20px' }}
            onClick={() => setQuantity(Math.max(1, quantity - 1))}
          >-</button>
          <div style={{ fontSize: '24px', fontWeight: '600' }}>{quantity}</div>
          <button 
            style={{ width: '40px', height: '40px', borderRadius: '20px', background: 'var(--accent-cyan-dim)', border: `1px solid var(--accent-cyan)`, color: 'var(--accent-cyan)', fontSize: '20px' }}
            onClick={() => setQuantity(quantity + 1)}
          >+</button>
        </div>

        <button className="action-btn" onClick={handleAdd}>
          加入購物車 - ${product.price * quantity}
        </button>
      </div>
    </div>
  )
}

function CartModal({ cart, setCart, total, onClose, onSubmit }) {
  const removeItem = (index) => {
    setCart(cart.filter((_, i) => i !== index))
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content slide-up" onClick={e => e.stopPropagation()}>
        <h2 style={{ marginBottom: '24px' }}>購物車</h2>
        
        {cart.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            購物車是空的
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
            {cart.map((item, i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '16px', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                <div>
                  <div style={{ fontSize: '16px', fontWeight: '500' }}>{item.productName} x {item.quantity}</div>
                  {item.note && <div style={{ fontSize: '12px', color: 'var(--accent-mint)', marginTop: '4px' }}>📝 {item.note}</div>}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{ fontWeight: '600' }}>${item.unitPrice * item.quantity}</div>
                  <button style={{ background: 'none', border: 'none', color: 'var(--accent-pink)', fontSize: '20px' }} onClick={() => removeItem(i)}>×</button>
                </div>
              </div>
            ))}
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', fontSize: '20px', fontWeight: '700' }}>
          <div>總計</div>
          <div style={{ color: 'var(--accent-cyan)' }}>${total}</div>
        </div>

        <button className="action-btn" onClick={onSubmit} disabled={cart.length === 0} style={{ opacity: cart.length === 0 ? 0.5 : 1 }}>
          送出訂單
        </button>
      </div>
    </div>
  )
}

export default App
