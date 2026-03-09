// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import DriverTaskView from '../../src/views/DriverTaskView.vue'
import { describe, it, expect, vi } from 'vitest'

// Mock dependencies
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  }),
  createRouter: vi.fn(() => ({})),
  createWebHistory: vi.fn()
}))

// Mock @amap/amap-jsapi-loader
vi.mock('@amap/amap-jsapi-loader', () => ({
  load: vi.fn(() => Promise.resolve({
    Map: vi.fn(),
    Plugin: vi.fn()
  }))
}))

vi.mock('../../src/api', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: [] })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} }))
  }
}))

// Mock piexifjs
vi.mock('piexifjs', () => ({
  default: {
    load: vi.fn(),
    dump: vi.fn(),
    insert: vi.fn()
  }
}))

// Add environment setup for document/window if needed or use environment: jsdom in config
// But since we are running with default vitest config which might not include jsdom
// We will mock document if missing or assume vitest needs config
// Let's assume we need to configure test environment in the test file or config.
// Adding comment to use jsdom environment
// @vitest-environment jsdom

describe('DriverTaskView Waste Type Mapping', () => {
  it('correctly maps waste types to Chinese', async () => {
    const wrapper = mount(DriverTaskView)
    
    // Access the component's internal function (exposed via setup or just testing logic if extracted)
    // Since it's <script setup>, functions are not automatically exposed to wrapper.vm unless defineExpose is used.
    // However, we can test the RENDERED output if we provide data.
    
    // BUT, since we mocked the API to return empty array, the list is empty.
    // We need to inject data.
    
    // Let's create a helper to access the function or just test the logic in isolation if we can't easily access component internals without defineExpose.
    // Actually, for <script setup>, we can test the template rendering.
    
    // Mock the tasks data
    const mockTasks = [
      { id: 1, orderNo: '12345678', wasteType: 'household', estWeight: 10, status: 20, pickupAddress: 'Addr 1' },
      { id: 2, orderNo: '87654321', wasteType: 'CONSTRUCTION', estWeight: 20, status: 20, pickupAddress: 'Addr 2' },
      { id: 3, orderNo: '11223344', wasteType: 'bulky', estWeight: 30, status: 20, pickupAddress: 'Addr 3' },
      { id: 4, orderNo: '55667788', wasteType: 'KITCHEN', estWeight: 40, status: 20, pickupAddress: 'Addr 4' },
      { id: 5, orderNo: '99887766', wasteType: 'RECYCLABLE', estWeight: 5, status: 20, pickupAddress: 'Addr 5' },
      { id: 6, orderNo: '44332211', wasteType: 'HAZARDOUS', estWeight: 2, status: 20, pickupAddress: 'Addr 6' },
      { id: 7, orderNo: '00000000', wasteType: 'OTHER', estWeight: 1, status: 20, pickupAddress: 'Addr 7' }
    ]
    
    // We need to override the fetchTasks or modify the ref value.
    // In Vue Test Utils with <script setup>, it's hard to modify internal refs directly.
    // Best approach is to mock the API response.
    
    const api = (await import('../../src/api')).default
    api.get.mockResolvedValueOnce({ data: mockTasks }) // For fetchTasks
    api.get.mockResolvedValueOnce({ data: [] }) // For fetchPoolOrders
    
    // Re-mount to trigger onMounted
    const wrapperWithData = mount(DriverTaskView)
    
    // Wait for async calls
    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapperWithData.vm.$nextTick()
    
    // Check rendered text
    const textElements = wrapperWithData.findAll('.info-row .text')
    // We have 2 info-rows per card. The second one contains waste type.
    // Structure:
    // <div class="info-row"><icon> Address</div>
    // <div class="info-row"><icon> Type | Weight</div>
    
    // We want the text inside the second info-row.
    // The selector `.info-row .text` matches the span with class "text".
    // In the template: <span class="text">{{ getWasteTypeName(order.wasteType) }}</span>
    
    const content = wrapperWithData.text()
    
    expect(content).toContain('生活垃圾')
    expect(content).toContain('建筑垃圾')
    expect(content).toContain('大件垃圾')
    expect(content).toContain('厨余垃圾')
    expect(content).toContain('可回收物')
    expect(content).toContain('有害垃圾')
    expect(content).toContain('其他垃圾')
    
    // Verify specific mapping if possible, but presence is good enough for now.
    
    // Also check "Pending" tab is active (default for no-fleet user or if we switch)
    // The mock data status=20 puts them in Pending tab logic?
    // fetchTasks filters status >= 20.
    // The pending tab iterates `tasks`.
    
    // Verify "Pool" tab logic if fleet user
    // ...
  })
})
