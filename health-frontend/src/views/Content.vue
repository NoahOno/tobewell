<template>
  <div class="content-explorer">
    <el-row :gutter="64">
      <!-- Main Feed (X-Style) -->
      <el-col :span="16">
        <div class="feed-container">
          <!-- Quick Post Area -->
          <el-card shadow="never" class="post-creation-card">
            <div class="post-input-wrapper">
              <el-avatar :size="48" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <div class="input-right">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="2"
                  placeholder="有什么健康心得想分享？"
                  resize="none"
                  class="content-input"
                />
                <div class="post-actions">
                  <div class="action-icons">
                    <el-icon><Picture /></el-icon>
                    <el-icon><VideoCamera /></el-icon>
                    <el-icon><Location /></el-icon>
                  </div>
                  <el-button type="success" round @click="handleAdd" :loading="submitLoading">发布动态</el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Feed List -->
          <div v-loading="loading" class="feed-list">
            <el-card v-for="item in contentList" :key="item.id" shadow="hover" class="feed-item-card">
              <div class="feed-header">
                <el-avatar :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <div class="user-meta">
                  <span class="nickname">健康达人 #{{ item.id }} <el-icon color="#409EFF"><CircleCheckFilled /></el-icon></span>
                  <span class="post-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <div class="item-actions-top">
                   <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, item)">
                    <el-icon class="more-icon"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete" style="color: #F56C6C">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              
              <div class="feed-content">
                <h3 class="post-title">{{ item.title }}</h3>
                <p class="post-text">{{ item.content }}</p>
                <div v-if="item.category" class="post-tags">
                  <el-tag size="small" effect="plain" round type="success"># {{ item.category }}</el-tag>
                </div>
              </div>

              <div class="feed-footer">
                <div class="engagement-item"><el-icon><ChatDotRound /></el-icon> <span>12</span></div>
                <div class="engagement-item"><el-icon><Refresh /></el-icon> <span>5</span></div>
                <div class="engagement-item like-active"><el-icon><Star /></el-icon> <span>88</span></div>
                <div class="engagement-item"><el-icon><Share /></el-icon></div>
              </div>
            </el-card>
          </div>
        </div>
      </el-col>

      <!-- Sidebar (Reddit-Style) -->
      <el-col :span="8">
        <div class="sidebar-sticky">
          <!-- Hot Topics -->
          <el-card shadow="never" class="sidebar-card">
            <template #header>
              <div class="sidebar-header">
                <span class="sidebar-title">热门话题</span>
              </div>
            </template>
            <div class="trending-list">
              <div class="trending-item" v-for="(topic, index) in trendingTopics" :key="index">
                <div class="topic-rank">{{ index + 1 }}</div>
                <div class="topic-info">
                  <span class="topic-name"># {{ topic.name }}</span>
                  <span class="topic-count">{{ topic.count }}k 动态</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Recommended -->
          <el-card shadow="never" class="sidebar-card">
            <template #header>
              <div class="sidebar-header">
                <span class="sidebar-title">特别关注</span>
              </div>
            </template>
            <div class="follow-list">
              <div class="follow-item" v-for="user in recommendedUsers" :key="user.id">
                <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                <div class="follow-info">
                  <span class="follow-name">{{ user.nickname || user.username }}</span>
                  <span class="follow-bio">{{ user.role === 'ADMIN' ? '官方认证专家' : '资深健康达人' }}</span>
                </div>
                <el-button size="small" round type="dark" class="follow-btn">关注</el-button>
              </div>
            </div>
            <el-button link type="primary" class="show-more">查看更多</el-button>
          </el-card>

          <footer class="legal-footer">
            <span>隐私政策</span> · <span>服务条款</span> · <span>更多</span>
            <p>© 2026 HealthPlatform</p>
          </footer>
        </div>
      </el-col>
    </el-row>

    <!-- Post Dialog (Classic Mode) -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑动态' : '新的创想'"
      width="500px"
      append-to-body
      destroy-on-close
      class="custom-dialog"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="标题 (可选)">
          <el-input v-model="form.title" placeholder="给你的想法起个名字" />
        </el-form-item>
        <el-form-item label="话题分类">
          <el-select v-model="form.category" placeholder="选择一个话题" style="width: 100%">
            <el-option label="🏃 运动" value="运动" />
            <el-option label="🥗 饮食" value="饮食" />
            <el-option label="🧘 心态" value="心态" />
            <el-option label="😴 睡眠" value="睡眠" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="分享你的健康生活..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" round @click="submitForm" :loading="submitLoading">
          {{ isEdit ? '保存修改' : '立即发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Picture, VideoCamera, Location, MoreFilled, 
  ChatDotRound, Refresh, Star, Share, CircleCheckFilled 
} from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const contentList = ref<any[]>([])
const trendingTopics = ref<any[]>([])
const recommendedUsers = ref<any[]>([])

const form = reactive({
  id: undefined,
  title: '',
  category: '',
  content: ''
})

const fetchTrending = async () => {
  try {
    const res: any = await request.get('/content/trending')
    trendingTopics.value = res.data
  } catch (err) {
    console.error(err)
  }
}

const fetchRecommendations = async () => {
  try {
    const res: any = await request.get('/auth/recommendations')
    recommendedUsers.value = res.data
  } catch (err) {
    console.error(err)
  }
}

const fetchContent = async () => {
  loading.value = true
  try {
    const userRes: any = await request.get('/auth/info')
    const isAdmin = userRes.data.role === 'ADMIN'
    const endpoint = isAdmin ? '/content/all' : '/content/my'
    const res: any = await request.get(endpoint)
    contentList.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  if (!form.content) {
    isEdit.value = false
    form.id = undefined
    form.title = ''
    form.category = ''
    form.content = ''
    dialogVisible.value = true
  } else {
    form.title = '新动态'
    form.category = '日常'
    submitForm()
  }
}

const handleCommand = (cmd: string, row: any) => {
  if (cmd === 'edit') {
    isEdit.value = true
    Object.assign(form, row)
    dialogVisible.value = true
  } else if (cmd === 'delete') {
    handleDelete(row)
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要移除这条想法吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    roundButton: true
  }).then(async () => {
    await request.delete(`/content/${row.id}`)
    ElMessage.success('已移除')
    fetchContent()
    fetchTrending() // Refresh trends
  })
}

const submitForm = async () => {
  if (!form.content) return ElMessage.warning('说点什么吧...')
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.put('/content/update', form)
    } else {
      await request.post('/content/create', form)
    }
    ElMessage.success('发布成功')
    dialogVisible.value = false
    form.content = '' 
    fetchContent()
    fetchTrending() // Refresh trends
  } catch (err) {
    console.error(err)
  } finally {
    submitLoading.value = false
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchContent()
  fetchTrending()
  fetchRecommendations()
})
</script>

