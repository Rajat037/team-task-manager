import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { CheckCircle2, ClipboardList, FolderKanban, LogOut, Plus, UserCircle, Users } from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api');

function App() {
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('team-task-auth');
    return saved ? JSON.parse(saved) : null;
  });
  const [view, setView] = useState('dashboard');

  function saveAuth(nextAuth) {
    setAuth(nextAuth);
    if (nextAuth) localStorage.setItem('team-task-auth', JSON.stringify(nextAuth));
    else localStorage.removeItem('team-task-auth');
  }

  if (!auth) {
    return <AuthPage onAuth={saveAuth} />;
  }

  return <Shell auth={auth} view={view} setView={setView} onLogout={() => saveAuth(null)} />;
}

function Shell({ auth, view, setView, onLogout }) {
  const pages = [
    ['dashboard', ClipboardList, 'Dashboard'],
    ['projects', FolderKanban, 'Projects'],
    ['tasks', CheckCircle2, 'Tasks'],
    ['profile', UserCircle, 'Profile']
  ];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">TT</div>
          <div>
            <strong>Team Task</strong>
            <span>{auth.user.role}</span>
          </div>
        </div>
        <nav>
          {pages.map(([id, Icon, label]) => (
            <button key={id} className={view === id ? 'active' : ''} onClick={() => setView(id)}>
              <Icon size={18} />
              {label}
            </button>
          ))}
        </nav>
        <button className="logout" onClick={onLogout}>
          <LogOut size={18} />
          Logout
        </button>
      </aside>
      <main>
        {view === 'dashboard' && <Dashboard auth={auth} />}
        {view === 'projects' && <Projects auth={auth} />}
        {view === 'tasks' && <Tasks auth={auth} />}
        {view === 'profile' && <Profile auth={auth} />}
      </main>
    </div>
  );
}

function AuthPage({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: 'admin@teamtask.local', password: 'Admin@123', role: 'MEMBER' });
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    const endpoint = mode === 'login' ? '/auth/login' : '/auth/signup';
    const payload = mode === 'login'
      ? { email: form.email, password: form.password }
      : form;
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!response.ok) throw new Error('Please check your details and try again.');
      const data = await response.json();
      onAuth(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-panel">
        <div className="auth-copy">
          <p>Team Task Manager</p>
          <h1>Plan work, assign owners, and track progress in one place.</h1>
        </div>
        <form onSubmit={submit} className="auth-form">
          <div className="tabs">
            <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Login</button>
            <button type="button" className={mode === 'signup' ? 'active' : ''} onClick={() => setMode('signup')}>Signup</button>
          </div>
          {mode === 'signup' && (
            <>
              <label>Name<input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required /></label>
              <label>Role<select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}><option>MEMBER</option><option>ADMIN</option></select></label>
            </>
          )}
          <label>Email<input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required /></label>
          <label>Password<input type="password" minLength="8" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required /></label>
          {error && <p className="error">{error}</p>}
          <button className="primary" type="submit">{mode === 'login' ? 'Login' : 'Create Account'}</button>
        </form>
      </section>
    </div>
  );
}

function useApi(auth, path, refreshKey = 0) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    fetch(`${API_BASE}${path}`, { headers: headers(auth) })
      .then(async res => {
        if (!res.ok) throw new Error(`Could not load ${path}`);
        return res.json();
      })
      .then(setData)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [auth, path, refreshKey]);

  return { data, loading, error };
}

function Dashboard({ auth }) {
  const { data } = useApi(auth, '/dashboard');
  const stats = data || { totalTasks: 0, completedTasks: 0, pendingTasks: 0, overdueTasks: 0 };
  return (
    <Page title="Dashboard" subtitle={`Welcome back, ${auth.user.name}.`}>
      <div className="stat-grid">
        <Stat label="Total Tasks" value={stats.totalTasks} />
        <Stat label="Completed" value={stats.completedTasks} />
        <Stat label="Pending" value={stats.pendingTasks} />
        <Stat label="Overdue" value={stats.overdueTasks} />
      </div>
    </Page>
  );
}

function Projects({ auth }) {
  const [refresh, setRefresh] = useState(0);
  const { data: projects, loading } = useApi(auth, '/projects', refresh);
  const { data: team } = useApi(auth, '/users/team');
  const [form, setForm] = useState({ name: '', description: '', memberIds: [] });
  const [editingProject, setEditingProject] = useState(null);
  const [editMemberIds, setEditMemberIds] = useState([]);

  async function createProject(event) {
    event.preventDefault();
    await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: headers(auth),
      body: JSON.stringify({ ...form, memberIds: form.memberIds.map(Number) })
    });
    setForm({ name: '', description: '', memberIds: [] });
    setRefresh(refresh + 1);
  }

  async function updateProjectMembers(projectId) {
    await fetch(`${API_BASE}/projects/${projectId}/members`, {
      method: 'PUT',
      headers: headers(auth),
      body: JSON.stringify(editMemberIds.map(Number))
    });
    setEditingProject(null);
    setRefresh(refresh + 1);
  }

  return (
    <Page title="Projects" subtitle="Create projects and keep the team grouped around shared work.">
      {auth.user.role === 'ADMIN' && (
        <form className="inline-form" onSubmit={createProject}>
          <input placeholder="Project name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          <select multiple value={form.memberIds} onChange={e => setForm({ ...form, memberIds: [...e.target.selectedOptions].map(o => o.value) })}>
            {(team || []).map(user => <option key={user.id} value={user.id}>{user.name}</option>)}
          </select>
          <button className="primary"><Plus size={16} />Create</button>
        </form>
      )}
      {!loading && (projects || []).length === 0 && (
        <p className="notice">No projects assigned yet.</p>
      )}
      <div className="list-grid">
        {(projects || []).map(project => (
          <article className="item-card" key={project.id}>
            <h3>{project.name}</h3>
            <p>{project.description}</p>
            <span>{project.memberCount} members · Created by {project.createdBy}</span>
            {auth.user.role === 'ADMIN' && (
              <div style={{ marginTop: '1rem' }}>
                {editingProject === project.id ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <select multiple value={editMemberIds} onChange={e => setEditMemberIds([...e.target.selectedOptions].map(o => o.value))}>
                      {(team || []).map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                    </select>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="primary" onClick={() => updateProjectMembers(project.id)}>Save</button>
                      <button onClick={() => setEditingProject(null)}>Cancel</button>
                    </div>
                  </div>
                ) : (
                  <button onClick={() => {
                    setEditingProject(project.id);
                    setEditMemberIds((project.memberIds || []).map(String));
                  }}>Edit Members</button>
                )}
              </div>
            )}
          </article>
        ))}
      </div>
    </Page>
  );
}

