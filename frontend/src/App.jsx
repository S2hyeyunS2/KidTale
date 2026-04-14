import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Home from './pages/Home'
import CreateStory from './pages/CreateStory'
import Preview from './pages/Preview'
import OrderForm from './pages/OrderForm'
import OrderComplete from './pages/OrderComplete'
import Signup from './pages/Signup'
import MyPage from './pages/MyPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/"                  element={<Home />} />
          <Route path="/create"            element={<CreateStory />} />
          <Route path="/preview/:id"       element={<Preview />} />
          <Route path="/order/:storyId"    element={<OrderForm />} />
          <Route path="/complete/:orderId" element={<OrderComplete />} />
          <Route path="/signup"            element={<Signup />} />
          <Route path="/mypage"            element={<MyPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
