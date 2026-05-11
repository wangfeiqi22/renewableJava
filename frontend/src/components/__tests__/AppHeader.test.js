import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AppHeader from '../AppHeader.vue'

const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

describe('AppHeader.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with given props', () => {
    const wrapper = mount(AppHeader, {
      props: {
        title: 'TestTitle',
        subtitle: 'TestSubtitle',
        icon: 'Menu'
      },
      global: {
        stubs: {
          'el-icon': true,
          'Van': true,
          'Menu': true
        }
      }
    })

    expect(wrapper.find('.brand-name').text()).toBe('TestTitle')
    expect(wrapper.find('.brand-sub').text()).toBe('TestSubtitle')
    // By default the AI Chat button should exist
    expect(wrapper.find('.ai-chat-btn').exists()).toBe(true)
  })

  it('handles AI chat button click', async () => {
    const wrapper = mount(AppHeader, {
      props: {
        title: 'EcoClear',
        subtitle: 'User',
        icon: 'Menu'
      },
      global: {
        stubs: {
          'el-icon': true,
          'Van': true,
          'Menu': true
        }
      }
    })

    await wrapper.find('.ai-chat-btn').trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/chat')
  })
})