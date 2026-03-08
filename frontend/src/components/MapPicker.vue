<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择地址"
    width="80%"
    :fullscreen="true"
    @opened="initMap"
    :before-close="handleClose"
    class="map-picker-dialog"
  >
    <div class="map-container">
      <div id="amap-container" class="amap-container"></div>
      <div class="map-controls">
        <div class="search-box">
          <el-input id="map-search-input" v-model="searchKeyword" placeholder="输入关键字搜索地址" />
        </div>
        <el-button @click="recenterToCurrentLocation" class="location-btn">
          回到我的位置
        </el-button>
      </div>
      <div class="address-display">
        <p><strong>当前选择地址：</strong></p>
        <p>{{ selectedAddress.formattedAddress || '请在地图上选点或搜索' }}</p>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedAddress.formattedAddress">
          确认地址
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:visible', 'address-selected']);

const dialogVisible = ref(props.visible);
const searchKeyword = ref('');
const selectedAddress = ref({});
let map = null;
let marker = null;
let geocoder = null;
let autocomplete = null;

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal;
});

const handleClose = () => {
  emit('update:visible', false);
};

const handleConfirm = () => {
  if (!selectedAddress.value.formattedAddress) {
    ElMessage.warning('请先选择一个地址');
    return;
  }
  emit('address-selected', selectedAddress.value);
  handleClose();
};

const setAddress = (lnglat, addressComponent) => {
    const { province, city, district, township, street, streetNumber } = addressComponent;
    const formattedAddress = `${province || ''}${city || ''}${district || ''}${township || ''}${street || ''}${streetNumber || ''}`;
    
    selectedAddress.value = {
        lng: lnglat.lng,
        lat: lnglat.lat,
        province: province,
        city: city,
        district: district,
        street: street + (streetNumber || ''),
        formattedAddress: formattedAddress,
        detailAddress: township, // 默认将乡镇/街道作为详细地址的起点
    };

    if (marker) {
        marker.setPosition(lnglat);
    } else {
        marker = new AMap.Marker({
            position: lnglat,
            map: map,
        });
    }
    map.setCenter(lnglat);
};

const initMap = () => {
  if (!document.querySelector("#amap-container")) return;
  
  map = new AMap.Map('amap-container', {
    zoom: 12,
  });

  AMap.plugin(['AMap.Geolocation', 'AMap.Geocoder', 'AMap.Autocomplete', 'AMap.PlaceSearch'], () => {
    // 1. Geolocation
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      buttonOffset: new AMap.Pixel(10, 20),
      zoomToAccuracy: true,
    });
    map.addControl(geolocation);
    
    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        setAddress(result.position, result.addressComponent);
      } else {
        ElMessage.error('定位失败: ' + result.message);
      }
    });

    // 2. Geocoder
    geocoder = new AMap.Geocoder({
      city: '全国',
    });

    // 3. Map click event
    map.on('click', (e) => {
      const lnglat = e.lnglat;
      geocoder.getAddress(lnglat, (status, result) => {
        if (status === 'complete' && result.regeocode) {
          setAddress(lnglat, result.regeocode.addressComponent);
        } else {
          ElMessage.error('地址解析失败');
        }
      });
    });

    // 4. Autocomplete & Search
    autocomplete = new AMap.Autocomplete({
      input: 'map-search-input',
    });
    const placeSearch = new AMap.PlaceSearch({
      map: map,
    });
    
    autocomplete.on('select', (e) => {
      placeSearch.setCity(e.poi.adcode);
      placeSearch.search(e.poi.name, (status, result) => {
         if (status === 'complete' && result.poiList.length > 0) {
            const poi = result.poiList[0];
            const lnglat = poi.location;
            const addressComponent = {
                province: poi.pname,
                city: poi.cityname,
                district: poi.adname,
                township: poi.address,
                street: '',
                streetNumber: ''
            };
            setAddress(lnglat, addressComponent);
         }
      });
    });
  });
};

const recenterToCurrentLocation = () => {
    const geolocation = new AMap.Geolocation();
    geolocation.getCurrentPosition((status, result) => {
        if (status === 'complete') {
            setAddress(result.position, result.addressComponent);
            map.setZoom(15);
        } else {
            ElMessage.error('重新定位失败: ' + result.message);
        }
    });
}

onUnmounted(() => {
  if (map) {
    map.destroy();
  }
});

</script>

<style>
.map-picker-dialog .el-dialog__body {
  padding: 0 !important;
  height: calc(100vh - 54px - 70px);
}
.map-picker-dialog .el-dialog__header {
    padding-bottom: 0;
}
.map-picker-dialog .el-dialog__footer {
    padding-top: 10px;
}
</style>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
}
.amap-container {
  width: 100%;
  height: 100%;
}
.map-controls {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  gap: 10px;
  z-index: 10;
}
.search-box {
  flex-grow: 1;
}
.address-display {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.1);
  z-index: 10;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
.address-display p {
    margin: 0;
    line-height: 1.5;
}
</style>
