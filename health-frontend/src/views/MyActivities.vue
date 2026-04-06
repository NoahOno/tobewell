<template>
  <div class="my-activities-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的活动</h1>
        <p class="page-subtitle">实时监控您的身体健康指标与运动表现</p>
      </div>
    </div>

    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="4" animated />
    </div>
    <div v-else-if="myActivities.length === 0" class="empty-area">
      <el-empty description="还未参与任何活动，去活动中心看看吧！" :image-size="100">
        <el-button type="primary" round @click="goToActivityCenter">去活动中心</el-button>
      </el-empty>
    </div>
    <div v-else class="activities-list">
      <div
        v-for="act in myActivities"
        :key="act.participationId"
        class="activity-item premium-card"
        @click="viewActivityDetail(act.activityId)"
      >
        <div class="act-header">
          <div class="act-title">
            {{ act.title }}
          </div>
          <div class="act-type-badge" :class="`type-${act.activityType}`">
            {{ activityTypeLabel(act.activityType) }}
          </div>
        </div>
        
        <div class="act-meta">
          <span>📅 {{ formatDateRange(act.startTime, act.endTime) }}</span>
          <span>⏱ {{ goalSummary(act) }}</span>
        </div>
        
        <div class="act-stat-mode" v-if="act.activityType === 1 || act.activityType === 2 || act.activityType === 3">
          <el-tag size="small" effect="plain" type="info">
            {{ getStatModeLabel(act) }}
          </el-tag>
        </div>

        <div class="act-progress">
          <div class="act-progress-header">
            <span class="progress-label">完成进度</span>
            <span
              v-if="act.activityType === 3 && act.topicStatMode === 'SHARED'"
              class="progress-value"
              :style="{ color: act.participationStatus === 'COMPLETED' ? '#10B981' : '#3B82F6' }"
            >
              {{ act.completedTasks >= 1 ? '已分享' : '未分享' }}
            </span>
            <span
              v-else
              class="progress-value"
              :style="{ color: act.participationStatus === 'COMPLETED' ? '#10B981' : '#3B82F6' }"
            >
              {{ act.completedTasks }}/{{ act.totalTasks }} {{ getStatUnit(act) }}
            </span>
          </div>
          <el-progress
            :percentage="act.totalTasks > 0 ? Math.round(act.completedTasks / act.totalTasks * 100) : 0"
            :color="act.participationStatus === 'COMPLETED' ? '#10B981' : '#3B82F6'"
            :stroke-width="8"
          />
        </div>

        <div class="act-footer">
          <div class="act-footer-left">
            <el-tag :type="act.participationStatus === 'COMPLETED' ? 'success' : 'primary'" size="small" effect="plain">
              {{ act.participationStatus === 'COMPLETED' ? '🎉 已完成' : '✅ 进行中' }}
            </el-tag>
            <span class="apply-time">参与时间: {{ formatApplyTime(act.applyTime) }}</span>
          </div>
          <div class="act-footer-right">
            <el-button type="primary" size="small" text @click="viewActivityDetail(act.activityId, $event)">查看详情</el-button>
            <el-button v-if="act.participationStatus !== 'COMPLETED'" type="danger" size="small" text @click="quitActivity(act.activityId, $event)">退出活动</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const myActivities = ref<any[]>([])

const activityTypeLabel = (type: number) => {
  if (type === 1) return '打卡类'
  if (type === 2) return '挑战类'
  if (type === 3) return '话题类'
  return '活动'
}

const getStatUnit = (act: any) => {
  if (act.activityType === 1) return act.countMode === 'COUNT' ? '次' : '天'
  if (act.activityType === 2) {
    if (act.countMode === 'COUNT') return '次'
    return '天'
  }
  if (act.activityType === 3) {
    if (act.topicStatMode === 'SHARED') return ''
    if (act.topicStatMode === 'DAYS') return '天'
    return '次'
  }
  return '天'
}

const goalSummary = (act: any) => {
  if (act.activityType === 3 && act.topicStatMode === 'SHARED') {
    return '目标：完成 1 次话题分享'
  }
  const u = getStatUnit(act)
  return u ? `目标：${act.requiredDays} ${u}` : `目标：${act.requiredDays}`
}

const getStatModeLabel = (act: any) => {
  if (act.activityType === 1) {
    return act.countMode === 'COUNT' ? '📊 按打卡次数' : '📊 按打卡天数'
  }
  if (act.activityType === 2) {
    return act.countMode === 'COUNT' ? '📊 按次数统计' : '📊 按天数统计'
  }
  if (act.activityType === 3) {
    if (act.topicStatMode === 'SHARED') return '📊 是否分享'
    if (act.topicStatMode === 'DAYS') return '📊 按分享天数'
    return '📊 按分享次数'
  }
  return ''
}

const formatDateRange = (start: any, end: any) => {
  if (!start) return ''
  const fmt = (d: Date) => `${d.getMonth() + 1}/${d.getDate()}`
  const s = new Date(start)
  if (!end) return fmt(s)
  const e = new Date(end)
  return `${fmt(s)} ~ ${fmt(e)}`
}

const formatApplyTime = (time: any) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const fetchMyActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/activity/my')
    myActivities.value = res.data || []
  } catch (e) {
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

const goToActivityCenter = () => {
  router.push('/app/community?tab=activityCenter')
}

const viewActivityDetail = (activityId: number, e?: MouseEvent) => {
  if (e) {
    e.stopPropagation()
  }
  router.push('/app/community?tab=activityCenter')
  setTimeout(() => {
    window.dispatchEvent(new CustomEvent('openActivityDetail', { detail: { activityId } }))
  }, 100)
}

const quitActivity = async (activityId: number, e: MouseEvent) => {
  e.stopPropagation()
  try {
    await ElMessageBox.confirm(
      '退出活动将删除您所有参与记录和进度数据，此操作无法恢复，确定要退出吗？',
      '确认退出',
      { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning' }
    )
    await request.post(`/activity/${activityId}/quit`)
    ElMessage.success('成功退出活动')
    await fetchMyActivities()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('退出失败')
  }
}

onMounted(fetchMyActivities)
</script>

<style scoped>
.my-activities-page {
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

.activities-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.activity-item {
  padding: 24px;
  background: white;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.activity-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0,0,0,0.08);
  border-color: #e2e8f0;
}

.act-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.act-title { font-size: 18px; font-weight: 800; color: #1e293b; }
.act-type-badge { font-size: 11px; font-weight: 700; padding: 4px 12px; border-radius: 12px; }
.type-1 { background: #f0fdf4; color: #10b981; }
.type-2 { background: #fef2f2; color: #ef4444; }
.type-3 { background: #fffbeb; color: #f59e0b; }

.act-meta { display: flex; gap: 20px; font-size: 13px; color: #64748b; margin-bottom: 16px; }

.act-progress { margin: 20px 0; }
.act-progress-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
.progress-value { font-weight: 800; }

.act-footer { display: flex; justify-content: space-between; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.act-footer-left { display: flex; align-items: center; gap: 12px; }
.apply-time { font-size: 12px; color: #94a3b8; }
</style>