function Tasks({ auth }) {
  const [refresh, setRefresh] = useState(0);
  const { data: tasks, loading } = useApi(auth, '/tasks', refresh);
  const { data: team } = useApi(auth, '/users/team');
  const { data: projects, loading: projectsLoading, error: projectsError } = useApi(auth, '/projects');
  const [form, setForm] = useState({ title: '', description: '', assignedToId: '', projectId: '', deadline: '' });
  const grouped = useMemo(() => groupTasks(tasks || []), [tasks]);
  const canAssignTask = Boolean(form.assignedToId && form.projectId);

  useEffect(() => {
    if (!form.assignedToId && team?.length) {
      setForm(current => ({ ...current, assignedToId: String(team[0].id) }));
    }
  }, [team, form.assignedToId]);

  useEffect(() => {
    if (!form.projectId && projects?.length) {
      setForm(current => ({ ...current, projectId: String(projects[0].id) }));
    }
  }, [projects, form.projectId]);

  async function createTask(event) {
    event.preventDefault();
    await fetch(`${API_BASE}/tasks`, {
      method: 'POST',
      headers: headers(auth),
      body: JSON.stringify(form)
    });
    setForm({ title: '', description: '', assignedToId: '', projectId: '', deadline: '' });
    setRefresh(refresh + 1);
  }

  async function updateStatus(task, status) {
    await fetch(`${API_BASE}/tasks/${task.id}`, {
      method: 'PUT',
      headers: headers(auth),
      body: JSON.stringify({ status })
    });
    setRefresh(refresh + 1);
  }

  return (
    <Page title="Tasks" subtitle="Track work by status and update progress quickly.">
      {auth.user.role === 'ADMIN' && (
        <form className="inline-form task-form" onSubmit={createTask}>
          <input placeholder="Task title" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
          <input placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          <select value={form.assignedToId} onChange={e => setForm({ ...form, assignedToId: e.target.value })} required>
            <option value="">Assignee</option>
            {(team || []).map(user => <option key={user.id} value={user.id}>{user.name}</option>)}
          </select>
          <select value={form.projectId} onChange={e => setForm({ ...form, projectId: e.target.value })} required>
            <option value="">{projectsLoading ? 'Loading projects...' : 'Project'}</option>
            {(projects || []).map(project => <option key={project.id} value={project.id}>{project.name}</option>)}
          </select>
          <input type="date" value={form.deadline} onChange={e => setForm({ ...form, deadline: e.target.value })} />
          <button className="primary" disabled={!canAssignTask}><Plus size={16} />Assign</button>
        </form>
      )}
      {projectsError && <p className="error">{projectsError}</p>}
      {!projectsLoading && auth.user.role === 'ADMIN' && !projectsError && (projects || []).length === 0 && (
        <p className="notice">Create a project first, then assign tasks to it.</p>
      )}
      {!loading && (tasks || []).length === 0 && (
        <p className="notice">No tasks assigned yet.</p>
      )}
      <div className="kanban">
        {['TODO', 'IN_PROGRESS', 'DONE'].map(status => (
          <section className="column" key={status}>
            <h2>{status.replace('_', ' ')}</h2>
            {(grouped[status] || []).map(task => (
              <article className="item-card task-card" key={task.id}>
                <h3>{task.title}</h3>
                <p>{task.description}</p>
                <span>{task.projectName} · {task.assignedToName}</span>
                <select value={task.status} onChange={e => updateStatus(task, e.target.value)}>
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
              </article>
            ))}
          </section>
        ))}
      </div>
    </Page>
  );
}

function Profile({ auth }) {
  return (
    <Page title="Profile" subtitle="Your signed-in account and access level.">
      <article className="profile-card">
        <Users size={34} />
        <h2>{auth.user.name}</h2>
        <p>{auth.user.email}</p>
        <strong>{auth.user.role}</strong>
      </article>
    </Page>
  );
}

function Page({ title, subtitle, children }) {
  return (
    <section className="page">
      <header>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </header>
      {children}
    </section>
  );
}

function Stat({ label, value }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function headers(auth) {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${auth.token}`
  };
}

function groupTasks(tasks) {
  return tasks.reduce((groups, task) => {
    groups[task.status] = groups[task.status] || [];
    groups[task.status].push(task);
    return groups;
  }, {});
}

createRoot(document.getElementById('root')).render(<App />);
