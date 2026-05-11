import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import HomeDashboardView from '../HomeDashboardView.vue'
import { useRouter, useRoute } from 'vue-router'

// Mock vue-router
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useRouter: vi.fn(),
    useRoute: vi.fn(),
    createRouter: vi.fn().mockReturnValue({
      beforeEach: vi.fn()
    }),
    createWebHistory: vi.fn()
  }
})

describe('HomeDashboardView.vue Navigation Logic', () => {
  let pushMock

  beforeEach(() => {
    pushMock = vi.fn()
    useRouter.mockReturnValue({ push: pushMock })
    useRoute.mockReturnValue({ query: {} })
    localStorage.clear()
  })

  it('should navigate to AI chat when clicking the AI chat icon', async () => {
    const wrapper = mount(HomeDashboardView, {
      global: {
        stubs: {
          'el-icon': true,
          'el-avatar': true,
          'el-button': true
        }
      }
    })
    
    // Find the AI chat button and trigger click
    await wrapper.find('.ai-chat-btn').trigger('click')
    
    // Expect redirection to /chat
    expect(pushMock).toHaveBeenCalledWith('/chat')
  })
})
