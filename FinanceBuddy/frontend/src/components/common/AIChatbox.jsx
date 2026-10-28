
import { useState } from 'react'
import api from '../../services/api'
import './AIChatbox.css'

function AIChatbox() {
  const [isOpen, setIsOpen] = useState(false)
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      text: 'Hi! I am FinanceBuddy AI. Ask me about budgeting, savings, or any general question.',
    },
  ])
  const [loading, setLoading] = useState(false)

  const sendMessage = async (event) => {
    event.preventDefault()

    const text = message.trim()
    if (!text || loading) return

    setMessages((prev) => [...prev, { role: 'user', text }])
    setMessage('')
    setLoading(true)

    try {
      const response = await api.post('/ai-chat', {
        message: text,
      })

      console.log('AI Chat Success:', response.data)

      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          text:
            response.data?.reply ||
            response.data?.message ||
            'I could not generate a reply.',
        },
      ])
    } catch (error) {
      // Show useful debugging information in browser console
      console.error('AI Chat Error:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
      })

      let errorMessage = 'Could not connect to AI. Please try again.'

      if (error.response?.status === 401) {
        errorMessage = 'Session expired. Please log in again.'
      } else if (error.response?.status === 403) {
        errorMessage = 'You are not authorized to use this feature.'
      } else if (error.response?.status === 500) {
        errorMessage =
          error.response?.data?.message ||
          error.response?.data?.error ||
          'AI server error. Please check the Spring Boot backend console.'
      } else if (!error.response) {
        errorMessage =
          'Cannot reach the backend. Check whether Spring Boot is running.'
      } else if (
        error.response?.data?.message ||
        error.response?.data?.error
      ) {
        errorMessage =
          error.response.data.message ||
          error.response.data.error
      }

      setMessages((prev) => [
        ...prev,
        { role: 'assistant', text: errorMessage },
      ])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="ai-chat-widget">
      {isOpen && (
        <section className="ai-chat-panel">
          <header className="ai-chat-header">
            <div>
              <strong>FinanceBuddy AI</strong>
              <small>Your personal assistant</small>
            </div>

            <button
              type="button"
              className="ai-chat-close"
              onClick={() => setIsOpen(false)}
              aria-label="Close chat"
            >
              ×
            </button>
          </header>

          <div className="ai-chat-messages" aria-live="polite">
            {messages.map((item, index) => (
              <div
                key={`${index}-${item.role}`}
                className={`ai-chat-message ${item.role}`}
              >
                {item.text}
              </div>
            ))}

            {loading && (
              <div className="ai-chat-message assistant">
                Thinking...
              </div>
            )}
          </div>

          <form className="ai-chat-form" onSubmit={sendMessage}>
            <input
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Ask anything..."
              aria-label="Your message"
              maxLength={4000}
              disabled={loading}
            />

            <button
              type="submit"
              disabled={loading || !message.trim()}
            >
              {loading ? 'Sending...' : 'Send'}
            </button>
          </form>
        </section>
      )}

      <button
        type="button"
        className="ai-chat-launcher"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-label={isOpen ? 'Close AI chat' : 'Open AI chat'}
      >
        {isOpen ? '×' : '✦ AI'}
      </button>
    </div>
  )
}

export default AIChatbox
