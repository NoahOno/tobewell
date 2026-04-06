<template>
  <div class="dashboard-root">
    <!-- 1. Header Section (Standardized) -->
    <div class="page-header reveal" v-reveal>
      <div class="header-content">
        <h1 class="page-title">我的数据</h1>
        <p class="page-subtitle">实时监控您的身体健康指标与运动表现</p>
      </div>
      <div class="header-right">
        <el-button type="primary" class="action-btn" round @click="recordDialogVisible = true">
          <el-icon><Plus /></el-icon> 记录指标
        </el-button>
        <el-button class="action-btn" plain round @click="workoutDialogVisible = true">
          <el-icon><Timer /></el-icon> 记录运动
        </el-button>
        <el-button circle class="refresh-btn" @click="fetchData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 2. Bento Grid Layout -->
    <div class="bento-grid">
      
      <!-- [1] Steps & Activity (Large: 2x2) -->
      <div class="bento-item span-2-2 steps-card" @click="showDetail('步数')">
        <div class="card-bg-icon">👟</div>
        <div class="card-glass">
          <div class="card-top">
            <div class="tag-box"><el-tag size="small" effect="dark" round>今日步数</el-tag></div>
            <span class="card-title">行走活动</span>
          </div>
          <div class="steps-main">
            <div class="steps-value-group">
              <span class="main-val">{{ getLatestValue('步数') || '0' }}</span>
              <span class="sub-unit">步</span>
            </div>
            <div class="progress-container">
              <el-progress type="circle" :percentage="Math.min(100, (Number(getLatestValue('步数')) || 0) / 100)" :width="140" :stroke-width="12" color="var(--primary-gradient)" />
              <div class="progress-info">
                <div class="p-label">目标达成</div>
                <div class="p-percent">{{ Math.floor((Number(getLatestValue('步数')) || 0) / 100) }}%</div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <div class="footer-stat">
              <span class="f-label">昨日步数</span>
              <span class="f-val">8,540</span>
            </div>
            <div class="footer-stat">
              <span class="f-label">距离目标</span>
              <span class="f-val">{{ Math.max(0, 10000 - (Number(getLatestValue('步数')) || 0)) }} 步</span>
            </div>
          </div>
        </div>
      </div>

      <!-- [2] Heart Rate (Wide: 2x1) -->
      <div class="bento-item span-2-1 heart-card" @click="showDetail('心率')">
        <div class="card-glass flex-row">
          <div class="left-info">
            <div class="card-title-group">
               <span class="icon-circle shadow-red"><el-icon><HotWater /></el-icon></span>
               <span class="card-title">心率中心</span>
            </div>
            <div class="hr-value">
              <span class="val">{{ getLatestValue('心率') || '--' }}</span>
              <span class="unit">bpm</span>
            </div>
            <div class="hr-range">
              <span>静息: {{ getLatestValue('静息心率') || '--' }}</span>
              <span class="divider">|</span>
              <span>最高: {{ getLatestValue('最大心率') || '--' }}</span>
            </div>
          </div>
          <div class="right-chart">
            <div ref="heartChartRef" class="chart-box"></div>
          </div>
        </div>
      </div>

      <!-- [3] Body Metrics (Tall: 1x2) -->
      <div class="bento-item span-1-2 body-card" @click="showDetail('体重')">
        <div class="card-glass">
          <div class="card-title">身体成分</div>
          <div class="metric-list">
            <div class="m-item">
              <div class="m-label">体重 (kg)</div>
              <div class="m-val">{{ getLatestValue('体重') || '--' }}</div>
              <div ref="weightChartRef" class="mini-chart-box"></div>
            </div>
            <div class="m-item">
              <div class="m-label">身高 (cm)</div>
              <div class="m-val">{{ getLatestValue('身高') || '--' }}</div>
            </div>
            <div class="m-item bmi-item" :class="getBMICategory(bmi).class">
              <div class="m-label">BMI 指数</div>
              <div class="m-val">{{ bmi || '--' }}</div>
              <div class="bmi-status">{{ getBMICategory(bmi).label }}</div>
            </div>
          </div>
          <el-button class="detail-btn" link @click.stop="detailVisible = true">查看详细分析 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
      </div>

      <!-- [4] Workout Stats (Tall: 1x2) -->
      <div class="bento-item span-1-2 workout-card">
        <div class="card-glass">
          <div class="card-title">训练表现</div>
          <div class="workout-summary">
             <div class="sum-item">
                <div class="s-val">{{ workoutStats.todayDuration || 0 }}</div>
                <div class="s-label">今日时长 (min)</div>
             </div>
             <div class="sum-item">
                <div class="s-val">{{ workoutStats.totalDuration || 0 }}</div>
                <div class="s-label">累计时长 (min)</div>
             </div>
          </div>
          <div class="dist-chart-container">
            <div ref="distChartRef" class="donut-chart"></div>
            <div class="chart-center">
              <span class="c-val">{{ Object.keys(workoutStats.categoryStats || {}).length }}</span>
              <span class="c-label">类型</span>
            </div>
          </div>
          <div class="category-list">
             <div v-for="(val, name) in (workoutStats.categoryStats || {})" :key="name" class="cat-item">
                <span class="dot" :style="{ backgroundColor: getCategoryColor(name) }"></span>
                <span class="c-name">{{ name }}</span>
                <span class="c-val">{{ val }}m</span>
             </div>
             <div v-if="!Object.keys(workoutStats.categoryStats || {}).length" class="empty-cat">暂无运动数据</div>
          </div>
        </div>
      </div>

      <!-- [5] Calories (Wide: 1x1) -->
      <div class="bento-item span-1-1 cal-card">
        <div class="card-glass">
          <div class="card-title-group">
            <el-icon class="icon-orange"><Timer /></el-icon>
            <span class="card-title">热量消耗</span>
          </div>
          <div class="cal-main">
            <div class="val">{{ getLatestValue('消耗') || '450' }} <span class="unit">kcal</span></div>
            <div ref="caloriesChartRef" class="bar-chart-mini"></div>
          </div>
        </div>
      </div>

      <!-- [6] Sleep (Wide: 1x1) -->
      <div class="bento-item span-1-1 sleep-card">
        <div class="card-glass">
          <div class="card-title-group">
            <el-icon class="icon-indigo"><Star /></el-icon>
            <span class="card-title">睡眠概览</span>
          </div>
          <div class="sleep-main">
            <div class="val">{{ getLatestValue('睡眠') || '--' }} <span class="unit">h</span></div>
            <div class="sleep-quality">睡眠报告: <span class="q-val excellent">极佳</span></div>
            <div class="progress-mini">
              <div class="bar" :style="{ width: (getLatestValue('睡眠') ? (Number(getLatestValue('睡眠'))/9 * 100) : 0) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- [7] Achievements (Small: 1x1) -->
      <div class="bento-item span-1-1 award-card">
        <div class="card-glass">
          <div class="card-title-group">
            <el-icon class="icon-yellow"><Trophy /></el-icon>
            <span class="card-title">荣誉</span>
          </div>
          <div class="medal-mini">
             <span class="m-icon active">🏅</span>
             <span class="m-icon active">🏃</span>
             <span class="m-icon">💪</span>
          </div>
          <div class="medal-count">12 枚勋章</div>
        </div>
      </div>

      <!-- [8] Water (Small: 1x1) -->
      <div class="bento-item span-1-1 water-card">
        <div class="card-glass">
          <div class="card-title-group">
             <el-icon class="icon-blue"><Coffee /></el-icon>
             <span class="card-title">饮水量</span>
          </div>
          <div class="water-main">
             <span class="val">1,800</span><span class="unit">ml</span>
             <div class="cup-list">
                <span v-for="i in 8" :key="i" class="cup" :class="{ filled: i <= 6 }">💧</span>
             </div>
          </div>
        </div>
      </div>

      <!-- [9] Blood Pressure (Small: 1x1) -->
      <div class="bento-item span-1-1 bp-card">
        <div class="card-glass">
          <div class="card-title-group">
             <el-icon class="icon-red"><CircleCheck /></el-icon>
             <span class="card-title">血压</span>
          </div>
          <div class="bp-main">
             <div class="val">118/76</div>
             <div class="status healthy">正常</div>
          </div>
        </div>
      </div>

      <!-- [10] Blood Oxygen (Small: 1x1) -->
      <div class="bento-item span-1-1 bo-card">
        <div class="card-glass">
          <div class="card-title-group">
             <el-icon class="icon-emerald"><Odometer /></el-icon>
             <span class="card-title">血氧</span>
          </div>
          <div class="bo-main">
             <div class="val">98%</div>
             <div class="status">理想</div>
          </div>
        </div>
      </div>

      <!-- [11] Stress (Small: 1x1) -->
      <div class="bento-item span-1-1 stress-card">
        <div class="card-glass">
          <div class="card-title-group">
             <el-icon class="icon-purple"><Sunny /></el-icon>
             <span class="card-title">压力指数</span>
          </div>
          <div class="stress-main">
             <div class="val">32</div>
             <div class="status">轻松</div>
          </div>
        </div>
      </div>

      <!-- [12] Device Sync (Small: 1x1) -->
      <div class="bento-item span-1-1 device-card">
        <div class="card-glass">
          <div class="card-title-group">
             <el-icon class="icon-gray"><Monitor /></el-icon>
             <span class="card-title">设备同步</span>
          </div>
          <div class="device-status">
             <div class="device-item"><span class="dot active"></span> Apple Watch</div>
             <div class="device-item"><span class="dot"></span> 智能体脂秤</div>
          </div>
        </div>
      </div>

    </div>

    <!-- Record Data Dialog -->
    <el-dialog v-model="recordDialogVisible" title="记录健康指标" width="400px" align-center>
      <el-form :model="recordForm" label-position="top">
        <el-form-item label="具体指标">
          <el-select v-model="recordForm.name" style="width: 100%">
            <el-option v-for="m in ALL_METRICS" :key="m.key" :label="`${m.icon} ${m.name}`" :value="m.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="数值">
          <el-input-number v-model="recordForm.value" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMetric">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Record Workout Dialog -->
    <el-dialog v-model="workoutDialogVisible" title="记录运动" width="400px" align-center>
      <el-form :model="workoutForm" label-position="top">
        <el-form-item label="运动类型">
          <el-select v-model="workoutForm.type" placeholder="请选择运动类型" style="width: 100%">
            <el-option label="跑步" value="跑步" />
            <el-option label="骑行" value="骑行" />
            <el-option label="游泳" value="游泳" />
            <el-option label="力量训练" value="力量训练" />
            <el-option label="瑜伽" value="瑜伽" />
            <el-option label="普拉提" value="普拉提" />
          </el-select>
        </el-form-item>
        <el-form-item label="运动时长 (分钟)">
          <el-input-number v-model="workoutForm.duration" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWorkout">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" :title="(selectedMetric?.name || '') + ' 详情'" width="800px">
      <div v-if="selectedMetric" class="detail-container">
        <div ref="detailChartRef" class="detail-chart-box"></div>
        <div class="metric-info-box">
          <h3>指标介绍</h3>
          <p>{{ selectedMetric.intro || '暂无说明' }}</p>
          <div v-if="selectedMetric.range" class="range-info">
            建议范围: <el-tag type="success">{{ selectedMetric.range }}</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { Plus, ArrowRight, Timer, Refresh, HotWater, Star, Trophy, CircleCheck, Odometer, Sunny, Monitor, Coffee } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../api/request'

