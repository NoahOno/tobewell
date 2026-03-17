<template>
  <div class="my-training-page">
    <div class="page-header">
      <div class="header-left">
        <h1>训练中心</h1>
        <p>在这里管理您的订阅计划并追踪每日训练进度</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" round @click="router.push('/app/explore')">
          <el-icon><Plus /></el-icon> 订阅新计划
        </el-button>
      </div>
    </div>

    <div class="today-section" v-if="activePlan">
      <h2 class="section-title">今日训练 <el-tag size="small" type="danger" round class="today-tag">训练中</el-tag></h2>
      <div class="today-card premium-card">
        <div class="today-header">
          <div class="th-left">
            <h3>{{ activePlan.title }}</h3>
            <span class="th-subtitle">{{ activePlan.category || '全方位训练' }}</span>
          </div>
          <div class="th-right">
            <el-button type="primary" size="large" round class="start-btn">
              <el-icon><VideoPlay /></el-icon> 继续训练
            </el-button>
          </div>
        </div>

        <div class="today-actions-list">
          <div v-for="(act, idx) in parseActions(activePlan.actions)" :key="idx" class="act-item">
            <div class="act-status">
              <div class="checkbox">
                <!-- Status tracking would need another table, for now just UI -->
              </div>
            </div>
            <div class="act-info">
              <div class="act-name">{{ idx + 1 }}. {{ act.name }}</div>
              <div class="act-sets">{{ act.sets }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="today-section">
      <el-empty description="当前暂无正在执行的训练计划">
        <el-button type="primary" round @click="router.push('/app/explore')">去开启新计划</el-button>
      </el-empty>
    </div>

    <div class="layout-grid">
      <!-- Left Column -->
      <div class="grid-left">
        <!-- 2. Weekly Calendar -->
        <h2 class="section-title">周训练日历</h2>
        <div class="week-calendar premium-card">
          <div class="wc-header">
            <span>本周进度</span>
            <el-tag type="success" effect="plain" round size="small">3/4 天</el-tag>
          </div>
          <div class="wc-days">
            <div 
              v-for="d in weekDays" 
              :key="d.date" 
              class="wc-day" 
              :class="{ 'is-today': d.isToday, 'is-rest': d.type === 'rest', 'is-done': d.status === 'done' }"
            >
              <div class="wc-date-label">{{ d.dayName }}</div>
              <div class="wc-circle">
                <el-icon v-if="d.status === 'done'"><Check /></el-icon>
                <el-icon v-else-if="d.type === 'rest'"><Mug /></el-icon>
                <span v-else>{{ d.dateNum }}</span>
              </div>
              <div class="wc-type-label">{{ d.label }}</div>
            </div>
          </div>
        </div>

        <!-- 3. My Subscriptions Center -->
        <div class="plans-header">
          <h2 class="section-title">我的计划库</h2>
          <el-radio-group v-model="planTab" size="default" class="plan-tabs">
            <el-radio-button label="current">正在执行</el-radio-button>
            <el-radio-button label="history">历史存档</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="my-plans-list" v-loading="loading">
          <template v-if="planTab === 'current'">
            <div v-for="plan in currentPlans" :key="plan.id" class="sub-card premium-card" :class="{ 'is-active': plan.status === 'ACTIVE' }">
              <div class="sub-card-banner" :class="getCardTheme(plan.category)">
                <div class="sub-badge" v-if="plan.status === 'ACTIVE'">执行中</div>
                <div class="sub-badge subscribed" v-else>已订阅</div>
              </div>
              <div class="sub-card-content">
                <div class="sub-info">
                  <h3 class="sub-title">{{ plan.title }}</h3>
                  <div class="sub-meta">
                    <span><el-icon><Calendar /></el-icon> {{ plan.duration || '4周' }}</span>
                    <span v-if="plan.category"><el-icon><PriceTag /></el-icon> {{ plan.category.split(',')[0] }}</span>
                  </div>
                </div>
                
                <div class="sub-actions">
                  <template v-if="plan.status === 'ACTIVE'">
                     <div class="mini-progress-box">
                        <div class="mp-text">执行进度 12%</div>
                        <el-progress :percentage="12" :show-text="false" stroke-width="6" color="var(--el-color-success)" />
                     </div>
                     <el-button type="danger" plain round size="small" @click="handleEndPlan(plan)">结束并存档</el-button>
                  </template>
                  <template v-else>
                    <el-button type="primary" round @click="handleStartPlan(plan)">立即开始执行</el-button>
                    <el-button link type="info" @click="handleUnsubscribe(plan)">退订</el-button>
                  </template>
                </div>
              </div>
            </div>
            <el-empty v-if="currentPlans.length === 0" description="您还没有任何订阅计划，去发现页看看吧！">
              <el-button type="primary" round @click="router.push('/app/explore')">去探索</el-button>
            </el-empty>
          </template>
          
          <template v-else>
            <div v-for="plan in historyPlans" :key="plan.id" class="history-item premium-card">
              <div class="hi-left">
                <div class="hi-icon"><el-icon><Memo /></el-icon></div>
                <div class="hi-info">
                  <h4>{{ plan.title }}</h4>
                  <p>存档于: {{ plan.endDate || '2026-03-09' }}</p>
                </div>
              </div>
              <div class="hi-right">
                <el-button text type="primary" @click="handleStartPlan(plan)">重新执行</el-button>
                <el-button text type="danger" @click="handleUnsubscribe(plan)">删除记录</el-button>
              </div>
            </div>
            <el-empty v-if="historyPlans.length === 0" description="暂无历史计划记录" />
          </template>
        </div>
      </div>

      <!-- Right Column -->
      <div class="grid-right">
        <!-- 4. Training Records -->
        <h2 class="section-title">训练数据概览</h2>
        <div class="stats-grid">
          <div class="stat-box premium-card">
            <div class="sb-label">本周完成率</div>
            <div class="sb-value highlight">75<span class="sb-unit">%</span></div>
          </div>
          <div class="stat-box premium-card">
            <div class="sb-label">总训练次数</div>
            <div class="sb-value">42<span class="sb-unit">次</span></div>
          </div>
          <div class="stat-box premium-card">
            <div class="sb-label">总训练时长</div>
            <div class="sb-value">1280<span class="sb-unit">min</span></div>
          </div>
          <div class="stat-box premium-card">
            <div class="sb-label">连续打卡</div>
            <div class="sb-value">5<span class="sb-unit">天</span></div>
          </div>
        </div>

        <div class="chart-card premium-card">
          <div class="chart-header">
            <span>最近7天训练时长</span>
          </div>
          <div class="chart-placeholder">
            <div class="bar-chart">
              <div class="bar" style="height: 40%"></div>
              <div class="bar" style="height: 60%"></div>
              <div class="bar" style="height: 0%"></div>
              <div class="bar" style="height: 80%"></div>
              <div class="bar" style="height: 45%"></div>
              <div class="bar active" style="height: 70%"></div>
              <div class="bar" style="height: 0%"></div>
            </div>
            <div class="bar-labels">
              <span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span class="active">六</span><span>日</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Replace Action Dialog -->
    <el-dialog v-model="replaceDialogVisible" title="替换动作" width="900px" top="5vh" class="replace-dialog" destroy-on-close>
      <!-- Embed the Exercise Library Component in select Mode -->
      <ExerciseLibrary :select-mode="true" @select="handleExSelect" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Compass, VideoPlay, Check, Refresh, Mug, Plus, PriceTag, Memo } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ExerciseLibrary from './ExerciseLibrary.vue'
