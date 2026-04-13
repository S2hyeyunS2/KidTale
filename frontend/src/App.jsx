import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import CreateStory from './pages/CreateStory'
import Preview from './pages/Preview'
import OrderForm from './pages/OrderForm'
import OrderComplete from './pages/OrderComplete'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"                element={<Home />} />
        <Route path="/create"          element={<CreateStory />} />
        <Route path="/preview/:id"     element={<Preview />} />
        <Route path="/order/:storyId"  element={<OrderForm />} />
        <Route path="/complete/:orderId" element={<OrderComplete />} />
      </Routes>
    </BrowserRouter>
  )
}