// --- Constants & Types ---
const ALL_METRICS = [
  { key: 'weight', name: '体重', icon: '⚖️', unit: 'kg', intro: '体重是反映健康状况的基础指标。' },
  { key: 'height', name: '身高', icon: '📏', unit: 'cm', intro: '成年后身高保持稳定。' },
  { key: 'steps', name: '步数', icon: '👟', unit: '步', intro: '每日步数反映了日常身体活跃度。', range: '8000-10000 步' },
  { key: 'heart', name: '心率', icon: '💓', unit: 'bpm', intro: '反映心脏搏动频率。', range: '60-100 bpm' },
  { key: 'hr_resting', name: '静息心率', icon: '🧘', unit: 'bpm', intro: '静止时的心率，越低通常代表心肺功能越好。' },
  { key: 'hr_max', name: '最大心率', icon: '⚡', unit: 'bpm', intro: '高强度运动时的最高心率。' },
  { key: 'sleep', name: '睡眠', icon: '😴', unit: 'h', intro: '充足的睡眠对身体恢复至关重要。', range: '7-9 小时' },
  { key: 'calories', name: '消耗', icon: '🔥', unit: 'kcal', intro: '全天活动产生的能量消耗。' }
]

const metricData = ref<Record<string, any[]>>({})
const workoutStats = ref<any>({})

