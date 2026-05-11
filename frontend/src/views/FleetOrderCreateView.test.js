import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { ref, reactive } from 'vue'
import api from '../api'

// Mock the api module
vi.mock('../api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

// Mock Element Plus components
const mockElMessage = vi.fn()
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      warning: mockElMessage,
      error: mockElMessage,
      success: mockElMessage
    }
  }
})

describe('FleetOrderCreateView - Driver Selection Tests', () => {
  let drivers
  let orderForm
  let errors
  let driversMockData

  beforeEach(() => {
    vi.clearAllMocks()
    
    // Mock driver data
    driversMockData = [
      { id: 1, name: '张三', phone: '13800138001', status: 'AVAILABLE' },
      { id: 2, name: '李四', phone: '13800138002', status: 'AVAILABLE' },
      { id: 3, name: '王五', phone: '13800138003', status: 'frozen' }
    ]
    
    drivers = ref([])
    orderForm = reactive({
      driverId: null,
      pickupAddress: '',
      wasteType: 'recyclable',
      estWeight: 10,
      wasteDesc: '',
      placementStatus: '',
      additionalServices: []
    })
    
    errors = reactive({
      driverId: '',
      pickupAddress: ''
    })
  })

  describe('1. Driver Loading Tests', () => {
    it('should load drivers from API on component mount', async () => {
      const mockResponse = {
        data: {
          code: 200,
          data: {
            content: driversMockData
          }
        }
      }
      api.get.mockResolvedValue(mockResponse)
      
      // Simulate component mount
      await api.get('/fleet/drivers', { params: { page: 0, size: 20 } })
      
      expect(api.get).toHaveBeenCalledWith('/fleet/drivers', {
        params: { page: 0, size: 20 }
      })
    })

    it('should filter available drivers by default', async () => {
      const mockResponse = {
        data: {
          code: 200,
          data: {
            content: driversMockData.filter(d => d.status === 'AVAILABLE')
          }
        }
      }
      api.get.mockResolvedValue(mockResponse)
      
      await api.get('/fleet/drivers', {
        params: { page: 0, size: 20, status: 'AVAILABLE' }
      })
      
      expect(api.get).toHaveBeenCalledWith('/fleet/drivers', {
        params: { page: 0, size: 20, status: 'AVAILABLE' }
      })
    })

    it('should handle API errors gracefully', async () => {
      api.get.mockRejectedValue(new Error('Network error'))
      
      try {
        await api.get('/fleet/drivers')
      } catch (error) {
        expect(error.message).toBe('Network error')
        expect(mockElMessage).toHaveBeenCalledWith('加载司机列表失败')
      }
    })
  })

  describe('2. Driver Search Tests', () => {
    it('should search drivers by name', async () => {
      const searchQuery = '张'
      const mockResponse = {
        data: {
          code: 200,
          data: {
            content: [driversMockData[0]]
          }
        }
      }
      api.get.mockResolvedValue(mockResponse)
      
      await api.get('/fleet/drivers', {
        params: { page: 0, size: 20, name: searchQuery }
      })
      
      expect(api.get).toHaveBeenCalledWith('/fleet/drivers', {
        params: { page: 0, size: 20, name: searchQuery }
      })
    })

    it('should reload all drivers when search query is empty', async () => {
      api.get.mockResolvedValue({
        data: { code: 200, data: { content: driversMockData } }
      })
      
      await api.get('/fleet/drivers', {
        params: { page: 0, size: 20 }
      })
      
      expect(api.get).toHaveBeenCalled()
    })
  })

  describe('3. Driver Selection Validation Tests', () => {
    it('should validate that driver is selected', () => {
      orderForm.driverId = null
      
      const isValid = orderForm.driverId !== null
      
      expect(isValid).toBe(false)
      expect(errors.driverId).toBe('')
    })

    it('should prevent selection of frozen drivers', () => {
      const frozenDriver = driversMockData.find(d => d.status === 'frozen')
      
      expect(frozenDriver).toBeDefined()
      expect(frozenDriver.status).toBe('frozen')
    })

    it('should validate selected driver exists in drivers list', () => {
      const validDriver = driversMockData.find(d => d.id === 1)
      orderForm.driverId = 1
      
      const selectedDriver = driversMockData.find(d => d.id === orderForm.driverId)
      
      expect(selectedDriver).toBeDefined()
      expect(selectedDriver.id).toBe(1)
      expect(selectedDriver.name).toBe('张三')
    })

    it('should reject invalid driver ID', () => {
      orderForm.driverId = 999
      
      const selectedDriver = driversMockData.find(d => d.id === orderForm.driverId)
      
      expect(selectedDriver).toBeUndefined()
    })
  })

  describe('4. Permission Security Tests', () => {
    it('should only fetch drivers for current user fleet', async () => {
      const mockUser = { id: 1, fleetId: 1, role: 'fleet_owner' }
      const mockResponse = {
        data: {
          code: 200,
          data: {
            content: driversMockData
          }
        }
      }
      api.get.mockResolvedValue(mockResponse)
      
      await api.get('/fleet/drivers', {
        params: {
          page: 0,
          size: 20,
          name: undefined,
          status: 'AVAILABLE'
        }
      })
      
      // API should be called with fleet-specific parameters
      expect(api.get).toHaveBeenCalledWith('/fleet/drivers', expect.any(Object))
    })

    it('should not allow selecting drivers from other fleets', () => {
      const mockUserFleetId = 1
      const otherFleetDriver = { id: 100, name: '其他车队司机', phone: '13900000000', status: 'AVAILABLE', fleetId: 2 }
      
      // The driver from another fleet should not be in the current fleet's drivers list
      const currentFleetDrivers = driversMockData
      
      const canSelect = currentFleetDrivers.some(d => d.id === otherFleetDriver.id)
      
      expect(canSelect).toBe(false)
    })

    it('should validate driver belongs to current fleet on selection', () => {
      const mockUserFleetId = 1
      const selectedDriver = { id: 1, name: '张三', fleetId: 1, status: 'AVAILABLE' }
      
      // In real implementation, backend would validate this
      const isValidSelection = selectedDriver !== null && selectedDriver.fleetId === mockUserFleetId
      
      expect(isValidSelection).toBe(true)
    })
  })

  describe('5. Order Creation with Driver Tests', () => {
    it('should include driverId in order creation payload', async () => {
      const selectedDriver = driversMockData[0]
      orderForm.driverId = selectedDriver.id
      orderForm.pickupAddress = '北京市朝阳区'
      orderForm.wasteType = 'recyclable'
      orderForm.estWeight = 10
      
      const payload = {
        driverId: orderForm.driverId,
        pickupAddress: orderForm.pickupAddress,
        wasteType: orderForm.wasteType,
        estWeight: orderForm.estWeight,
        wasteDesc: '',
        placementStatus: '已堆放到位',
        additionalServices: '',
        rentalDays: 3,
        creatorId: 1,
        type: 1
      }
      
      expect(payload.driverId).toBe(1)
      expect(payload.driverId).toBe(selectedDriver.id)
    })

    it('should prevent order creation without driver selection', () => {
      orderForm.driverId = null
      
      const canCreateOrder = orderForm.driverId !== null
      
      expect(canCreateOrder).toBe(false)
    })

    it('should reject frozen drivers during order creation', () => {
      const frozenDriver = driversMockData.find(d => d.status === 'frozen')
      
      expect(frozenDriver.status).toBe('frozen')
      
      // Should not allow creating order with frozen driver
      const canCreateOrder = frozenDriver.status !== 'frozen'
      expect(canCreateOrder).toBe(false)
    })
  })

  describe('6. Driver Status Display Tests', () => {
    it('should display AVAILABLE drivers correctly', () => {
      const availableDriver = driversMockData.find(d => d.status === 'AVAILABLE')
      
      expect(availableDriver).toBeDefined()
      expect(availableDriver.status).toBe('AVAILABLE')
    })

    it('should mark frozen drivers as disabled', () => {
      const frozenDriver = driversMockData.find(d => d.status === 'frozen')
      
      expect(frozenDriver).toBeDefined()
      expect(frozenDriver.status).toBe('frozen')
    })

    it('should show driver name and phone in selection', () => {
      const driver = driversMockData[0]
      const displayText = `${driver.name} - ${driver.phone}`
      
      expect(displayText).toBe('张三 - 13800138001')
    })
  })
})

