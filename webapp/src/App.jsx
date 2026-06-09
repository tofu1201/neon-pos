import { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import './index.css'

const API_BASE = window.location.port === '5173' ? 'http://127.0.0.1:8080/api' : '/api'

function App() {
  const [activeTab, setActiveTab] = useState('kds')
  const [pin, setPin] = useState(localStorage.getItem('admin_pin') || '')
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('admin_pin'))
  const [loginError, setLoginError] = useState(false)

  // Configure Axios header
  axios.defaults.headers.common['X-Admin-Pin'] = pin

  // Handle 401 globally
  axios.interceptors.response.use(
    response => response,
    error => {
      if (error.response && error.response.status === 401) {
        setIsAuthenticated(false)
        localStorage.removeItem('admin_pin')
      }
      return Promise.reject(error)
    }
  )

  const handleLogin = (e) => {
    e.preventDefault()
    const inputPin = e.target.pin.value
    // Just a basic check here, actual validation happens on API call
    if (inputPin) {
      setPin(inputPin)
      setIsAuthenticated(true)
      localStorage.setItem('admin_pin', inputPin)
      setLoginError(false)
      window.location.reload() // Reload to apply headers to interval fetches
    }
  }

  const handleLogout = () => {
    setPin('')
    setIsAuthenticated(false)
    localStorage.removeItem('admin_pin')
  }

  if (!isAuthenticated) {
    return (
      <div className="login-container">
        <div className="glass-panel animate-fade-in fade-scale" style={{ padding: '40px', maxWidth: '400px', margin: '100px auto', textAlign: 'center' }}>
          <h2 style={{ marginBottom: '8px' }}>NEON Web Manager</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>請輸入管理員密碼以登入</p>
          <form onSubmit={handleLogin}>
            <input 
              type="password" 
              name="pin"
              className="modern-input" 
              placeholder="請輸入密碼 (預設: 1234)"
              autoFocus
              style={{ width: '100%', marginBottom: '16px', textAlign: 'center', letterSpacing: '8px', fontSize: '24px' }}
            />
            {loginError && <div style={{ color: 'var(--accent-pink)', marginBottom: '16px' }}>密碼錯誤或已過期，請重新登入</div>}
            <button type="submit" className="action-btn" style={{ width: '100%' }}>登入</button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <>
      <nav className="navbar">
        <div className="nav-brand">NEON Web Manager</div>
        <div className="nav-links">
          <button className={`nav-link ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>數據儀表板</button>
          <button className={`nav-link ${activeTab === 'orders' ? 'active' : ''}`} onClick={() => setActiveTab('orders')}>歷史訂單</button>
          <button className={`nav-link ${activeTab === 'kds' ? 'active' : ''}`} onClick={() => setActiveTab('kds')}>廚房看板</button>
          <button className={`nav-link ${activeTab === 'menu' ? 'active' : ''}`} onClick={() => setActiveTab('menu')}>菜單管理</button>
          <button className={`nav-link ${activeTab === 'members' ? 'active' : ''}`} onClick={() => setActiveTab('members')}>會員管理</button>
          <button className={`nav-link ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => setActiveTab('settings')}>商店設定</button>
          <button className="nav-link" onClick={handleLogout} style={{ color: 'var(--accent-pink)' }}>登出</button>
        </div>
      </nav>
      
      <main className="main-content">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'orders' && <OrderHistory />}
        {activeTab === 'kds' && <KDS />}
        {activeTab === 'menu' && <MenuManager />}
        {activeTab === 'members' && <MemberManager />}
        {activeTab === 'settings' && <SettingsManager />}
      </main>
    </>
  )
}

