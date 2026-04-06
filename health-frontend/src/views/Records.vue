<template>
  <div class="records-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的动态</h1>
        <p class="page-subtitle">实时监控您的身体健康指标与运动表现</p>
      </div>
    </div>

    <div class="records-tabs">
      <el-tabs v-model="activeTab" class="bento-tabs" @tab-change="fetchTimeline">
        <el-tab-pane label="全部" name="all">
          <div class="timeline-container">
            <div class="timeline">
              <div v-for="activity in allActivities" :key="activity.id" class="timeline-item">
                <div class="timeline-dot" :class="getActivityIconClass(activity.type)"></div>
                <div class="timeline-content">
                  <div class="timeline-time">{{ formatTime(activity.timestamp) }}</div>
                  <div class="timeline-card">
                    <div class="timeline-header">
                      <div class="timeline-icon">{{ getActivityIcon(activity.type) }}</div>
                      <div class="timeline-title">{{ getActivityTitle(activity) }}</div>
                    </div>
                    <div class="timeline-body">
                      {{ getActivityContent(activity) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="allActivities.length === 0 && !loading" description="暂无动态记录" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="社区" name="community">
          <div class="timeline-container">
            <div class="timeline">
              <div v-for="activity in communityActivities" :key="activity.id" class="timeline-item">
                <div class="timeline-dot" :class="getActivityIconClass(activity.type)"></div>
                <div class="timeline-content">
                  <div class="timeline-time">{{ formatTime(activity.timestamp) }}</div>
                  <div class="timeline-card">
                    <div class="timeline-header">
                      <div class="timeline-icon">{{ getActivityIcon(activity.type) }}</div>
                      <div class="timeline-title">{{ getActivityTitle(activity) }}</div>
                    </div>
                    <div class="timeline-body">
                      {{ getActivityContent(activity) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="communityActivities.length === 0 && !loading" description="暂无社区动态" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="训练" name="training">
          <div class="timeline-container">
            <div class="timeline">
              <div v-for="activity in trainingActivities" :key="activity.id" class="timeline-item">
                <div class="timeline-dot training-icon"></div>
                <div class="timeline-content">
                  <div class="timeline-time">{{ formatTime(activity.timestamp) }}</div>
                  <div class="timeline-card">
                    <div class="timeline-header">
                      <div class="timeline-icon">🏃</div>
                      <div class="timeline-title">{{ activity.title || '完成训练' }}</div>
                    </div>
                    <div class="timeline-body">
                      {{ getActivityContent(activity) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="trainingActivities.length === 0 && !loading" description="暂无训练记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('all')
const loading = ref(false)
const timelineData = ref<any[]>([])

// Computed properties for different tabs
const allActivities = computed(() => timelineData.value)
const communityActivities = computed(() => 
  timelineData.value.filter(a => a.type === 'activity' || a.type === 'post')
)
const trainingActivities = computed(() => 
  timelineData.value.filter(a => a.type === 'training')
)

const formatTime = (time: any) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { 
    month: 'short', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'activity': return '🎯'
    case 'post': return '📝'
    case 'training': return '🏃'
    default: return '📅'
  }
}

const getActivityIconClass = (type: string) => {
  if (type === 'activity') return 'activity-icon'
  if (type === 'post') return 'post-icon'
  if (type === 'training') return 'training-icon'
  return 'data-icon'
}

const getActivityTitle = (activity: any) => {
  if (activity.type === 'activity') {
    const typeMap: Record<number, string> = {
      1: '打卡活动',
      2: '挑战活动',
      3: '话题活动'
    }
    const typeDesc = typeMap[activity.activityType] || '活动'
    return `${typeDesc}：${activity.title}`
  }
  if (activity.type === 'post') {
    return activity.title || '发布帖子'
  }
  if (activity.type === 'training') {
    return activity.title || '完成训练'
  }
  return '动态'
}

const getActivityContent = (activity: any) => {
  if (activity.type === 'activity') {
    const statusMap: Record<string, string> = {
      'APPLIED': '已报名参与',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消'
    }
    const status = statusMap[activity.status] || '参与中'
    return `${status}`
  }
  if (activity.type === 'post') {
    const content = activity.content || ''
    return content.length > 100 ? content.substring(0, 100) + '...' : content
  }
  if (activity.type === 'training') {
    return `完成了 ${activity.duration} 分钟的训练${activity.feeling ? ` - 感受：${activity.feeling}` : ''}`
  }
  return ''
}

const fetchTimeline = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/timeline/my', { 
      params: { type: activeTab.value } 
    })
    timelineData.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch timeline:', e)
    ElMessage.error('获取动态失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTimeline)
</script>

<style scoped>
.records-page {
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

.bento-tabs :deep(.el-tabs__header) { margin-bottom: 16px; border: none; }
.bento-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 700; color: #64748b; }
.bento-tabs :deep(.el-tabs__item.is-active) { color: #6366f1; }
.bento-tabs :deep(.el-tabs__active-bar) { background-color: #6366f1; height: 3px; border-radius: 3px; }

.timeline-container { padding: 10px 0; }
.timeline { position: relative; padding-left: 32px; }
.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 10px;
  bottom: 0;
  width: 2px;
  background: #f1f5f9;
}

.timeline-item { position: relative; padding-bottom: 40px; }
.timeline-dot {
  position: absolute;
  left: -30px;
  top: 6px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 4px solid white;
  box-shadow: 0 0 0 2px currentColor;
  z-index: 2;
}

.training-icon { color: #6366f1; background: #6366f1; }
.post-icon { color: #10b981; background: #10b981; }
.activity-icon { color: #f59e0b; background: #f59e0b; }
.data-icon { color: #8b5cf6; background: #8b5cf6; }

.timeline-time { font-size: 12px; color: #94a3b8; margin-bottom: 12px; font-weight: 600; }
.timeline-card {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #f1f5f9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.timeline-card:hover {
  transform: translateX(6px);
  box-shadow: 0 8px 24px -12px rgba(0,0,0,0.08);
  border-color: #e2e8f0;
}

.timeline-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.timeline-icon { font-size: 20px; }
.timeline-title { font-size: 16px; font-weight: 800; color: #1e293b; }
.timeline-body { font-size: 14px; color: #64748b; line-height: 1.6; }
</style>