const recordDialogVisible = ref(false)
const workoutDialogVisible = ref(false)
const detailVisible = ref(false)
const selectedMetric = ref<any>(null)

const recordForm = reactive({ name: '体重', value: 70 })
const workoutForm = reactive({ type: '跑步', duration: 30 })

const heartChartRef = ref<HTMLElement | null>(null)
const weightChartRef = ref<HTMLElement | null>(null)
const distChartRef = ref<HTMLElement | null>(null)
const caloriesChartRef = ref<HTMLElement | null>(null)
const detailChartRef = ref<HTMLElement | null>(null)

let chartInstances: Record<string, echarts.ECharts | null> = {
  heart: null,
  weight: null,
  dist: null,
  calories: null,
  detail: null
}

const bmi = computed(() => {
  const w = getLatestValue('体重')
  const h = getLatestValue('身高')
  if (w && h) {
    const heightInMeters = Number(h) / 100
    return (Number(w) / (heightInMeters * heightInMeters)).toFixed(1)
  }
  return null
})

const getLatestValue = (name: string) => {
  const data = metricData.value[name]
  if (data && data.length > 0) {
    return data[data.length - 1].value
  }
  return null
}

const getBMICategory = (val: any) => {
  const n = Number(val)
  if (!n) return { label: '未知', class: '' }
  if (n < 18.5) return { label: '偏瘦', class: 'underweight' }
  if (n < 24) return { label: '正常', class: 'normal' }
  if (n < 28) return { label: '偏胖', class: 'overweight' }
  return { label: '肥胖', class: 'obese' }
}