import request from '../api/request'

const router = useRouter()

// Mock logic for Today's Actions
const todayActions = ref([
  { id: 1, name: '深蹲 (Squat)', sets: '3组 × 12次', completed: true },
  { id: 2, name: '平板支撑 (Plank)', sets: '3组 × 30秒', completed: false },
  { id: 3, name: '开合跳 (Jumping Jacks)', sets: '3组 × 45秒', completed: false },
])

const toggleComplete = (act: any) => {
  act.completed = !act.completed
  if(todayActions.value.every(a => a.completed)) {
    ElMessage.success('太棒了！您已完成今日所有训练！')
  }
}

// Replace Logic
const replaceDialogVisible = ref(false)
const replacingIndex = ref(-1)

const openReplaceDialog = (act: any, idx: number) => {
  replacingIndex.value = idx
  replaceDialogVisible.value = true
}

const handleExSelect = (ex: any) => {
  if(replacingIndex.value !== -1) {
    todayActions.value[replacingIndex.value].name = ex.name
    todayActions.value[replacingIndex.value].sets = ex.recommendedSets || '3组 × 12次'
    ElMessage.success(`成功替换为: ${ex.name}`)
  }
  replaceDialogVisible.value = false
}

// Mock Week Calendar
const weekDays = ref([
  { date: '2026-03-02', dayName: 'Mon', dateNum: 2, label: '上肢', type: 'train', status: 'done' },
  { date: '2026-03-03', dayName: 'Tue', dateNum: 3, label: '有氧', type: 'train', status: 'done' },
  { date: '2026-03-04', dayName: 'Wed', dateNum: 4, label: '休息', type: 'rest', status: 'pending' },
  { date: '2026-03-05', dayName: 'Thu', dateNum: 5, label: '下肢', type: 'train', status: 'done' },
  { date: '2026-03-06', dayName: 'Fri', dateNum: 6, label: '有氧+核心', type: 'train', status: 'pending', isToday: true },
  { date: '2026-03-07', dayName: 'Sat', dateNum: 7, label: '休息', type: 'rest', status: 'pending' },
  { date: '2026-03-08', dayName: 'Sun', dateNum: 8, label: '拉伸', type: 'train', status: 'pending' },
])

