import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ChatView from '../ChatView.vue'
import AiChatWidget from '../../components/AiChatWidget.vue'

// Mock vue-router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  }),
  useRoute: () => ({
    query: {}
  })
}))

// Mock API
vi.mock('../../api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: { id: 'mock-session-id' } })
  }
}))

describe('ChatView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    localStorage.setItem('user', JSON.stringify({ id: 1, role: 'individual' }))
  })

  it('renders the chat widget component', () => {
    const wrapper = mount(ChatView, {
      global: {
        components: { AiChatWidget }
      }
    })

    // Should contain the chat widget
    expect(wrapper.findComponent(AiChatWidget).exists()).toBe(true)
    
    // Should have the correct layout classes
    expect(wrapper.find('.chat-page').exists()).toBe(true)
  })

  it('contains the EcoClear brand header within the widget', () => {
    const wrapper = mount(ChatView, {
      global: {
        components: { AiChatWidget }
      }
    })
    
    expect(wrapper.text()).toContain('EcoClear')
    expect(wrapper.text()).toContain('您好，请问有什么可以帮您？')
  })

  it('contains the microphone input card for user typing', () => {
    const wrapper = mount(ChatView, {
      global: {
        components: { AiChatWidget }
      }
    })

    const inputCard = wrapper.find('.input-card')
    expect(inputCard.exists()).toBe(true)
    
    const input = wrapper.find('input[type="text"]')
    expect(input.attributes('placeholder')).toBe('说出或输入您的需求...')
  })
})