
import { useMemo, useState } from 'react'

const initialFriends = [
  { id: 1, name: 'Ajay Bhai', email: 'ajay@example.com' },
  { id: 2, name: 'Rohit Kumar', email: 'rohit@example.com' },
  { id: 3, name: 'Sahil Patil', email: 'sahil@example.com' },
  { id: 4, name: 'Vivek Sharma', email: 'vivek@example.com' },
]

const initialBills = [
  {
    id: 101,
    name: 'Dinner at Cafe',
    date: '26 Oct 2026',
    total: 2400,
    participants: [
      { name: 'Ajay Bhai', share: 600 },
      { name: 'Rohit Kumar', share: 600 },
      { name: 'Sahil Patil', share: 600 },
      { name: 'Vivek Sharma', share: 600 },
    ],
    status: 'Pending',
  },
  {
    id: 102,
    name: 'Movie Tickets',
    date: '18 Oct 2026',
    total: 1800,
    participants: [
      { name: 'Ajay Bhai', share: 450 },
      { name: 'Rohit Kumar', share: 450 },
      { name: 'Sahil Patil', share: 450 },
      { name: 'Vivek Sharma', share: 450 },
    ],
    status: 'Settled',
  },
  {
    id: 103,
    name: 'Groceries',
    date: '05 Oct 2026',
    total: 3200,
    participants: [
      { name: 'Ajay Bhai', share: 800 },
      { name: 'Rohit Kumar', share: 800 },
      { name: 'Sahil Patil', share: 800 },
      { name: 'Vivek Sharma', share: 800 },
    ],
    status: 'Pending',
  },
]

