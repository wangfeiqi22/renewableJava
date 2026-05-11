import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import DriverProfileView from '../DriverProfileView.vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

// Mock dependencies
const pushMock = vi.fn()
const replaceMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
    replace: replaceMock
  })
}))

vi.mock('@/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn()
    }
  }
})

describe('DriverProfileView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    // Mock navigator.geolocation
    global.navigator.geolocation = {
      getCurrentPosition: vi.fn().mockImplementation((success) => 
        success({ coords: { latitude: 39.9, longitude: 116.4 } })
      )
    }
  })

  const setDriver = (fleetId = null) => {
    localStorage.setItem('user', JSON.stringify({
      id: 'd1',
      role: 'driver',
      fleetId
    }))
  }

  it('renders personal exclusive area for non-fleet drivers', async () => {
    setDriver(null)
    api.get.mockImplementation((url) => {
      if (url === '/driver/order/ongoing') return Promise.resolve({ data: [] })
      if (url === '/driver/order/stats') return Promise.resolve({ data: { last30DaysCompleted: 10 } })
      return Promise.resolve({ data: [] })
    })

    const wrapper = mount(DriverProfileView)
    await flushPromises()

    expect(wrapper.find('.personal-exclusive-area').exists()).toBe(true)
    expect(wrapper.find('.create-order-fab').exists()).toBe(true)
    // Empty state should be visible
    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('sorts ongoing orders by createdAt descending and limits to 3', async () => {
    setDriver(null)
    const mockOrders = [
      { id: 1, createdAt: '2023-10-01T10:00:00Z', status: 20 },
      { id: 2, createdAt: '2023-10-03T10:00:00Z', status: 30 },
      { id: 3, createdAt: '2023-10-02T10:00:00Z', status: 25 },
      { id: 4, createdAt: '2023-10-04T10:00:00Z', status: 40 },
    ]
    
    api.get.mockImplementation((url) => {
      if (url.includes('/orders/user/')) return Promise.resolve({ data: mockOrders })
      return Promise.resolve({ data: [] })
    })

    const wrapper = mount(DriverProfileView)
    await flushPromises()

    const cards = wrapper.findAll('.ongoing-card')
    expect(cards.length).toBe(3)
    // The latest is id: 4, then id: 2, then id: 3
    // We can check if it triggers goToOrderDetail with correct ID on click
    await cards[0].trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/order/detail/4')
  })

  it('navigates to /order/create when clicking create order button', async () => {
    setDriver(null)
    api.get.mockResolvedValue({ data: [] })
    const wrapper = mount(DriverProfileView)
    await flushPromises()

    await wrapper.find('.create-order-fab').trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/order/create')
  })
})
