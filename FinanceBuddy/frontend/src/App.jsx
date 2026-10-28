
import { Toaster } from 'react-hot-toast'
import AppRoutes from './routes/AppRoutes'
import AIChatbox from './components/common/AIChatbox'

function App() {
  return (
    <>
      <AppRoutes />
      <AIChatbox />
      <Toaster position="top-right" />
    </>
  )
}

export default App