function Dashboard() {
  const [orders, setOrders] = useState([])
  const [stats, setStats] = useState({ todayRevenue: 0, totalOrders: 0, averageOrderValue: 0 })
  
  useEffect(() => {
    fetchData()
    const interval = setInterval(fetchData, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchData = async () => {
    try {
      const [ordersRes, statsRes] = await Promise.all([
        axios.get(`${API_BASE}/history/orders`),
        axios.get(`${API_BASE}/stats`)
      ])
      setOrders(ordersRes.data)
      setStats(statsRes.data)
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="animate-fade-in fade-scale">
      <div className="dashboard-stats">
        <div className="glass-panel stat-card cyan">
          <div className="stat-label">今日總營業額</div>
          <div className="stat-value">NT$ {stats.todayRevenue.toFixed(0)}</div>
        </div>
        <div className="glass-panel stat-card mint">
          <div className="stat-label">今日訂單數</div>
          <div className="stat-value">{stats.totalOrders}</div>
        </div>
        <div className="glass-panel stat-card pink">
          <div className="stat-label">平均客單價</div>
          <div className="stat-value">NT$ {stats.averageOrderValue.toFixed(0)}</div>
        </div>
      </div>
      <div className="glass-panel" style={{ padding: '24px' }}>
        <h3>最近訂單記錄</h3>
        <table className="modern-table">
          <thead>
            <tr>
              <th>單號</th>
              <th>狀態</th>
              <th>付款方式</th>
              <th>金額</th>
            </tr>
          </thead>
          <tbody>
            {orders.slice(0, 10).map(order => (
              <tr key={order.id}>
                <td>{order.orderNo}</td>
                <td><span className="order-type">{order.status}</span></td>
                <td>{order.paymentMethod}</td>
                <td style={{ color: 'var(--accent-cyan)' }}>${order.totalAmount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function OrderHistory() {
  const [orders, setOrders] = useState([])
  
  useEffect(() => {
    fetchOrders()
  }, [])

  const fetchOrders = async () => {
    try {
      const res = await axios.get(`${API_BASE}/history/orders`)
      setOrders(res.data)
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="animate-fade-in fade-scale glass-panel" style={{ padding: '24px' }}>
      <h3>所有歷史訂單</h3>
      <table className="modern-table">
        <thead>
          <tr>
            <th>單號</th>
            <th>時間</th>
            <th>狀態</th>
            <th>付款方式</th>
            <th>金額</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.orderNo}</td>
              <td>{new Date(order.createdAt).toLocaleString()}</td>
              <td><span className="order-type">{order.status}</span></td>
              <td>{order.paymentMethod}</td>
              <td style={{ color: 'var(--accent-cyan)' }}>${order.totalAmount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function KDS() {
  const [orders, setOrders] = useState([])
  const [soundEnabled, setSoundEnabled] = useState(false)
  const knownOrderIds = useRef(new Set())

  useEffect(() => {
    fetchOrders()
    const interval = setInterval(fetchOrders, 3000)
    return () => clearInterval(interval)
  }, [])

  const playDing = () => {
    if (!soundEnabled) return;
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, ctx.currentTime);
      osc.frequency.setValueAtTime(1108.73, ctx.currentTime + 0.1); // C#6
      gain.gain.setValueAtTime(0, ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.5, ctx.currentTime + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.5);
    } catch(e) {}
  }

  const fetchOrders = async () => {
    try {
      const res = await axios.get(`${API_BASE}/orders`)
      const pendingOrders = res.data.filter(o => o.status === 'PAID' || o.status === 'PREPARING')
      
      let hasNew = false;
      const currentIds = new Set(pendingOrders.map(o => o.id))
      currentIds.forEach(id => {
        if (!knownOrderIds.current.has(id)) {
          hasNew = true;
          knownOrderIds.current.add(id);
        }
      })
      if (hasNew) playDing();

      setOrders(pendingOrders)
    } catch (e) {
      console.error(e)
    }
  }

  const markCompleted = async (id) => {
    try {
      await axios.post(`${API_BASE}/orders/${id}/status`, { status: 'COMPLETED' })
      knownOrderIds.current.delete(id)
      fetchOrders()
    } catch (e) {
      console.error("Failed to update status", e)
    }
  }

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
        <h2>廚房看板 (KDS)</h2>
        <button 
          className={`btn ${soundEnabled ? 'btn-primary' : ''}`} 
          style={{ background: soundEnabled ? 'var(--accent-cyan)' : 'transparent', border: '1px solid var(--accent-cyan)', color: soundEnabled ? '#fff' : 'var(--accent-cyan)' }}
          onClick={() => {
            setSoundEnabled(!soundEnabled)
            if (!soundEnabled) playDing() // Play test sound when enabled
          }}
        >
          {soundEnabled ? '🔊 提示音已開啟' : '🔈 點擊開啟提示音'}
        </button>
      </div>

      <div className="grid-cards">
        {orders.length === 0 ? (
          <div style={{ textAlign: 'center', width: '100%', padding: '40px', color: 'var(--text-muted)' }}>
            目前沒有待處理的訂單
          </div>
        ) : (
          orders.map(order => (
            <div key={order.id} className="glass-panel order-card fade-scale">
              <div className="order-header">
                <span className="order-no">#{order.orderNo}</span>
                <span className="order-type">{order.paymentMethod === '外帶' ? '外帶' : '內用'}</span>
              </div>
              <div className="order-items">
                {order.lines && order.lines.map((line, idx) => (
                  <div key={idx} className="order-item" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                      <span className="item-name" style={{ fontSize: '1.2rem', fontWeight: 600 }}>{line.productName}</span>
                      <span className="item-qty" style={{ fontSize: '1.2rem', color: 'var(--accent-red)' }}>x {line.quantity}</span>
                    </div>
                    {line.modifiers && line.modifiers.length > 0 && (
                      <div style={{ fontSize: '0.9rem', color: 'var(--accent-mint)', marginTop: '4px' }}>
                        👉 選項: {line.modifiers.join(', ')}
                      </div>
                    )}
                    {line.note && (
                      <div style={{ fontSize: '0.9rem', color: 'var(--accent-cyan)', marginTop: '4px' }}>
                        📝 備註: {line.note}
                      </div>
                    )}
                  </div>
                ))}
              </div>
              <button className="btn btn-primary" onClick={() => markCompleted(order.id)}>
                出餐完成
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

function MenuManager() {
  const [products, setProducts] = useState([])
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [newProduct, setNewProduct] = useState({ sku: '', name: '', price: '', category: '' })

  useEffect(() => { fetchProducts() }, [])

  const fetchProducts = async () => {
    try {
      const res = await axios.get(`${API_BASE}/products`)
      setProducts(res.data)
    } catch (e) {}
  }

  const handleAddProduct = async (e) => {
    e.preventDefault()
    try {
      await axios.post(`${API_BASE}/products`, {
        id: editingId,
        sku: newProduct.sku,
        name: newProduct.name,
        price: parseFloat(newProduct.price) || 0,
        category: newProduct.category
      })
      setShowModal(false)
      fetchProducts()
    } catch (e) {
      alert("儲存失敗")
    }
  }

  return (
    <div className="animate-fade-in glass-panel fade-scale" style={{ padding: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
        <h3>菜單管理</h3>
        <button className="btn btn-primary" onClick={() => { setEditingId(null); setNewProduct({sku:'',name:'',price:'',category:''}); setShowModal(true) }}>+ 新增商品</button>
      </div>
      <table className="modern-table">
        <thead><tr><th>SKU</th><th>分類</th><th>品名</th><th>價格</th><th>操作</th></tr></thead>
        <tbody>
          {products.map(p => (
            <tr key={p.id}>
              <td>{p.sku}</td><td>{p.category}</td><td>{p.name}</td><td style={{ color: 'var(--accent-mint)' }}>${p.price}</td>
              <td>
                <button className="btn" style={{ padding: '4px 12px', background: 'rgba(0, 240, 255, 0.1)', color: 'var(--accent-cyan)', marginRight: '8px' }} onClick={() => { setEditingId(p.id); setNewProduct(p); setShowModal(true) }}>編輯</button>
                <button className="btn" style={{ padding: '4px 12px', background: 'rgba(255, 42, 109, 0.1)', color: 'var(--accent-red)' }} onClick={async () => { if(window.confirm('確定刪除？')) { await axios.delete(`${API_BASE}/products/${p.id}`); fetchProducts() } }}>刪除</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {showModal && (
        <div className="modal-overlay">
          <div className="glass-panel modal-content slide-up">
            <h3>{editingId ? '編輯商品' : '新增商品'}</h3>
            <form onSubmit={handleAddProduct} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <input className="modern-input" required value={newProduct.sku} onChange={e => setNewProduct({...newProduct, sku: e.target.value})} placeholder="SKU 條碼" />
              <input className="modern-input" required value={newProduct.category} onChange={e => setNewProduct({...newProduct, category: e.target.value})} placeholder="分類" />
              <input className="modern-input" required value={newProduct.name} onChange={e => setNewProduct({...newProduct, name: e.target.value})} placeholder="品名" />
              <input className="modern-input" type="number" required value={newProduct.price} onChange={e => setNewProduct({...newProduct, price: e.target.value})} placeholder="價格" />
              <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                <button type="button" className="btn" onClick={() => setShowModal(false)}>取消</button>
                <button type="submit" className="btn btn-primary">儲存</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

function MemberManager() {
  const [members, setMembers] = useState([])
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ phone: '', name: '', balance: 0, points: 0, nfcCardId: '' })

  useEffect(() => { fetchMembers() }, [])

  const fetchMembers = async () => {
    try {
      const res = await axios.get(`${API_BASE}/members`)
      setMembers(res.data)
    } catch (e) {}
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await axios.post(`${API_BASE}/members`, {
        ...form,
        balance: parseFloat(form.balance) || 0,
        points: parseInt(form.points) || 0
      })
      setShowModal(false)
      fetchMembers()
    } catch (e) { alert("儲存失敗") }
  }

  return (
    <div className="animate-fade-in glass-panel fade-scale" style={{ padding: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
        <h3>會員管理</h3>
        <button className="btn btn-primary" onClick={() => { setForm({phone:'',name:'',balance:0,points:0,nfcCardId:''}); setShowModal(true) }}>+ 新增會員 / 儲值</button>
      </div>
      <table className="modern-table">
        <thead><tr><th>電話</th><th>姓名</th><th>餘額</th><th>點數</th><th>NFC卡號</th></tr></thead>
        <tbody>
          {members.map(m => (
            <tr key={m.id}>
              <td>{m.phone}</td><td>{m.name}</td><td style={{ color: 'var(--accent-mint)' }}>${m.balance}</td><td style={{ color: 'var(--accent-cyan)' }}>{m.points}</td><td>{m.nfcCardId || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {showModal && (
        <div className="modal-overlay">
          <div className="glass-panel modal-content slide-up">
            <h3>新增會員 / 編輯儲值</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>若輸入已存在的電話號碼，將會覆蓋其名稱並調整餘額至您輸入的金額。</p>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <input className="modern-input" required value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} placeholder="電話號碼" />
              <input className="modern-input" required value={form.name} onChange={e => setForm({...form, name: e.target.value})} placeholder="姓名" />
              <label style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>最新餘額</label>
              <input className="modern-input" type="number" required value={form.balance} onChange={e => setForm({...form, balance: e.target.value})} placeholder="餘額" />
              <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                <button type="button" className="btn" onClick={() => setShowModal(false)}>取消</button>
                <button type="submit" className="btn btn-primary">儲存</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

function SettingsManager() {
  const [settings, setSettings] = useState({ storeName: '', storeAddress: '', storePhone: '', taxRate: '0.05' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    axios.get(`${API_BASE}/settings`).then(res => {
      setSettings(prev => ({ ...prev, ...res.data }))
    })
  }, [])

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await axios.post(`${API_BASE}/settings`, settings)
      setTimeout(() => setSaving(false), 500)
    } catch (e) { setSaving(false) }
  }

  return (
    <div className="animate-fade-in fade-scale" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div className="glass-panel" style={{ padding: '32px' }}>
        <h2 style={{ marginBottom: '24px' }}>⚙️ 商店設定</h2>
        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>商店名稱</label>
            <input className="modern-input" required value={settings.storeName} onChange={e => setSettings({...settings, storeName: e.target.value})} placeholder="例如: NEON POS" />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>地址</label>
            <input className="modern-input" required value={settings.storeAddress} onChange={e => setSettings({...settings, storeAddress: e.target.value})} placeholder="列印在收據上的地址" />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>電話</label>
            <input className="modern-input" required value={settings.storePhone} onChange={e => setSettings({...settings, storePhone: e.target.value})} placeholder="列印在收據上的電話" />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>預設稅率 (小數)</label>
            <input className="modern-input" type="number" step="0.01" required value={settings.taxRate} onChange={e => setSettings({...settings, taxRate: e.target.value})} placeholder="例如: 0.05" />
          </div>
          
          <button type="submit" className="btn btn-primary" style={{ marginTop: '16px', height: '48px', fontSize: '1.1rem' }}>
            {saving ? '儲存中...' : '儲存設定'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default App
