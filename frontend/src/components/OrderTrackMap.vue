<template>
  <div class="map-container">
    <div id="amap-container" class="amap-box"></div>
    <div class="map-controls">
      <el-button-group>
        <el-button type="primary" size="small" @click="startAnimation" :disabled="!canAnimate">
          <el-icon><VideoPlay /></el-icon> 轨迹回放
        </el-button>
        <el-button type="info" size="small" @click="toggleLayer">
          <el-icon><MapLocation /></el-icon> 切换图层
        </el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { VideoPlay, MapLocation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  startPoint: {
    type: Object, // { lat: Number, lon: Number }
    default: () => null
  },
  endPoint: {
    type: Object, // { lat: Number, lon: Number }
    default: () => ({ lat: 39.984120, lon: 116.307484 }) // Default: Beijing Haidian
  },
  status: {
    type: Number,
    default: 10
  }
})

// ShallowRefs for AMap instances to avoid Vue reactivity performance issues
const map = shallowRef(null)
const driving = shallowRef(null)
const marker = shallowRef(null)
const polyline = shallowRef(null)
const passedPolyline = shallowRef(null)
const trafficLayer = shallowRef(null)

const canAnimate = ref(false)
const isTrafficOn = ref(false)
const pathPath = ref([]) // Array of [lon, lat]

// Replace with your keys
const AMAP_KEY = 'YOUR_AMAP_KEY' 
const AMAP_SECURITY_CODE = 'YOUR_SECURITY_CODE'

onMounted(() => {
  window._AMapSecurityConfig = {
    securityJsCode: AMAP_SECURITY_CODE,
  }
  
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})

const initMap = () => {
  AMapLoader.load({
    key: AMAP_KEY,
    version: '2.0',
    plugins: ['AMap.Driving', 'AMap.MoveAnimation', 'AMap.ToolBar', 'AMap.Scale'],
  })
    .then((AMap) => {
      map.value = new AMap.Map('amap-container', {
        viewMode: '3D',
        zoom: 11,
        center: [116.397428, 39.90923], // Default Beijing
      })

      map.value.addControl(new AMap.ToolBar())
      map.value.addControl(new AMap.Scale())

      trafficLayer.value = new AMap.TileLayer.Traffic({
        zIndex: 10,
        visible: false
      })
      map.value.add(trafficLayer.value)

      if (props.startPoint) {
        planRoute()
      }
    })
    .catch((e) => {
      console.error(e)
      ElMessage.error('地图加载失败，请检查Key配置')
    })
}

const planRoute = () => {
  if (!map.value || !props.startPoint) return

  const startLngLat = [props.startPoint.lon || 116.40, props.startPoint.lat || 39.90]
  const endLngLat = [props.endPoint.lon, props.endPoint.lat]

  // Add Start/End Markers
  new AMap.Marker({
    map: map.value,
    position: startLngLat,
    icon: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png',
    title: '起点'
  })

  new AMap.Marker({
    map: map.value,
    position: endLngLat,
    icon: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png',
    title: '终点'
  })

  // Route Planning
  const drivingPlugin = new AMap.Driving({
    map: map.value,
    hideMarkers: true, // Use custom markers
    showTraffic: false,
    autoFitView: true
  })

  drivingPlugin.search(startLngLat, endLngLat, (status, result) => {
    if (status === 'complete') {
      if (result.routes && result.routes.length) {
        parseRoute(result.routes[0])
      }
    } else {
      console.error('Path planning failed:', result)
    }
  })
}

const parseRoute = (route) => {
  pathPath.value = []
  route.steps.forEach(step => {
    step.path.forEach(p => {
      pathPath.value.push([p.lng, p.lat])
    })
  })

  // Draw full path (Gray)
  polyline.value = new AMap.Polyline({
    map: map.value,
    path: pathPath.value,
    showDir: true,
    strokeColor: "#999",  // line color
    strokeOpacity: 0.8,   // line opacity
    strokeWeight: 6,      // line width
  })

  // Vehicle Marker
  marker.value = new AMap.Marker({
    map: map.value,
    position: pathPath.value[0],
    icon: 'https://webapi.amap.com/images/car.png',
    offset: new AMap.Pixel(-26, -13),
    autoRotation: true,
    angle: -90,
  })

  // Draw Passed Path (Green) - Simulate based on status
  // If status >= 40 (Transporting), we assume some progress.
  // If status >= 60 (Done), full progress.
  let progressIndex = 0
  if (props.status >= 60) {
    progressIndex = pathPath.value.length
  } else if (props.status >= 40) {
    progressIndex = Math.floor(pathPath.value.length * 0.5) // Simulate 50%
  }

  if (progressIndex > 0) {
    const passedPath = pathPath.value.slice(0, progressIndex)
    passedPolyline.value = new AMap.Polyline({
      map: map.value,
      path: passedPath,
      strokeColor: "#28F",  // Green/Blue
      strokeWeight: 6,
    })
    marker.value.setPosition(pathPath.value[progressIndex - 1] || pathPath.value[0])
  }

  canAnimate.value = true
}

const startAnimation = () => {
  if (!marker.value || !pathPath.value.length) return
  
  marker.value.stopMove()
  marker.value.setPosition(pathPath.value[0]) // Reset to start
  
  marker.value.moveAlong(pathPath.value, {
    duration: 200, // speed
    autoRotation: true,
  })
}

const toggleLayer = () => {
  if (trafficLayer.value) {
    isTrafficOn.value = !isTrafficOn.value
    if (isTrafficOn.value) {
      trafficLayer.value.show()
    } else {
      trafficLayer.value.hide()
    }
  }
}

watch(() => props.startPoint, () => {
  if (map.value) {
    map.value.clearMap()
    planRoute()
  }
})
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.amap-box {
  width: 100%;
  height: 100%;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px;
  border-radius: 4px;
}
</style>
