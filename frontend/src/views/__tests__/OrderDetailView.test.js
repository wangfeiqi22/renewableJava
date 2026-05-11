import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderDetailView from '../OrderDetailView.vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../../api'

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useRouter: vi.fn(),
    useRoute: vi.fn(),
  }
})

vi.mock('../../api', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('OrderDetailView.vue', () => {
  let pushMock
  let backMock

  beforeEach(() => {
    pushMock = vi.fn()
    backMock = vi.fn()
    useRouter.mockReturnValue({ push: pushMock, back: backMock })
    useRoute.mockReturnValue({ params: { id: '123' } })
    vi.clearAllMocks()
  })

  it('renders order details correctly', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('/logs')) {
        return Promise.resolve({ data: [] })
      }
      return Promise.resolve({
        data: {
          id: 123,
          orderNo: 'ORD123456',
          status: 60,
          pickupAddress: 'Test Address',
          wasteType: 'construction',
          estVolume: 15,
          createdAt: '2026-03-04T14:30:00'
        }
      })
    })

    const wrapper = mount(OrderDetailView, {
      global: {
        stubs: {
          'el-icon': true,
          'el-skeleton': true,
          'el-empty': true,
          'el-button': true,
          'el-tag': true,
          'el-dialog': true
        }
      }
    })

    // wait for onMounted and fetch
    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.order-no').text()).toContain('ORD123456')
    expect(wrapper.find('.status-text').text()).toBe('已完成')
  })
})