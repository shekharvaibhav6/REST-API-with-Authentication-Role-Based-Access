import { useState, useEffect, useCallback } from 'react'
import { useAuth } from './context/AuthContext'
import { taskApi } from './services/api'
import TaskModal from '../components/TaskModal'

const STATUS_COLOR = {
  PENDING: '#ffd166',
  IN_PROGRESS: '#74b9ff',
  DONE: '#00e5a0'
}

const PRIORITY_COLOR = {
  LOW: '#888',
  MEDIUM: '#ffd166',
  HIGH: '#ff4d6d'
}

export default function DashboardPage() {
  const { user, logout } = useAuth()
  const [tasks, setTasks] = useState([])
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editTask, setEditTask] = useState(null)
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const fetchTasks = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const res = await taskApi.getAll(page)
      const { content, page: pg, totalPages, totalElements } = res.data.data
      setTasks(content)
      setPagination({ page: pg, totalPages, totalElements })
    } catch {
      showToast('Failed to load tasks', 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchTasks() }, [fetchTasks])

  const handleCreate = async (form) => {
    await taskApi.create(form)
    showToast('Task created')
    fetchTasks()
  }

  const handleUpdate = async (form) => {
    await taskApi.update(editTask.id, form)
    showToast('Task updated')
    fetchTasks(pagination.page)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this task?')) return
    try {
      await taskApi.delete(id)
      showToast('Task deleted')
      fetchTasks(pagination.page)
    } catch {
      showToast('Delete failed', 'error')
    }
  }

  const openEdit = (task) => {
    setEditTask(task)
    setShowModal(true)
  }

  const openCreate = () => {
    setEditTask(null)
    setShowModal(true)
  }

  return (
    <div style={styles.layout}>
      {/* Sidebar */}
      <aside style={styles.sidebar}>
        <div style={styles.brand}>Sporty</div>
        <nav style={styles.nav}>
          <div style={styles.navItem}>
            <span>📋</span> My Tasks
          </div>
        </nav>
        <div style={styles.profile}>
          <div style={styles.avatar}>{user?.name?.[0]?.toUpperCase()}</div>
          <div>
            <div style={{ fontSize: 13, fontWeight: 600 }}>{user?.name}</div>
            <div style={{ fontSize: 11, color: 'var(--muted)' }}>{user?.role}</div>
          </div>
          <button
            onClick={logout}
            style={{ marginLeft: 'auto', background: 'none', border: 'none', color: 'var(--muted)', fontSize: 16 }}
            title="Logout"
          >⏻</button>
        </div>
      </aside>

      {/* Main */}
      <main style={styles.main}>
        <div style={styles.topbar}>
          <div>
            <h2 style={{ fontSize: 20, fontFamily: "'Space Mono', monospace" }}>My Tasks</h2>
            <p style={{ color: 'var(--muted)', fontSize: 12, marginTop: 2 }}>
              {pagination.totalElements} total tasks
            </p>
          </div>
          <button className="btn-primary" onClick={openCreate}>+ New Task</button>
        </div>

        {loading ? (
          <div style={styles.empty}>Loading tasks...</div>
        ) : tasks.length === 0 ? (
          <div style={styles.empty}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>📭</div>
            <p>No tasks yet. Create your first one!</p>
          </div>
        ) : (
          <div style={styles.grid}>
            {tasks.map(task => (
              <div key={task.id} style={styles.card}>
                <div style={styles.cardTop}>
                  <span style={{ ...styles.badge, background: STATUS_COLOR[task.status] + '22', color: STATUS_COLOR[task.status] }}>
                    {task.status.replace('_', ' ')}
                  </span>
                  <span style={{ ...styles.badge, background: PRIORITY_COLOR[task.priority] + '22', color: PRIORITY_COLOR[task.priority] }}>
                    {task.priority}
                  </span>
                </div>
                <h3 style={styles.cardTitle}>{task.title}</h3>
                {task.description && (
                  <p style={styles.cardDesc}>{task.description}</p>
                )}
                <div style={styles.cardFooter}>
                  <span style={{ color: 'var(--muted)', fontSize: 11 }}>
                    {new Date(task.createdAt).toLocaleDateString()}
                  </span>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(task)}>Edit</button>
                    <button className="btn-danger btn-sm" onClick={() => handleDelete(task.id)}>Delete</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {pagination.totalPages > 1 && (
          <div style={styles.pager}>
            <button className="btn-secondary btn-sm"
              disabled={pagination.page === 0}
              onClick={() => fetchTasks(pagination.page - 1)}>← Prev</button>
            <span style={{ color: 'var(--muted)', fontSize: 13 }}>
              Page {pagination.page + 1} of {pagination.totalPages}
            </span>
            <button className="btn-secondary btn-sm"
              disabled={pagination.page + 1 >= pagination.totalPages}
              onClick={() => fetchTasks(pagination.page + 1)}>Next →</button>
          </div>
        )}
      </main>

      {showModal && (
        <TaskModal
          task={editTask}
          onClose={() => setShowModal(false)}
          onSave={editTask ? handleUpdate : handleCreate}
        />
      )}

      {toast && (
        <div style={{
          ...styles.toast,
          background: toast.type === 'error' ? 'var(--danger)' : 'var(--accent)',
          color: toast.type === 'error' ? '#fff' : '#0d0d0d'
        }}>
          {toast.msg}
        </div>
      )}
    </div>
  )
}

const styles = {
  layout: {
    display: 'flex',
    minHeight: '100vh',
    background: 'var(--bg)'
  },
  sidebar: {
    width: 220,
    background: 'var(--surface)',
    borderRight: '1px solid var(--border)',
    display: 'flex',
    flexDirection: 'column',
    padding: '24px 16px',
    flexShrink: 0
  },
  brand: {
    fontFamily: "'Space Mono', monospace",
    fontSize: 18,
    fontWeight: 700,
    color: 'var(--accent)',
    marginBottom: 32
  },
  nav: { flex: 1 },
  navItem: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '10px 12px',
    borderRadius: 8,
    background: 'var(--accent-dim)',
    color: 'var(--accent)',
    fontWeight: 600,
    fontSize: 13,
    cursor: 'pointer'
  },
  profile: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    marginTop: 'auto',
    paddingTop: 20,
    borderTop: '1px solid var(--border)'
  },
  avatar: {
    width: 32,
    height: 32,
    borderRadius: '50%',
    background: 'var(--accent)',
    color: '#0d0d0d',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 700,
    fontSize: 14,
    flexShrink: 0
  },
  main: {
    flex: 1,
    padding: '28px 32px',
    overflow: 'auto'
  },
  topbar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 28
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
    gap: 16
  },
  card: {
    background: 'var(--surface)',
    border: '1px solid var(--border)',
    borderRadius: 10,
    padding: 18,
    display: 'flex',
    flexDirection: 'column',
    gap: 10,
    transition: 'border-color 0.2s'
  },
  cardTop: { display: 'flex', gap: 8 },
  badge: {
    fontSize: 11,
    padding: '3px 8px',
    borderRadius: 20,
    fontWeight: 600,
    fontFamily: "'Space Mono', monospace"
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: 600,
    lineHeight: 1.4
  },
  cardDesc: {
    fontSize: 13,
    color: 'var(--muted)',
    lineHeight: 1.5,
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden'
  },
  cardFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4
  },
  empty: {
    textAlign: 'center',
    padding: '80px 20px',
    color: 'var(--muted)'
  },
  pager: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 16,
    marginTop: 28
  },
  toast: {
    position: 'fixed',
    bottom: 24,
    right: 24,
    padding: '12px 20px',
    borderRadius: 8,
    fontWeight: 600,
    fontSize: 13,
    fontFamily: "'Space Mono', monospace",
    zIndex: 999,
    boxShadow: '0 4px 16px rgba(0,0,0,0.4)'
  }
}