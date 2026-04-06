<template>
  <div class="collections">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的收藏</h1>
        <p class="page-subtitle">实时监控您的身体健康指标与运动表现</p>
      </div>
    </div>

    <div v-loading="loading" class="content-container">
      <el-tabs v-model="activeTab" class="collection-tabs" @tab-change="fetchCollections">
        <el-tab-pane label="社区帖子" name="POST">
          <div v-if="collections.length === 0 && !loading" class="empty-state">
            <el-empty description="还没有收藏帖子，去社区逛逛吧！" />
          </div>
          <div v-else class="post-collection-list">
            <div
              v-for="item in collections"
              :key="item.id"
              class="post-collection-card"
            >
              <div class="card-top">
                <div class="card-author">
                  <el-avatar :size="32" style="background:#6366f1">{{ (item.authorName || '#')[0] }}</el-avatar>
                  <span class="author-name">{{ item.authorName || '未知用户' }}</span>
                  <span class="collect-time">{{ formatTime(item.createTime) }} 收藏</span>
                </div>
                <el-button
                  link
                  type="danger"
                  size="small"
                  @click="handleUncollect(item)"
                >取消收藏</el-button>
              </div>
              <h3 class="card-title">{{ item.targetTitle || '未知帖子' }}</h3>
              <p v-if="item.content" class="card-excerpt">{{ item.content }}</p>
              
              <div v-if="item.images" class="card-thumbs">
                <img
                  v-for="(img, i) in item.images.split(',').slice(0, 3)"
                  :key="i"
                  :src="img.trim()"
                  class="card-thumb"
                />
              </div>
              <div class="card-stats">
                <span><el-icon><HotWater /></el-icon> {{ item.likeCount || 0 }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ item.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ChatDotRound, HotWater } from '@element-plus/icons-vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('POST')
const loading = ref(false)
const collections = ref<any[]>([])

const fetchCollections = async () => {
  loading.value = true
  collections.value = []
  try {
    const res: any = await request.get('/interaction/collections', { params: { type: activeTab.value } })
    collections.value = res.data || []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleUncollect = async (item: any) => {
  try {
    await request.delete('/interaction/collect', {
      params: { targetId: item.targetId, targetType: activeTab.value }
    })
    ElMessage.success('已取消收藏')
    fetchCollections()
  } catch (e) {}
}

const formatTime = (t: string) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(fetchCollections)
</script>

<style scoped>
.collections {
  padding: 32px;
  max-width: 1440px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 850;
  letter-spacing: -1px;
  margin: 0 0 4px 0;
  color: #1e293b;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
}

.collection-tabs :deep(.el-tabs__header) { margin-bottom: 16px; border: none; }
.collection-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 700; color: #64748b; }
.collection-tabs :deep(.el-tabs__item.is-active) { color: #6366f1; }
.collection-tabs :deep(.el-tabs__active-bar) { background-color: #6366f1; height: 3px; border-radius: 3px; }

.post-collection-list { display: flex; flex-direction: column; gap: 20px; padding: 8px 0; }
.post-collection-card {
  padding: 24px;
  border-radius: 20px;
  background: white;
  border: 1px solid #f1f5f9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.post-collection-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0,0,0,0.06);
  border-color: #e2e8f0;
}

.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.card-author { display: flex; align-items: center; gap: 12px; }
.author-name { font-weight: 700; font-size: 14px; color: #1e293b; }
.collect-time { font-size: 12px; color: #94a3b8; }
.card-title { font-size: 20px; font-weight: 850; color: #1e293b; margin: 0 0 10px; line-height: 1.4; }
.card-excerpt { font-size: 15px; color: #64748b; line-height: 1.6; margin: 0 0 16px; }

.card-thumbs { display: flex; gap: 12px; margin: 16px 0; }
.card-thumb { width: 120px; height: 80px; border-radius: 12px; object-fit: cover; border: 1px solid #f1f5f9; }
.card-stats { display: flex; gap: 24px; align-items: center; font-size: 13px; color: #94a3b8; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.card-stats span { display: flex; align-items: center; gap: 6px; }

.empty-state { padding: 60px 0; }
</style>
