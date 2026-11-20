import { useEffect, useState } from 'react'
import HomePage from './pages/HomePage'
import AdminPage from './pages/AdminPage'

// Minimal hash routing until the app needs a real router
function useHash() {
  const [hash, setHash] = useState(window.location.hash)
  useEffect(() => {
    const onChange = () => setHash(window.location.hash)
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }, [])
  return hash
}

function App() {
  const hash = useHash()
  return hash === '#/admin' ? <AdminPage /> : <HomePage />
}

export default App