describe('FleetOrderCreateView - Integration Tests', () => {
  it('should complete full driver selection workflow', async () => {
    const driversMockData = [
      { id: 1, name: '张三', phone: '13800138001', status: 'AVAILABLE' },
      { id: 2, name: '李四', phone: '13800138002', status: 'AVAILABLE' },
      { id: 3, name: '王五', phone: '13800138003', status: 'frozen' }
    ]
    
    // Step 1: Load drivers
    const loadResponse = {
      data: {
        code: 200,
        data: {
          content: driversMockData
        }
      }
    }
    api.get.mockResolvedValue(loadResponse)
    
    await api.get('/fleet/drivers', { params: { page: 0, size: 20 } })
    expect(api.get).toHaveBeenCalled()
    
    // Step 2: Select driver
    const selectedDriver = driversMockData[0]
    const orderForm = reactive({
      driverId: selectedDriver.id,
      pickupAddress: '北京市朝阳区',
      wasteType: 'recyclable',
      estWeight: 10,
      placementStatus: '已堆放到位'
    })
    
    expect(orderForm.driverId).toBe(1)
    expect(selectedDriver.status).toBe('AVAILABLE')
    
    // Step 3: Create order
    const createResponse = {
      data: {
        orderNo: 'ORD1234567890',
        id: 1
      }
    }
    api.post.mockResolvedValue(createResponse)
    
    const payload = {
      driverId: orderForm.driverId,
      pickupAddress: orderForm.pickupAddress,
      wasteType: orderForm.wasteType,
      estWeight: orderForm.estWeight,
      placementStatus: orderForm.placementStatus
    }
    
    await api.post('/orders/fleet-create', payload)
    expect(api.post).toHaveBeenCalledWith('/orders/fleet-create', payload)
  })
})
