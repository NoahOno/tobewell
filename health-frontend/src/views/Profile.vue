<template>
  <div class="profile-experience">
    <div class="profile-header-area">
      <h1>账户设置 ⚙️</h1>
      <p>管理您的个人档案和数据概览。</p>
    </div>

    <!-- Stats Dashboard -->
    <div class="stats-dashboard grid-premium">
      <div class="stat-card premium-card">
        <div class="stat-value">{{ stats.postCount }}</div>
        <div class="stat-label">我的发布</div>
      </div>
      <div class="stat-card premium-card">
        <div class="stat-value">{{ stats.collectionCount }}</div>
        <div class="stat-label">我的收藏</div>
      </div>
      <div class="stat-card premium-card">
        <div class="stat-value">{{ stats.followingCount }}</div>
        <div class="stat-label">正在关注</div>
      </div>
      <div class="stat-card premium-card">
        <div class="stat-value">{{ stats.followersCount }}</div>
        <div class="stat-label">我的粉丝</div>
      </div>
    </div>

    <div class="premium-card settings-card">
      <div class="settings-header">
        <el-icon :size="24" color="#3B82F6"><User /></el-icon>
        <h3>基本资料</h3>
      </div>
      
      <el-form :model="form" label-position="top" class="form-premium" style="max-width: 500px">
        <el-form-item label="登录账号">
          <el-input v-model="form.username" disabled class="disabled-premium" />
          <div class="premium-tip">用户名不可更改，已与您的健康数据绑定。</div>
        </el-form-item>
        
        <el-form-item label="公开昵称">
          <el-input v-model="form.nickname" placeholder="设置一个能代表您的昵称" />
        </el-form-item>
        
        <el-form-item label="更新密码">
          <el-input v-model="newPassword" type="password" placeholder="如需变更，请输入新密码" show-password />
        </el-form-item>
        
        <el-form-item label="个人头像">
          <div class="avatar-manager">
            <el-avatar :size="80" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="premium-avatar" />
            <div class="avatar-rights">
              <el-button size="small" round disabled>更换头像</el-button>
              <p class="premium-tip">由于系统限制，当前仅支持默认能量头像。</p>
            </div>
          </div>
        </el-form-item>
        
        <div class="form-actions-premium">
          <el-button type="primary" round :loading="loading" @click="handleUpdate" class="save-btn">
            保存更改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import request from '../api/request'

const loading = ref(false)
const newPassword = ref('')
const form = reactive({
  username: '',
  nickname: ''
})

const stats = reactive({
  postCount: 0,
  collectionCount: 0,
  followingCount: 0,
  followersCount: 0
})

const fetchStats = async () => {
  try {
    const res: any = await request.get('/auth/stats')
    Object.assign(stats, res.data)
  } catch (err) {
    console.error(err)
  }
}

const fetchProfile = async () => {
  try {
    const res: any = await request.get('/auth/info')
    form.username = res.data.username
    form.nickname = res.data.nickname
  } catch (err) {
    console.error(err)
  }
}

const handleUpdate = async () => {
  if (!form.nickname) return ElMessage.warning('昵称不能为空')
  loading.value = true
  try {
    const updateData: any = { nickname: form.nickname }
    if (newPassword.value) updateData.password = newPassword.value
    
    const res: any = await request.post('/auth/update', updateData)
    if (res.code === 200) {
      ElMessage.success('配置更新成功')
      newPassword.value = ''
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProfile()
  fetchStats()
})
</script>

<style scoped>
.profile-experience {
  padding: 24px 0;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header-area {
  margin-bottom: 32px;
}

.profile-header-area h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: var(--text-main);
}

.profile-header-area p {
  color: var(--text-muted);
  margin: 0;
  font-size: 16px;
}

.stats-dashboard {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 24px;
  text-align: center;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.settings-card {
  padding: 40px;
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
}

.settings-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
}

.form-premium :deep(.el-form-item) {
  margin-bottom: 28px;
}

.form-premium :deep(.el-form-item__label) {
  font-weight: 700;
  color: var(--text-muted);
  padding-bottom: 8px;
}

.premium-tip {
  font-size: 13px;
  color: var(--text-light);
  margin-top: 8px;
  line-height: 1.4;
}

.disabled-premium :deep(.el-input__wrapper) {
  background-color: #F8FAFC !important;
  box-shadow: none !important;
}

.avatar-manager {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--bg-main);
  padding: 20px;
  border-radius: var(--radius-inner);
}

.premium-avatar {
  border: 4px solid white;
  box-shadow: var(--shadow-sm);
}

.avatar-rights {
  flex: 1;
}

.form-actions-premium {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #F1F5F9;
}

.save-btn {
  padding: 12px 32px;
  font-weight: 700;
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3);
}

/* My Activities */
.my-activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.my-activity-item {
  padding: 16px 20px;
  background: #F8FAFC;
  border-radius: 14px;
  border: 1px solid #F1F5F9;
  transition: box-shadow 0.18s;
}
.my-activity-item:hover { box-shadow: 0 4px 16px rgba(59,130,246,0.08); }
.my-act-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.my-act-title {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
  flex: 1;
}
.my-act-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #94A3B8;
  margin-bottom: 10px;
}
.my-act-progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748B;
}
</style>