const planTab = ref('current')

const myPlans = ref<any[]>([])
const activePlan = ref<any>(null)
const loading = ref(false)

const fetchMyPlans = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/training/list')
    myPlans.value = res.data || []
  } catch(e) {} finally {
    loading.value = false
  }
}

const fetchCurrentPlan = async () => {
  try {
    const res: any = await request.get('/training/current')
    activePlan.value = res.data
  } catch(e) {}
}

const parseActions = (json?: string) => {
  if (!json) return []
  try {
    return JSON.parse(json)
  } catch(e) { return [] }
}

const handleStartPlan = async (plan: any) => {
  try {
    await ElMessageBox.confirm(`确定要开始执行“${plan.title}”并将其设为当前计划吗？`, '开始训练', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    // In a real app, this would call an API like /training/activate/{id}
    // For now we use save or a custom endpoint if implemented
    await request.post('/training/save', { ...plan, status: 'ACTIVE', startDate: new Date().toISOString().split('T')[0] })
    ElMessage.success('计划已激活！加油！')
    await fetchMyPlans()
    await fetchCurrentPlan()
  } catch(e) {}
}

const handleEndPlan = async (plan: any) => {
  try {
    await ElMessageBox.confirm(`确定要结束并存档“${plan.title}”吗？`, '结束训练', {
      confirmButtonText: '结束',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post('/training/save', { ...plan, status: 'ARCHIVED', endDate: new Date().toISOString().split('T')[0] })
    ElMessage.success('计划已成功存档')
    await fetchMyPlans()
    await fetchCurrentPlan()
  } catch(e) {}
}

const handleUnsubscribe = async (plan: any) => {
   try {
    await ElMessageBox.confirm(`确定要移除“${plan.title}”吗？`, '移除确认', { type: 'warning' })
    await request.delete(`/training/${plan.id}`)
    ElMessage.success('已移除')
    await fetchMyPlans()
    await fetchCurrentPlan()
  } catch(e) {}
}

const getCardTheme = (category?: string) => {
  if (!category) return 'theme-1';
  if (category.includes('减脂')) return 'theme-2';
  if (category.includes('增肌')) return 'theme-3';
  if (category.includes('跑步')) return 'theme-4';
  return 'theme-1';
}

onMounted(() => {
  fetchMyPlans()
  fetchCurrentPlan()
})

const currentPlans = computed(() => myPlans.value.filter(p => p.status === 'ACTIVE' || (p.status === 'PLANNING' && p.isSubscribed !== false)))
const historyPlans = computed(() => myPlans.value.filter(p => p.status === 'ARCHIVED' && p.isSubscribed !== false))

</script>

<style scoped>
.my-training-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}
.header-left h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 900;
  color: #1e293b;
}
.header-left p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Today's Training */
.today-section {
  margin-bottom: 32px;
}
.today-tag {
  font-weight: 800;
  font-size: 14px;
}

.today-card {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
  padding: 32px;
  border-radius: 16px;
}
.today-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.th-left h3 {
  margin: 0 0 8px;
  font-size: 24px;
}
.th-subtitle {
  color: #94a3b8;
  font-size: 14px;
}
.start-btn {
  font-size: 16px;
  font-weight: 600;
  padding: 12px 24px;
  background: #10b981;
  border: none;
}
.start-btn:hover { background: #059669; }

.today-actions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.act-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255,255,255,0.05);
  padding: 16px 20px;
  border-radius: 12px;
  transition: all 0.2s;
}
.act-item:hover { background: rgba(255,255,255,0.1); }
.act-item.is-completed { opacity: 0.6; }

