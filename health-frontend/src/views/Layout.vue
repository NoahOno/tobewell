<template>
  <el-container class="layout-container">
    <el-header class="top-header">
      <div class="logo">
        <el-icon color="#67C23A" :size="30"><TrendCharts /></el-icon>
        <span>健康管理平台</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeTopModule"
        class="top-menu"
        @select="handleTopMenuSelect"
      >
        <template v-if="userInfo.role !== 'ADMIN'">
          <el-menu-item index="explore">探索</el-menu-item>
          <el-menu-item index="training">训练</el-menu-item>
          <el-menu-item index="community">社区</el-menu-item>
          <el-menu-item index="mine">我的</el-menu-item>
        </template>
        <el-menu-item v-else index="admin">系统管理</el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="user-name">{{ userInfo.nickname || userInfo.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人设置</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="sub-container">
      <el-aside v-if="activeTopModule !== 'community'" width="220px" class="aside">
        <el-menu
          :default-active="activeSidebarItem"
          class="sidebar-menu"
          @select="handleSidebarSelect"
        >
          <template v-if="activeTopModule === 'explore'">
            <el-menu-item index="/app/explore?tab=plans">
              <el-icon><Collection /></el-icon>
              <span>训练计划</span>
            </el-menu-item>
            <el-menu-item index="/app/explore?tab=courses">
              <el-icon><Timer /></el-icon>
              <span>单次课程</span>
            </el-menu-item>
            <el-menu-item index="/app/explore?tab=services">
              <el-icon><Aim /></el-icon>
              <span>健康服务</span>
            </el-menu-item>
            <el-menu-item index="/app/exercises">
              <el-icon><Bicycle /></el-icon>
              <span>动作库</span>
            </el-menu-item>
          </template>

          <template v-else-if="activeTopModule === 'training'">
            <el-menu-item index="/app/training?tab=overview">
              <el-icon><TrendCharts /></el-icon>
              <span>全局数据</span>
            </el-menu-item>
            <el-menu-item index="/app/training?tab=calendar">
              <el-icon><Calendar /></el-icon>
              <span>训练日程</span>
            </el-menu-item>
            <el-menu-item index="/app/training?tab=plans">
              <el-icon><Collection /></el-icon>
              <span>训练管理</span>
            </el-menu-item>
            <el-menu-item index="/app/training?tab=favorites">
              <el-icon><Star /></el-icon>
              <span>想练清单</span>
            </el-menu-item>
            <el-menu-item index="/app/training?tab=created">
              <el-icon><EditPen /></el-icon>
              <span>我的创建</span>
            </el-menu-item>
          </template>

          <template v-else-if="activeTopModule === 'community'">
            <el-menu-item index="/app/community">
              <el-icon><MessageBox /></el-icon>
              <span>社区广场</span>
            </el-menu-item>
          </template>

          <template v-else-if="activeTopModule === 'mine'">
            <el-menu-item index="/app/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>我的数据</span>
            </el-menu-item>
            <el-menu-item index="/app/collections">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>
            <el-menu-item index="/app/records">
              <el-icon><EditPen /></el-icon>
              <span>健康档案</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, MessageBox, TrendCharts, EditPen, Calendar, User, Timer, Aim, Collection, Star, Bicycle } from '@element-plus/icons-vue'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const userInfo = ref<any>({})

const activeTopModule = ref('mine')

watch(() => route.path, (path) => {
  if (path.includes('/admin')) activeTopModule.value = 'admin'
  else if (path.includes('/community')) activeTopModule.value = 'community'
  else if (path.includes('/explore') || path.includes('/exercises')) activeTopModule.value = 'explore'
  else if (path.includes('/training')) activeTopModule.value = 'training'
  else if (path.includes('/mine') || path.includes('/dashboard') || path.includes('/records') || path.includes('/profile')) {
    activeTopModule.value = 'mine'
  }
}, { immediate: true })

const handleTopMenuSelect = (val: string) => {
  activeTopModule.value = val
  if (val === 'community') {
    router.push('/app/community')
  } else if (val === 'explore') {
    router.push('/app/explore')
  } else if (val === 'training') {
    router.push('/app/training?tab=overview')
  } else if (val === 'mine') {
    router.push('/app/dashboard')
  } else if (val === 'admin') {
    router.push('/admin')
  }
}

const fetchUserInfo = async () => {
  try {
    const res: any = await request.get('/auth/info')
    userInfo.value = res.data
    // If admin lands on regular app routes, redirect to admin center
    if (res.data.role === 'ADMIN' && route.path.startsWith('/app')) {
      router.push('/admin')
    }
  } catch (err) {
    console.error(err)
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    request.post('/auth/logout').then(() => {
      localStorage.removeItem('token')
      router.push('/login')
    })
  } else if (command === 'profile') {
    activeTopModule.value = 'mine'
    router.push('/app/profile')
  }
}

const activeSidebarItem = computed(() => {
  if (route.path === '/app/training') {
    return route.query.tab ? `/app/training?tab=${route.query.tab}` : '/app/training?tab=calendar'
  }
  if (route.path.startsWith('/app/explore')) {
    if (route.query.tab) return `/app/explore?tab=${route.query.tab}`
    // Default highlight to "训练计划" when landing on /app/explore
    return '/app/explore?tab=plans'
  }
  return route.path
})

const handleSidebarSelect = (index: string) => {
  router.push(index)
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: var(--bg-main);
  display: flex;
  flex-direction: column;
}

.top-header {
  height: 72px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  padding: 0 32px;
  z-index: 100;
  position: sticky;
  top: 0;
}

.sub-container {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: 48px;
  font-weight: 800;
  font-size: 20px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.top-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}

.top-menu :deep(.el-menu-item) {
  font-weight: 600;
  font-size: 15px;
  border-bottom: 2px solid transparent !important;
  transition: all 0.3s ease;
}

.top-menu :deep(.el-menu-item.is-active) {
  color: var(--primary-color) !important;
  border-bottom-color: var(--primary-color) !important;
}

.header-right {
  margin-left: 20px;
}

.aside {
  background-color: white;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-premium);
  padding: 16px;
  height: calc(100vh - 120px);
  margin-top: 8px;
}

.sidebar-menu {
  border: none !important;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 12px;
  margin-bottom: 4px;
  height: 48px;
  line-height: 48px;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--primary-light) !important;
  color: var(--primary-color) !important;
  font-weight: 600;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #f8fafc !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 100px;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.03);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

.main {
  padding: 0 16px;
  overflow-y: auto;
}
</style>

