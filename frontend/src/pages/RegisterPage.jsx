import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleChange = e => {
    setForm(p => ({ ...p, [e.target.name]: e.target.value }))
    setFieldErrors(p => ({ ...p, [e.target.name]: '' }))
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setFieldErrors({})
    setLoading(true)
    try {
      await register(form.name, form.email, form.password)
      navigate('/dashboard')
    } catch (err) {
      if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors)
      } else {
        setError(err.response?.data?.message || 'Registration failed')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.logo}>Sporty</div>
        <p style={styles.subtitle}>Create your account</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              name="name"
              placeholder="Enter your name"
              value={form.name}
              onChange={handleChange}
              required
            />
            {fieldErrors.name && <p className="error-msg">{fieldErrors.name}</p>}
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              placeholder="you@email.com"
              value={form.email}
              onChange={handleChange}
              required
            />
            {fieldErrors.email && <p className="error-msg">{fieldErrors.email}</p>}
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              placeholder="Min. 6 characters"
              value={form.password}
              onChange={handleChange}
              required
            />
            {fieldErrors.password && <p className="error-msg">{fieldErrors.password}</p>}
          </div>

          {error && <p className="error-msg" style={{ marginBottom: 14 }}>{error}</p>}

          <button type="submit" className="btn-primary" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" style={styles.link}>Sign In</Link>
        </p>
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--bg)',
    padding: 16
  },
  card: {
    background: 'var(--surface)',
    border: '1px solid var(--border)',
    borderRadius: 12,
    padding: '40px 36px',
    width: '100%',
    maxWidth: 400
  },
  logo: {
    fontFamily: "'Space Mono', monospace",
    fontSize: 24,
    fontWeight: 700,
    color: 'var(--accent)',
    marginBottom: 6
  },
  subtitle: {
    color: 'var(--muted)',
    marginBottom: 28,
    fontSize: 14
  },
  footer: {
    marginTop: 20,
    textAlign: 'center',
    color: 'var(--muted)',
    fontSize: 13
  },
  link: {
    color: 'var(--accent)',
    textDecoration: 'none',
    fontWeight: 600
  }
}