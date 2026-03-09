/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderTimeline from '../src/components/OrderTimeline.vue'

describe('OrderTimeline.vue', () => {
  it('renders correctly even when photos is undefined', () => {
    const wrapper = mount(OrderTimeline, {
      props: {
        logs: [
          { id: 1, actionName: '开始装车', newStatus: 30, createdAt: new Date().toISOString() }
        ],
        photos: undefined
      },
      global: {
        stubs: {
          'el-timeline': { template: '<div><slot /></div>' },
          'el-timeline-item': { template: '<div><slot /></div>' },
          'el-card': { template: '<div><slot /></div>' },
          'el-tag': { template: '<span><slot /></span>' },
          'el-image': true,
          'el-icon': true
        }
      }
    })
    
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('开始装车')
  })

  it('renders correctly when photos is null', () => {
    const wrapper = mount(OrderTimeline, {
      props: {
        logs: [
          { id: 1, actionName: '开始装车', newStatus: 30, createdAt: new Date().toISOString() }
        ],
        photos: null
      },
      global: {
        stubs: {
          'el-timeline': { template: '<div><slot /></div>' },
          'el-timeline-item': { template: '<div><slot /></div>' },
          'el-card': { template: '<div><slot /></div>' },
          'el-tag': { template: '<span><slot /></span>' },
          'el-image': true,
          'el-icon': true
        }
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  it('filters photos by nodeType correctly', () => {
    const logs = [{ id: 1, actionName: '开始装车', newStatus: 30, createdAt: new Date().toISOString() }]
    const photos = [{ nodeType: 'load', fileUrl: 'test.jpg' }, { nodeType: 'unload', fileUrl: 'other.jpg' }]
    
    const wrapper = mount(OrderTimeline, {
      props: { logs, photos },
      global: {
        stubs: {
          'el-timeline': { template: '<div><slot /></div>' },
          'el-timeline-item': { template: '<div><slot /></div>' },
          'el-card': { template: '<div><slot /></div>' },
          'el-tag': { template: '<span><slot /></span>' },
          'el-image': { template: '<img class="log-photo" />' },
          'el-icon': true
        }
      }
    })
    
    // Should match 1 photo (load -> 30)
    expect(wrapper.findAll('.log-photo').length).toBe(1)
  })
})