const styles = `
  .bs-page {
    min-height: 100vh;
    padding: 28px;
    background: #f5f8fc;
    color: #142039;
    font-family: Inter, system-ui, sans-serif;
    box-sizing: border-box;
  }
  .bs-page *, .bs-page *::before, .bs-page *::after {
    box-sizing: border-box;
  }
  .bs-welcome, .bs-heading, .bs-section-title,
  .bs-friends-title, .bs-friend, .bs-bill,
  .bs-selected-friend, .bs-summary {
    display: flex;
    align-items: center;
  }
  .bs-welcome, .bs-section-title, .bs-friends-title {
    justify-content: space-between;
  }
  .bs-welcome { margin-bottom: 30px; }
  .bs-muted { color: #78869c; font-size: 13px; }
  .bs-welcome h2 { margin: 4px 0 0; font-size: 23px; }
  .bs-avatar, .bs-heading-icon {
    display: grid; place-items: center;
    width: 46px; height: 46px; border-radius: 14px;
    background: #d6f8ec; color: #009f75; font-weight: 800;
  }
  .bs-heading { gap: 14px; margin-bottom: 25px; }
  .bs-heading h1 { margin: 0; font-size: 29px; }
  .bs-heading p { margin: 6px 0 0; color: #71809a; }
  .bs-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1.15fr);
    gap: 22px; align-items: start;
  }
  .bs-card {
    min-width: 0; padding: 23px;
    background: #fff; border: 1px solid #e4eaf2;
    border-radius: 17px; box-shadow: 0 3px 12px #10204008;
  }
  .bs-card h2 { margin: 0 0 20px; font-size: 18px; }
  .bs-form label {
    display: block; margin: 15px 0 7px;
    font-size: 13px; font-weight: 650;
  }
  .bs-form input:not([type="checkbox"]), .bs-form select {
    width: 100%; min-width: 0; height: 44px;
    padding: 0 12px; color: #17233b; background: white;
    border: 1px solid #dfe7f1; border-radius: 9px;
    font: inherit; font-size: 13px;
  }
  .bs-form input:focus, .bs-form select:focus {
    outline: 2px solid #b6f0db; border-color: #00b889;
  }
  .bs-fields {
    display: grid; grid-template-columns: 1fr 1fr; gap: 14px;
  }
  .bs-friends-title { margin: 24px 0 8px; }
  .bs-friends-title h3 { margin: 0; font-size: 14px; }
  .bs-link {
    padding: 6px; color: #008f6b; border: 0;
    background: transparent; cursor: pointer; font: inherit; font-size: 12px;
  }
  .bs-friend { gap: 10px; min-height: 64px; border-bottom: 1px solid #f0f3f7; }
  .bs-friend input[type="checkbox"] {
    width: 16px; height: 16px; accent-color: #08b889; cursor: pointer;
  }
  .bs-friend-avatar {
    display: grid; place-items: center; flex: 0 0 38px;
    width: 38px; height: 38px; border-radius: 50%;
    color: white; font-size: 12px; font-weight: 800;
    background: #219e82;
  }
  .bs-friend-info { flex: 1; min-width: 0; }
  .bs-friend-info strong, .bs-selected-friend strong { font-size: 12px; }
  .bs-friend-info span, .bs-bill-info span, .bs-bill-total span,
  .bs-selected-friend span {
    display: block; color: #78869c; font-size: 11px; margin-top: 4px;
  }
  .bs-share { color: #32445e; font-size: 12px; white-space: nowrap; }
  .bs-form input.bs-share-input { width: 105px; height: 36px; padding: 0 8px; }
  .bs-allocation {
    margin-top: 12px; padding: 12px; border-radius: 8px;
    background: #fff5df; color: #875b0b; font-size: 12px;
  }
  .bs-allocation.is-balanced { background: #e1f8ee; color: #00845f; }
  .bs-submit {
    width: 100%; min-height: 46px; margin-top: 22px;
    border: 0; border-radius: 10px; background: #08b889;
    color: white; font-size: 14px; font-weight: 750; cursor: pointer;
  }
  .bs-submit:hover { background: #009c73; }
  .bs-message { padding: 11px 12px; border-radius: 8px; font-size: 13px; }
  .bs-error { background: #fff0f0; color: #b42318; }
  .bs-success { background: #e5f9ef; color: #087443; }
  .bs-right { display: grid; gap: 22px; }
  .bs-section-title { margin-bottom: 14px; }
  .bs-section-title h2 { margin: 0; }
  .bs-section-title span { color: #009f75; font-size: 12px; }
  .bs-bill { gap: 12px; padding: 16px 0; border-top: 1px solid #edf1f6; }
  .bs-bill-icon {
    display: grid; place-items: center; flex: 0 0 40px;
    height: 40px; border-radius: 12px; background: #e2f8ef;
    color: #009d72; font-weight: 800;
  }
  .bs-bill-info { flex: 1; min-width: 0; }
  .bs-bill-info strong, .bs-bill-total strong { font-size: 12px; }
  .bs-bill-total { text-align: right; white-space: nowrap; }
  .bs-status {
    padding: 7px 10px; border: 0; border-radius: 20px;
    font-size: 11px; font-weight: 650; cursor: pointer;
  }
  .bs-status.pending { background: #fff1d4; color: #a76000; }
  .bs-status.settled { background: #dff8ee; color: #00865f; }
  .bs-note { color: #8792a5; font-size: 11px; line-height: 1.5; }
  .bs-selected {
    display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px;
  }
  .bs-selected-friend { gap: 10px; min-width: 0; padding: 12px; border-radius: 10px; background: #f7f9fc; }
  .bs-selected-friend > div:last-child { min-width: 0; }
  .bs-summary {
    flex-wrap: wrap; justify-content: space-between; gap: 10px;
    margin-top: 20px; padding: 16px; border-radius: 10px;
    background: #e3f9ef; color: #00845e; font-size: 12px;
  }
  .bs-empty { color: #77849a; font-size: 13px; }
  @media (max-width: 1000px) {
    .bs-grid { grid-template-columns: 1fr; }
  }
  @media (max-width: 560px) {
    .bs-page { padding: 14px; }
    .bs-card { padding: 16px; }
    .bs-fields { grid-template-columns: 1fr; gap: 0; }
    .bs-selected { grid-template-columns: 1fr; }
    .bs-bill { flex-wrap: wrap; }
    .bs-bill-total { margin-left: auto; }
  }
`

function money(amount) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount)
}

