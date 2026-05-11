import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AiChatWidget from '../AiChatWidget.vue'

// Mock vue-router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

// Mock API
vi.mock('../../api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: { id: 'mock-session-id' } })
  }
}))

// Helper to mock localStorage
const setMockUser = (userObj) => {
  localStorage.setItem('user', JSON.stringify(userObj))
}

describe('AiChatWidget.vue Avatar Routing Logic', () => {
  let consoleLogSpy;

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    consoleLogSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
  })

  afterEach(() => {
    consoleLogSpy.mockRestore()
  })

  it('should navigate to /login if no user is logged in', async () => {
    // No user in localStorage
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/login')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'guest' })
    )
  })

  it('should navigate to /home for normal user (individual)', async () => {
    setMockUser({ id: 1, role: 'individual' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/home')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'individual' })
    )
  })

  it('should navigate to /vip/dashboard for VIP user', async () => {
    setMockUser({ id: 2, role: 'vip' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/vip/dashboard')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'vip' })
    )
  })

  it('should navigate to /admin/personal for admin user', async () => {
    setMockUser({ id: 3, role: 'admin' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/admin/personal')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'admin' })
    )
  })

  it('should navigate to /home for t1 account regardless of role', async () => {
    // 假设 t1 账号是 admin 角色，但由于用户名是 t1，应当优先被拦截跳转到 /home
    setMockUser({ id: 999, username: 't1', role: 'admin' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/home')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userId: 999 })
    )
  })

  it('should navigate to /profile/enterprise for enterprise roles', async () => {
    setMockUser({ id: 4, role: 'station' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/profile/enterprise')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'station' })
    )
  })

  it('should navigate to /profile/driver for driver role', async () => {
    setMockUser({ id: 5, role: 'driver' })
    const wrapper = mount(AiChatWidget)
    const avatarBtn = wrapper.find('.user-avatar-btn')
    
    await avatarBtn.trigger('click')
    
    expect(pushMock).toHaveBeenCalledWith('/profile/driver')
    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Track Event: Avatar Clicked', 
      expect.objectContaining({ userType: 'driver' })
    )
  })
})