<template>
  <el-container class="admin-layout">
    <el-aside v-if="isAdmin" width="260px" class="admin-sidebar">
      <div class="sidebar-logo">
        <el-icon :size="28" color="var(--primary-color)"><TrendCharts /></el-icon>
        <span>管理后台</span>
      </div>

      <el-menu :default-active="activePath" router class="admin-menu">
        <el-menu-item index="/admin/dashboard">
          <el-icon><TrendCharts /></el-icon>
          <span>看板</span>
        </el-menu-item>

        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>全站用户管理</span>
        </el-menu-item>

        <el-sub-menu index="/admin/community">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>社区管理</span>
          </template>
          <el-menu-item index="/admin/community/posts">帖子管理</el-menu-item>
          <el-menu-item index="/admin/community/activities">活动管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/admin/content-library">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>内容库管理</span>
          </template>
          <el-menu-item index="/admin/content-library/actions">动作管理</el-menu-item>
          <el-menu-item index="/admin/content-library/courses">课程管理</el-menu-item>
          <el-menu-item index="/admin/content-library/plans">计划管理</el-menu-item>
          <el-menu-item index="/admin/content-library/audit">共享审核</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/health-services">
          <el-icon><Monitor /></el-icon>
          <span>健康服务管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理中心</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="admin-user-info">
              <el-avatar :size="32" :src="adminAvatar || defaultAvatar" />
              <div class="admin-name">
                <span class="role-tag">系统管理员</span>
                <span class="name-text">{{ nickname }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出后台</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <div class="view-container">
          <router-view v-if="isAdmin" />
          <div v-else class="forbidden">
            <el-result icon="error" title="访问受限" sub-title="当前账号没有管理员权限。">
              <template #extra>
                <el-button type="primary" @click="router.push('/app')">返回前台</el-button>
              </template>
            </el-result>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Files, Monitor, TrendCharts, User } from '@element-plus/icons-vue'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()

const nickname = ref('')
const adminAvatar = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const isAdmin = ref(false)

const activePath = computed(() => route.path)

const currentTitle = computed(() => {
  if (route.path.includes('/admin/dashboard')) return '数据看板'
  if (route.path.includes('/admin/users')) return '用户管理'
  if (route.path.includes('/admin/community/posts')) return '社区管理 / 帖子管理'
  if (route.path.includes('/admin/community/activities')) return '社区管理 / 活动管理'
  if (route.path.includes('/admin/content-library/actions')) return '内容库管理 / 动作管理'
  if (route.path.includes('/admin/content-library/courses')) return '内容库管理 / 课程管理'
  if (route.path.includes('/admin/content-library/plans')) return '内容库管理 / 计划管理'
  if (route.path.includes('/admin/content-library/audit')) return '内容库管理 / 共享审核'
  if (route.path.includes('/admin/health-services')) return '健康服务管理'
  return '管理后台'
})

const handleCommand = async (command: string) => {
  if (command !== 'logout') return
  await request.post('/auth/logout')
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(async () => {
  try {
    const res: any = await request.get('/auth/info')
    if (res.data?.role !== 'ADMIN') {
      isAdmin.value = false
      router.push('/app')
      return
    }
    nickname.value = res.data.nickname || res.data.username || 'Administrator'
    adminAvatar.value = res.data.avatar || ''
    isAdmin.value = true
  } catch (error) {
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: var(--bg-main);
  background-image:
    radial-gradient(at 0% 0%, rgba(74, 222, 128, 0.05) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(59, 130, 246, 0.05) 0px, transparent 50%);
}

.admin-sidebar {
  background: #fff;
  border-right: 1px solid rgba(226, 232, 240, 0.8);
  padding: 24px;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 40px;
  color: var(--text-main);
}

.admin-menu {
  border: none !important;
}

.admin-menu :deep(.el-menu-item) {
  border-radius: 12px;
  margin-bottom: 8px;
  height: 54px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: #f0fdf4 !important;
  color: #16a34a !important;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.1);
}

.admin-header {
  height: 72px;
  background-color: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
}

.admin-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 16px;
  border-radius: 999px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s;
}

.admin-user-info:hover {
  background: #f1f5f9;
}

.admin-name {
  display: flex;
  flex-direction: column;
}

.role-tag {
  font-size: 10px;
  font-weight: 700;
  color: #16a34a;
  text-transform: uppercase;
}

.name-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

.admin-main {
  padding: 40px;
  height: calc(100vh - 72px);
  overflow-y: auto;
}

.view-container {
  max-width: 1400px;
  margin: 0 auto;
}

.forbidden {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}
</style>