function BillSplit() {
  const [friends] = useState(initialFriends)
  const [bills, setBills] = useState(initialBills)
  const [billName, setBillName] = useState('')
  const [total, setTotal] = useState('')
  const [date, setDate] = useState('2026-10-27')
  const [splitBy, setSplitBy] = useState('equal')
  const [selected, setSelected] = useState([1, 2, 3, 4])
  const [customShares, setCustomShares] = useState({})
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const participants = friends.filter(friend => selected.includes(friend.id))
  const amount = Number(total) || 0

  const shares = useMemo(() => {
    if (!participants.length) return []

    if (splitBy === 'equal') {
      const totalPaise = Math.round(amount * 100)
      const base = Math.floor(totalPaise / participants.length)

      return participants.map((friend, index) => ({
        ...friend,
        share: (base + (index < totalPaise % participants.length ? 1 : 0)) / 100,
      }))
    }

    return participants.map(friend => ({
      ...friend,
      share: Math.round((Number(customShares[friend.id]) || 0) * 100) / 100,
    }))
  }, [amount, participants, splitBy, customShares])

  const allocated = Math.round(shares.reduce((sum, item) => sum + item.share, 0) * 100) / 100
  const remaining = Math.round((amount - allocated) * 100) / 100

  function toggleFriend(id) {
    setSelected(current =>
      current.includes(id)
        ? current.filter(friendId => friendId !== id)
        : [...current, id]
    )
    setError('')
    setSuccess('')
  }

  function createBill(event) {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (!billName.trim()) {
      setError('Please enter a bill name.')
      return
    }
    if (!Number.isFinite(amount) || amount <= 0) {
      setError('Enter a valid amount greater than zero.')
      return
    }
    if (!participants.length) {
      setError('Select at least one participant.')
      return
    }
    if (splitBy === 'custom' && (shares.some(item => item.share < 0) || Math.abs(remaining) > 0.009)) {
      setError('Custom shares must add up to the total bill.')
      return
    }

    const newBill = {
      id: Date.now(),
      name: billName.trim(),
      date: new Date(`${date}T12:00:00`).toLocaleDateString('en-GB', {
        day: '2-digit', month: 'short', year: 'numeric',
      }),
      total: amount,
      participants: shares.map(item => ({ name: item.name, share: item.share })),
      status: 'Pending',
    }

    setBills(current => [newBill, ...current])
    setBillName('')
    setTotal('')
    setSplitBy('equal')
    setCustomShares({})
    setSuccess('Bill split created successfully!')
  }

  function toggleStatus(id) {
    setBills(current => current.map(bill =>
      bill.id === id
        ? { ...bill, status: bill.status === 'Settled' ? 'Pending' : 'Settled' }
        : bill
    ))
  }

  return (
    <>
      <style>{styles}</style>
      <main className="bs-page">
        <header className="bs-welcome">
          <div>
            <span className="bs-muted">Welcome back,</span>
            <h2>Sahil</h2>
          </div>
          <div className="bs-avatar">SC</div>
        </header>

        <section className="bs-heading">
          <div className="bs-heading-icon">⇄</div>
          <div>
            <h1>Bill Splitter</h1>
            <p>Split your bills with friends and family.</p>
          </div>
        </section>

        <div className="bs-grid">
          <form className="bs-card bs-form" onSubmit={createBill}>
            <h2>Add Bill Details</h2>

            <label htmlFor="bs-name">Bill name</label>
            <input id="bs-name" value={billName}
              onChange={e => setBillName(e.target.value)}
              placeholder="e.g. Dinner at Cafe" required />

            <div className="bs-fields">
              <div>
                <label htmlFor="bs-total">Total amount (₹)</label>
                <input id="bs-total" type="number" min="0.01" step="0.01"
                  value={total} onChange={e => setTotal(e.target.value)}
                  placeholder="2400" required />
              </div>
              <div>
                <label htmlFor="bs-split">Split by</label>
                <select id="bs-split" value={splitBy}
                  onChange={e => setSplitBy(e.target.value)}>
                  <option value="equal">Equally</option>
                  <option value="custom">Custom amounts</option>
                </select>
              </div>
            </div>

            <label htmlFor="bs-date">Date</label>
            <input id="bs-date" type="date" value={date}
              onChange={e => setDate(e.target.value)} required />

            <div className="bs-friends-title">
              <h3>Select Friends</h3>
              <button type="button" className="bs-link"
                onClick={() => setSelected(
                  selected.length === friends.length ? [] : friends.map(friend => friend.id)
                )}>
                {selected.length === friends.length ? 'Deselect all' : 'Select all'}
              </button>
            </div>

            <div>
              {friends.map((friend, index) => {
                const isSelected = selected.includes(friend.id)
                const share = shares.find(item => item.id === friend.id)?.share ?? 0

                return (
                  <div className="bs-friend" key={friend.id}>
                    <input type="checkbox" aria-label={`Select ${friend.name}`}
                      checked={isSelected} onChange={() => toggleFriend(friend.id)} />
                    <div className="bs-friend-avatar"
                      style={{ background: ['#219e82', '#8050cf', '#f0783d', '#149ab3'][index] }}>
                      {friend.name.split(' ').map(part => part[0]).join('').slice(0, 2)}
                    </div>
                    <div className="bs-friend-info">
                      <strong>{friend.name}</strong>
                      <span>{friend.email}</span>
                    </div>
                    {isSelected && (splitBy === 'custom' ? (
                      <input className="bs-share-input" aria-label={`${friend.name} share`}
                        type="number" min="0" step="0.01"
                        value={customShares[friend.id] ?? ''}
                        onChange={e => setCustomShares(current => ({
                          ...current, [friend.id]: e.target.value,
                        }))}
                        placeholder="₹ Share" />
                    ) : <strong className="bs-share">{money(share)}</strong>)}
                  </div>
                )
              })}
            </div>

            {splitBy === 'custom' && (
              <div className={`bs-allocation ${Math.abs(remaining) < 0.009 ? 'is-balanced' : ''}`}>
                Allocated: {money(allocated)} · Remaining: {money(remaining)}
              </div>
            )}

            {error && <p className="bs-message bs-error">{error}</p>}
            {success && <p className="bs-message bs-success">{success}</p>}

            <button className="bs-submit" type="submit">Split &amp; Create Bill</button>
          </form>

          <div className="bs-right">
            <section className="bs-card">
              <div className="bs-section-title">
                <h2>Recent Bills</h2>
                <span>{bills.length} bills</span>
              </div>

              {bills.length === 0 ? (
                <p className="bs-empty">No bills yet. Create your first bill!</p>
              ) : bills.map(bill => (
                <article className="bs-bill" key={bill.id}>
                  <div className="bs-bill-icon">₹</div>
                  <div className="bs-bill-info">
                    <strong>{bill.name}</strong>
                    <span>{bill.date} · {bill.participants.length} people</span>
                  </div>
                  <div className="bs-bill-total">
                    <strong>{money(bill.total)}</strong>
                    <span>{money(bill.total / bill.participants.length)} each*</span>
                  </div>
                  <button type="button"
                    className={`bs-status ${bill.status === 'Settled' ? 'settled' : 'pending'}`}
                    onClick={() => toggleStatus(bill.id)}>
                    {bill.status}
                  </button>
                </article>
              ))}

              <p className="bs-note">
                *Average share shown for reference. Click a status to toggle the demo payment state.
              </p>
            </section>

            <section className="bs-card">
              <h2>Friends in This Split</h2>

              {shares.length === 0 ? (
                <p className="bs-empty">Select friends to see their shares.</p>
              ) : (
                <div className="bs-selected">
                  {shares.map((friend, index) => (
                    <div className="bs-selected-friend" key={friend.id}>
                      <div className="bs-friend-avatar"
                        style={{ background: ['#219e82', '#8050cf', '#f0783d', '#149ab3'][index % 4] }}>
                        {friend.name.split(' ').map(part => part[0]).join('').slice(0, 2)}
                      </div>
                      <div>
                        <strong>{friend.name}</strong>
                        <span>{money(friend.share)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <div className="bs-summary">
                <strong>Total bill: {money(amount)}</strong>
                <span>{shares.length} selected · {money(allocated)} allocated</span>
              </div>
            </section>
          </div>
        </div>
      </main>
    </>
  )
}

export default BillSplit