<style scoped>
.content-explorer {
  padding: 100px 0 120px; /* Increased top padding to clear 72px header */
  max-width: 1200px;
  margin: 0 auto;
}

/* Base Feed */
.feed-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl); /* 64px gaps between major feed blocks */
}

/* Post Creation - Clean Apple Style */
.post-creation-card {
  padding: 24px;
}

.post-input-wrapper {
  display: flex;
  gap: 20px;
}

.input-right {
  flex: 1;
}

.content-input :deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  font-size: 18px;
  font-weight: 500;
  padding: 8px 0;
  background: transparent;
  color: var(--text-main);
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #F1F5F9;
}

.action-icons {
  display: flex;
  gap: 20px;
  color: var(--text-muted);
  font-size: 20px;
  cursor: pointer;
}

.action-icons .el-icon:hover {
  color: var(--primary-color);
}

/* Feed Items - Grouped and Premium */
.feed-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md); /* 32px between posts */
}

.feed-item-card {
  padding: 24px;
}

.feed-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  position: relative;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-main);
}

.post-time {
  font-size: 13px;
  color: var(--text-muted);
}

.item-actions-top {
  margin-left: auto;
}

.feed-content {
  margin-left: 56px;
}

.post-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: var(--text-main);
}

.post-text {
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-main);
  opacity: 0.9;
}

.post-tags {
  margin-top: 16px;
}

.feed-footer {
  margin-left: 56px;
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
  max-width: 450px;
  color: var(--text-muted);
}

.engagement-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.engagement-item:hover { color: var(--primary-color); }
.like-active { color: #F87171; }

/* Sidebar - Modern Widgets */
.sidebar-sticky {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: var(--space-lg); /* Explicit flex gap for components */
}

.sidebar-card {
  padding: 24px;
}

.sidebar-header {
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
}

.trending-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #F1F5F9;
}

.topic-rank {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary-color);
  opacity: 0.5;
  width: 24px;
}

.topic-info {
  flex: 1;
}

.topic-name {
  display: block;
  font-weight: 700;
  font-size: 15px;
  color: var(--text-main);
  margin-bottom: 2px;
}

.topic-count {
  font-size: 12px;
  color: var(--text-muted);
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.follow-info {
  flex: 1;
}

.follow-name {
  display: block;
  font-weight: 700;
  font-size: 14px;
}

.follow-bio {
  font-size: 12px;
  color: var(--text-muted);
}

.follow-btn {
  background-color: var(--text-main) !important;
  color: white !important;
  font-weight: 600;
}

.legal-footer {
  margin-top: 32px;
  color: var(--text-light);
  font-size: 12px;
  line-height: 2;
}
</style>
