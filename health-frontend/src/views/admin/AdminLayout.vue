<template>
  <el-container class="admin-layout">
    <el-aside width="260px" class="admin-sidebar" v-if="isAdmin">
      <div class="sidebar-logo">
        <el-icon :size="28" color="var(--primary-color)"><TrendCharts /></el-icon>
        <span>健康管理后台</span>
      </div>
      
      <el-menu
        :default-active="activePath"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>全站用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/content">
          <el-icon><MessageBox /></el-icon>
          <span>社区帖子审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/training">
          <el-icon><Bicycle /></el-icon>
          <span>公有训练库维护</span>
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
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
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
             <el-result icon="error" title="访问受限" sub-title="您当前账号权限不足。">
               <template #extra>
                 <el-button type="primary" @click="router.push('/app')">回前台</el-button>
               </template>
             </el-result>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TrendCharts, User, MessageBox, Bicycle } from '@element-plus/icons-vue'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()
const activePath = computed(() => route.path)
const nickname = ref('')
const isAdmin = ref(false)

const currentTitle = computed(() => {
  if (route.path.includes('users')) return '用户管理'
  if (route.path.includes('content')) return '社区审核'
  if (route.path.includes('training')) return '训练资源库'
  return '概览'
})

const handleCommand = (cmd: string) => {
  if (cmd === 'logout') {
    request.post('/auth/logout').then(() => {
      localStorage.removeItem('token')
      router.push('/login')
    })
  }
}

onMounted(async () => {
  try {
    const res: any = await request.get('/auth/info')
    if (res.data.role !== 'ADMIN') {
      isAdmin.value = false
      router.push('/app')
      return
    }
    nickname.value = res.data.nickname || res.data.username
    isAdmin.value = true
  } catch (e) {
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: var(--bg-main);
  background-image: radial-gradient(at 0% 0%, rgba(74, 222, 128, 0.05) 0px, transparent 50%),
                    radial-gradient(at 100% 100%, rgba(59, 130, 246, 0.05) 0px, transparent 50%);
}

.admin-sidebar {
  background: white;
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
  background-color: rgba(255, 255, 255, 0.8);
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
  border-radius: 100px;
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