const getCategoryColor = (name: any) => {
  const colors: Record<string, string> = {
    '跑步': '#6366f1',
    '骑行': '#10b981',
    '游泳': '#3b82f6',
    '力量训练': '#f59e0b',
    '瑜伽': '#ec4899',
    '系统训练': '#8b5cf6'
  }
  return colors[name] || '#94a3b8'
}

const fetchData = async () => {
  try {
    const [metricRes, workRes] = await Promise.all([
      request.get('/metric/latest'),
      request.get('/workout/stats')
    ])
    
    // Group metrics
    const grouped: Record<string, any[]> = {}
    metricRes.data.forEach((m: any) => {
      if (!grouped[m.name]) grouped[m.name] = []
      grouped[m.name].push(m)
    })
    metricData.value = grouped
    workoutStats.value = workRes.data
    
    nextTick(initCharts)
  } catch (e) {
    console.error('Fetch error:', e)
  }
}

const initCharts = () => {
  // Dispose old ones
  Object.values(chartInstances).forEach(c => c?.dispose())

  // [1] Heart Chart
  if (heartChartRef.value) {
    chartInstances.heart = echarts.init(heartChartRef.value)
    const hrData = (metricData.value['心率'] || []).map(d => [d.recordTime, d.value])
    chartInstances.heart.setOption({
      grid: { left: 0, right: 0, top: 10, bottom: 0 },
      xAxis: { type: 'time', show: false },
      yAxis: { type: 'value', show: false, scale: true },
      series: [{
        data: hrData.length > 0 ? hrData : [[new Date(), 60], [new Date(), 80]],
        type: 'line',
        smooth: true,
        showSymbol: false,
        areaStyle: { color: 'rgba(239, 68, 68, 0.1)' },
        lineStyle: { color: '#ef4444', width: 3 }
      }]
    })
  }

  // [2] Weight mini chart
  if (weightChartRef.value) {
    chartInstances.weight = echarts.init(weightChartRef.value)
    const wData = (metricData.value['体重'] || []).map(d => d.value)
    chartInstances.weight.setOption({
      grid: { left: 0, right: 0, top: 0, bottom: 0 },
      xAxis: { type: 'category', show: false },
      yAxis: { type: 'value', show: false, scale: true },
      series: [{
        data: wData.length > 1 ? wData : [70.5, 70.2],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#6366f1', width: 2 }
      }]
    })
  }

  // [3] Workout Donut
  if (distChartRef.value) {
    chartInstances.dist = echarts.init(distChartRef.value)
    const stats = workoutStats.value.categoryStats || {}
    const pieData = Object.entries(stats).map(([name, val]) => ({
      name, value: val, itemStyle: { color: getCategoryColor(name) }
    }))
    chartInstances.dist.setOption({
      series: [{
        type: 'pie',
        radius: ['60%', '85%'],
        avoidLabelOverlap: false,
        label: { show: false },
        data: pieData.length > 0 ? pieData : [{ value: 1, name: '无数据', itemStyle: { color: '#f1f5f9' } }]
      }]
    })
  }

  // [4] Calories Mini Bar
  if (caloriesChartRef.value) {
    chartInstances.calories = echarts.init(caloriesChartRef.value)
    chartInstances.calories.setOption({
      grid: { left: 0, right: 0, top: 0, bottom: 0 },
      xAxis: { type: 'category', show: false },
      yAxis: { show: false },
      series: [{
        data: [120, 200, 150, 80, 220, 110, 130],
        type: 'bar',
        itemStyle: { color: '#f59e0b', borderRadius: [4, 4, 0, 0] }
      }]
    })
  }
}