.act-status { cursor: pointer; }
.checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #64748b;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.2s;
}
.checkbox.checked {
  background: #10b981;
  border-color: #10b981;
}

.act-info { flex: 1; }
.act-name {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 4px;
}
.act-item.is-completed .act-name { text-decoration: line-through; }
.act-sets {
  font-size: 13px;
  color: #94a3b8;
}
.act-ops .el-button { color: #60a5fa; }

/* Grid Layout */
.layout-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 32px;
}

@media (max-width: 900px) {
  .layout-grid { grid-template-columns: 1fr; }
}

/* Weekly Calendar */
.wc-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}
.wc-days {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
.wc-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.wc-date-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}
.wc-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.2s;
}
.wc-type-label {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
}

.wc-day.is-rest .wc-circle { background: transparent; border: 1px dashed #cbd5e1; color: #94a3b8; }
.wc-day.is-done .wc-circle { background: #10b981; color: white; }
.wc-day.is-today .wc-circle { background: #3b82f6; color: white; box-shadow: 0 4px 12px rgba(59,130,246,0.3); }
.wc-day.is-today .wc-type-label { color: #3b82f6; font-weight: 700; }

/* My Plans */
.plans-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 32px 0 16px;
}
/* My Subscriptions Redesign */
.plan-tabs :deep(.el-radio-button__inner) {
  border-radius: 20px;
  margin: 0 4px;
  border: 1px solid #E2E8F0 !important;
  box-shadow: none !important;
}

.my-plans-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sub-card {
  display: flex;
  overflow: hidden;
  padding: 0;
  transition: all 0.3s;
}

.sub-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.sub-card-banner {
  width: 8px;
  position: relative;
}

.theme-1 { background: #3B82F6; }
.theme-2 { background: #F59E0B; }
.theme-3 { background: #EF4444; }
.theme-4 { background: #10B981; }

.sub-card-content {
  flex: 1;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-badge {
  position: absolute;
  top: 12px;
  left: 20px;
  background: #10B981;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  box-shadow: 0 2px 4px rgba(16,185,129,0.3);
}
.sub-card-banner .sub-badge {
    position: static;
    display: none;
}

.sub-info .sub-title {
  font-size: 18px;
  font-weight: 800;
  color: #1E293B;
  margin: 0 0 8px;
}

.sub-meta {
  display: flex;
  gap: 16px;
  color: #64748B;
  font-size: 13px;
}
.sub-meta span { display: flex; align-items: center; gap: 4px; }

.sub-card-content {
    position: relative;
}
.sub-card-content .sub-badge {
    position: absolute;
    top: -12px;
    right: 24px;
    display: block;
}
.sub-badge.subscribed {
    background: #3B82F6;
    box-shadow: 0 2px 4px rgba(59,130,246,0.3);
}

.mini-progress-box {
  min-width: 140px;
  margin-right: 24px;
}
.mp-text {
  font-size: 11px;
  color: #64748B;
  font-weight: 700;
  margin-bottom: 6px;
  text-align: right;
}

.sub-actions {
  display: flex;
  align-items: center;
}

/* History Items */
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-left: 4px solid #94A3B8;
}
.hi-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.hi-icon {
  width: 40px;
  height: 40px;
  background: #F1F5F9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #64748B;
}
.hi-info h4 { margin: 0 0 4px; font-size: 16px; color: #334155; }
.hi-info p { margin: 0; font-size: 12px; color: #94A3B8; }

.history-item:hover { opacity: 1; transform: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

/* Custom Dialog */
.replace-dialog :deep(.el-dialog__body) {
  padding: 0;
}
</style>