const submitMetric = async () => {
  try {
    const mod = ALL_METRICS.find(m => m.name === recordForm.name)
    await request.post('/metric/record', { 
      name: recordForm.name, 
      value: recordForm.value,
      unit: mod?.unit || ''
    })
    ElMessage.success('记录成功')
    recordDialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('记录失败') }
}

const submitWorkout = async () => {
  try {
    await request.post('/workout/record', workoutForm)
    ElMessage.success('运动记录成功')
    workoutDialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('记录错误') }
}

const showDetail = (name: string) => {
  const metric = ALL_METRICS.find(m => m.name === name)
  if (metric) {
    selectedMetric.value = metric
    detailVisible.value = true
    nextTick(() => {
      if (detailChartRef.value) {
        chartInstances.detail = echarts.init(detailChartRef.value)
        const data = (metricData.value[name] || []).map(d => [d.recordTime, d.value])
        chartInstances.detail.setOption({
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'time' },
          yAxis: { type: 'value', scale: true },
          series: [{
            data,
            type: 'line',
            smooth: true,
            areaStyle: { color: 'rgba(99, 102, 241, 0.1)' },
            lineStyle: { color: '#6366f1' }
          }]
        })
      }
    })
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => {
    Object.values(chartInstances).forEach(c => c?.resize())
  })
})

onUnmounted(() => {
  Object.values(chartInstances).forEach(c => c?.dispose())
})
</script>

<style scoped>
.dashboard-root {
  padding: 32px;
  max-width: 1440px;
  margin: 0 auto;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.header-right {
  display: flex;
  gap: 12px;
}

.action-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
}

/* Bento Grid */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: minmax(180px, auto);
  gap: 24px;
}

.bento-item {
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.bento-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0,0,0,0.1);
}

.span-2-2 { grid-column: span 2; grid-row: span 2; }
.span-2-1 { grid-column: span 2; }
.span-1-2 { grid-row: span 2; }

.card-glass {
  height: 100%;
  padding: 32px;
  background: white;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Steps Card */
.steps-card { background: #6366f1; }
.steps-card .card-glass { background: rgba(255,255,255,0.05); color: white; border: none; }
.card-bg-icon { position: absolute; right: -20px; bottom: -20px; font-size: 180px; opacity: 0.1; }

.steps-main { display: flex; justify-content: space-between; align-items: center; flex: 1; margin: 24px 0; }
.main-val { font-size: 64px; font-weight: 900; }
.sub-unit { font-size: 20px; margin-left: 8px; opacity: 0.8; }

.progress-info { position: absolute; text-align: center; color: white; }
.p-label { font-size: 12px; opacity: 0.7; }
.p-percent { font-size: 24px; font-weight: 800; }

.card-footer { display: flex; gap: 40px; padding-top: 24px; border-top: 1px solid rgba(255,255,255,0.1); }
.f-label { font-size: 12px; opacity: 0.6; display: block; }
.f-val { font-size: 18px; font-weight: 700; }

/* Heart Card */
.flex-row { flex-direction: row; gap: 32px; }
.left-info { width: 40%; }
.right-chart { flex: 1; height: 100%; min-height: 120px; }
.hr-value .val { font-size: 44px; font-weight: 900; }
.hr-range { font-size: 14px; color: #94a3b8; margin-top: 8px; }
.divider { margin: 0 8px; opacity: 0.3; }

/* Body Card */
.metric-list { flex: 1; margin: 20px 0; }
.m-item { margin-bottom: 16px; position: relative; }
.m-label { font-size: 12px; color: #94a3b8; }
.m-val { font-size: 24px; font-weight: 800; }
.mini-chart-box { position: absolute; right: 0; bottom: 0; width: 100px; height: 40px; }
.bmi-item { background: #f8fafc; padding: 16px; border-radius: 16px; }
.bmi-status { font-size: 12px; font-weight: 700; color: #10b981; margin-top: 4px; }

/* Workout Card */
.workout-summary { display: flex; gap: 24px; margin: 20px 0; }
.dist-chart-container { position: relative; height: 160px; }
.donut-chart { width: 100%; height: 100%; }
.chart-center { position: absolute; left: 50%; top: 50%; transform: translate(-50%,-50%); text-align: center; }
.category-list { margin-top: 20px; display: grid; gap: 8px; }
.cat-item { display: flex; align-items: center; font-size: 12px; }
.dot { width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.c-val { margin-left: auto; color: #64748b; }

/* Global Icons */
.icon-circle { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.shadow-red { background: #fee2e2; color: #ef4444; }

/* Small charts */
.bar-chart-mini { height: 80px; width: 100%; margin-top: 12px; }

/* Awards */
.medal-list { display: flex; justify-content: space-between; margin-top: 32px; }
.medal-item { text-align: center; filter: grayscale(1); opacity: 0.3; }
.medal-item.active { filter: grayscale(0); opacity: 1; transform: translateY(-4px); }
.m-icon { font-size: 32px; display: block; }
.m-name { font-size: 11px; margin-top: 8px; font-weight: 600; color: #64748b; }

.detail-chart-box { height: 300px; width: 100%; }

@media (max-width: 1200px) {
  .bento-grid { grid-template-columns: repeat(2, 1fr); }
  .span-2-2, .span-2-1 { grid-column: span 2; }
}

@media (max-width: 768px) {
  .bento-grid { grid-template-columns: 1fr; }
  .span-2-2, .span-2-1, .span-1-2 { grid-column: span 1; grid-row: auto; }
}
</style>
