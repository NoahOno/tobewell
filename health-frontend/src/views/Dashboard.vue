<template>
  <div class="summary-page">
    <!-- Header -->
    <div class="summary-header">
      <div class="header-left">
        <h1 class="page-title">我的数据</h1>
        <p class="page-subtitle">实时监控您的身体健康指标与运动表现</p>
      </div>
      <div class="header-right">
        <el-popover placement="bottom-end" :width="240" trigger="click">
          <template #reference>
            <el-button circle class="manage-btn">
              <el-icon><Setting /></el-icon>
            </el-button>
          </template>
          <div class="popover-manage">
            <h4 class="manage-title">模块可见性</h4>
            <div class="manage-switches">
              <div class="switch-item">
                <span>运动记录</span>
                <el-switch v-model="visibility.workout" @change="saveVisibility" />
              </div>
              <div class="switch-item">
                <span>身高体重</span>
                <el-switch v-model="visibility.body" @change="saveVisibility" />
              </div>
              <div class="switch-item">
                <span>心率中心</span>
                <el-switch v-model="visibility.heart" @change="saveVisibility" />
              </div>
            </div>
          </div>
        </el-popover>
        <el-button type="primary" class="premium-btn" @click="recordDialogVisible = true">
          <el-icon><Plus /></el-icon> 记录数据
        </el-button>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="summary-tabs">
      <el-tabs v-model="activeTab" class="modern-tabs">
        <el-tab-pane label="概览" name="overview">
          <div class="tab-content overview-grid">
            <!-- Workout Records Module -->
            <div v-if="visibility.workout" class="overview-section workout-section premium-card">
              <div class="section-header">
                <span class="section-icon">🏃</span>
                <span class="section-title">运动记录</span>
                <el-button link type="primary" @click="openWorkoutRecord">记录运动</el-button>
              </div>
              <div class="workout-main-stats">
                <div class="stat-item">
                  <div class="stat-label">总时长</div>
                  <div class="stat-value">{{ workoutStats.totalDuration || 0 }} <span class="unit">min</span></div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item highlight">
                  <div class="stat-label">今日运动</div>
                  <div class="stat-value">{{ workoutStats.todayDuration || 0 }} <span class="unit">min</span></div>
                </div>
              </div>
              <div class="workout-categories">
                <div v-for="(duration, type) in workoutStats.categoryStats" :key="type" class="category-pill">
                  <span class="cat-type">{{ type }}</span>
                  <span class="cat-duration">{{ duration }}min</span>
                </div>
                <div v-if="!workoutStats.categoryStats || Object.keys(workoutStats.categoryStats).length === 0" class="empty-hint">暂无运动分类统计</div>
              </div>
            </div>

            <!-- Body Metrics Module -->
            <div v-if="visibility.body" class="overview-section body-section premium-card">
              <div class="section-header">
                <span class="section-icon">📏</span>
                <span class="section-title">身高体重</span>
              </div>
              <div class="metrics-row">
                <div class="metric-box">
                  <div class="mb-label">身高</div>
                  <div class="mb-value">{{ getLatestValue('身高') || '--' }} <span class="unit">cm</span></div>
                </div>
                <div class="metric-box">
                  <div class="mb-label">体重</div>
                  <div class="mb-value">{{ getLatestValue('体重') || '--' }} <span class="unit">kg</span></div>
                </div>
                <div class="metric-box bmi-box" :class="getBMICategory(bmi).class">
                  <div class="mb-label">BMI</div>
                  <div class="mb-value">{{ bmi || '--' }}</div>
                  <div class="bmi-tag">{{ getBMICategory(bmi).label }}</div>
                </div>
              </div>
            </div>

            <!-- Heart Rate Module -->
            <div v-if="visibility.heart" class="overview-section heart-section premium-card">
              <div class="section-header">
                <span class="section-icon">❤️</span>
                <span class="section-title">心率中心</span>
              </div>
              <div class="hr-stats">
                <div class="hr-main">
                  <div class="hr-curr-label">
                    最近心率
                  </div>
                  <div class="hr-curr-value">{{ getLatestValue('心率') || '--' }} <span class="unit">bpm</span></div>
                </div>
                <div class="hr-others">
                  <div class="hr-sub">
                    <span class="sub-label">最大心率</span>
                    <span class="sub-value max">{{ getLatestValue('最大心率') || '--' }}</span>
                  </div>
                  <div class="hr-sub">
                    <span class="sub-label">静息心率</span>
                    <span class="sub-value resting">{{ getLatestValue('静息心率') || '--' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="趋势" name="trends">
          <div class="tab-content trends-container">
            <div v-for="metric in trendMetrics" :key="metric.key" class="trend-card premium-card">
              <div class="trend-header">
                <span class="trend-title">{{ metric.icon }} {{ metric.name }} 7日趋势</span>
                <span class="latest-val">{{ getLatestValue(metric.name) }} {{ metric.unit }}</span>
              </div>
              <div :ref="el => setChartRef(el as HTMLElement, metric.key)" class="trend-chart-mini"></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="全部" name="all">
          <div class="tab-content all-container">
            <div v-for="(metrics, cat) in groupedMetrics" :key="cat" class="category-block">
              <h3 class="category-title">{{ cat }}</h3>
              <div class="all-grid">
                <div 
                  v-for="mod in metrics" 
                  :key="mod.key" 
                  class="metric-card premium-card"
                  @click="showMetricDetail(mod)"
                >
                  <div class="m-icon">{{ mod.icon }}</div>
                  <div class="m-info">
                    <div class="m-name">{{ mod.name }}</div>
                    <div class="m-val">{{ getLatestValue(mod.name) || '--' }} <span class="m-unit">{{ mod.unit }}</span></div>
                  </div>
                  <el-icon class="m-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Record Data Dialog -->
    <el-dialog v-model="recordDialogVisible" title="记录健康指标" width="400px" align-center>
      <el-form :model="recordForm" label-position="top">
        <el-form-item label="指标类型">
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
          <el-select v-model="workoutForm.type" placeholder="请选择" style="width: 100%">
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
    <el-dialog v-model="detailVisible" :title="selectedMetric?.name + ' 详情'" width="800px" custom-class="detail-dialog">
      <div v-if="selectedMetric" class="detail-layout">
        <!-- 1. 顶部：趋势图 -->
        <div class="detail-section trend-section">
          <div class="section-top">
            <h3 class="inner-title">趋势图</h3>
            <div class="range-summary-box" v-if="rangeSummary">
              <span class="range-summary-label">{{ rangeSummary.label }}:</span>
              <span class="range-summary-value">{{ rangeSummary.value }}</span>
              <span class="range-summary-unit">{{ selectedMetric.unit }}</span>
            </div>
            <el-radio-group v-model="detailTimeRange" size="small" @change="handleRangeChange">
              <el-radio-button label="day">日</el-radio-button>
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="detailChartRef" class="detail-chart-canvas"></div>
        </div>

        <!-- 2. 中部：指标介绍 -->
        <div class="detail-section intro-section">
          <h3 class="inner-title">指标介绍</h3>
          <div class="intro-card premium-card">
            <div class="intro-item">
              <span class="intro-label">简要说明：</span>
              <span class="intro-text">{{ selectedMetric.intro || '暂无说明' }}</span>
            </div>
            <div class="intro-item">
              <span class="intro-label">作用意义：</span>
              <span class="intro-text">帮助用户了解其{{ selectedMetric.name }}变化，从而更好地管理健康。</span>
            </div>
            <div v-if="selectedMetric.range" class="intro-item">
              <span class="intro-label">正常范围：</span>
              <el-tag effect="light" type="success" class="range-tag">{{ selectedMetric.range }}</el-tag>
            </div>
          </div>
        </div>

        <!-- 3. 底部：数据来源 -->
        <div class="detail-section source-section">
          <h3 class="inner-title">数据来源</h3>
          <div class="source-list">
            <div class="source-item active">
              <el-icon><EditPen /></el-icon>
              <span>手动填写 (当前使用)</span>
            </div>
            <div class="source-item disabled">
              <el-icon><Iphone /></el-icon>
              <span>Apple Health (即将支持)</span>
            </div>
            <div class="source-item disabled">
              <el-icon><Connection /></el-icon>
              <span>第三方设备 (即将支持)</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, onUnmounted, watch } from 'vue'
import { Plus, ArrowRight, Setting, EditPen, Iphone, Connection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../api/request'

// --- Definitions ---
const ALL_METRICS = [
  // 1. 运动记录类
  { key: 'total_running', name: '总跑步', icon: '🏃', unit: 'km', category: '运动记录类', chartType: 'bar', intro: '跑步是一项极佳的有氧运动，能增强心肺功能，消耗热量。', range: '建议每周至少 150 分钟中等强度运动' },
  { key: 'total_cycling', name: '总骑行', icon: '🚴', unit: 'km', category: '运动记录类', chartType: 'bar', intro: '骑行是一种低冲击的有氧运动，有助于增强下肢肌肉力量和心肺健康。' },
  { key: 'total_walking', name: '总行走', icon: '🚶', unit: '步', category: '运动记录类', chartType: 'bar', intro: '步行是老少皆宜的运动方式，建议每日达到 8000-10000 步。', range: '每日 8000+ 步' },
  { key: 'stairs', name: '爬楼梯', icon: '🪜', unit: '层', category: '运动记录类', chartType: 'bar', intro: '爬楼梯是一项高强度的日常运动，能显著提升心肺耐力和腿部力量。' },
  { key: 'hiking', name: '徒步', icon: '🥾', unit: 'km', category: '运动记录类', chartType: 'bar', intro: '在自然环境中进行的长途步行，有助于缓解压力和增强体能。' },
  { key: 'trail_running', name: '越野跑', icon: '⛰️', unit: 'km', category: '运动记录类', chartType: 'bar', intro: '在野外小径、森林或山地进行的跑步，挑战性更强，能锻炼全身肌肉。' },
  { key: 'swimming', name: '游泳', icon: '🏊', unit: 'm', category: '运动记录类', chartType: 'bar', intro: '游泳是全身性的协调运动，能有效增强心肺功能和身体线条。' },
  { key: 'strength', name: '力量训练', icon: '🏋️', unit: 'min', category: '运动记录类', chartType: 'bar', intro: '通过各种抗阻练习，增强肌肉力量、爆发力及耐力。' },
  { key: 'yoga', name: '瑜伽', icon: '🧘', unit: 'min', category: '运动记录类', chartType: 'bar', intro: '结合体式、呼吸和冥想，提升身体柔韧性并达到身心平衡。' },
  { key: 'pilates', name: '普拉提', icon: '🤸', unit: 'min', category: '运动记录类', chartType: 'bar', intro: '专注于核心力量、姿态改善和精准控制。' },
  
  // 2. 身体数据类
  { key: 'weight', name: '体重', icon: '⚖️', unit: 'kg', category: '身体数据类', chartType: 'line', intro: '体重是反映人体骨骼、肌肉、脂肪和水分总量的一个重要指标。', range: '18.5 ≤ BMI < 24' },
  { key: 'bmi', name: 'BMI', icon: '📊', unit: '', category: '身体数据类', chartType: 'line', intro: 'BMI（身体质量指数）是用体重（公斤）除以身高（米）的平方得出的数值。', range: '18.5 - 23.9' },
  { key: 'body_fat', name: '体脂率', icon: '📉', unit: '%', category: '身体数据类', chartType: 'line', intro: '体脂率是指人体内脂肪重量在人体总体重中所占的比例。', range: '男: 15-18%, 女: 20-25%' },
  { key: 'height', name: '身高', icon: '📏', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '身高的增长主要受遗传、内分泌、营养、运动及疾病等因素的影响。' },
  { key: 'chest', name: '胸围', icon: '👕', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '反映胸部肌肉和骨骼发育情况的重要指标。' },
  { key: 'waist', name: '腰围', icon: '👖', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '腹部肥胖筛查的主要指标，与代谢综合征密切相关。', range: '男: <90cm, 女: <85cm' },
  { key: 'hip', name: '臀围', icon: '🍑', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '反映臀部肌肉及脂肪分布情况。' },
  { key: 'thigh', name: '大腿围', icon: '🦵', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '反映大腿肌肉发育情况。' },
  { key: 'calf', name: '小腿围', icon: '🦶', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '反映小腿肌肉发育情况。' },
  { key: 'arm', name: '手臂围', icon: '💪', unit: 'cm', category: '身体数据类', chartType: 'line', intro: '反映上肢肌肉发育情况。' },
  
  // 3. 健康 data类
  { key: 'hr_realtime', name: '心率', icon: '💓', unit: 'bpm', category: '健康数据类', chartType: 'line', intro: '最近一次测量的心脏跳动频率。', range: '60-100 bpm' },
  { key: 'hr_resting', name: '静息心率', icon: '🧘', unit: 'bpm', category: '健康数据类', chartType: 'line', intro: '静息心率是在清醒、不活动、安静状态下每分钟心跳的次数。', range: '50-80 bpm' },
  { key: 'hr_max', name: '最大心率', icon: '⚡', unit: 'bpm', category: '健康数据类', chartType: 'line', intro: '在剧烈运动下心脏所能达到的最高心率。' },
  { key: 'sleep', name: '睡眠', icon: '😴', unit: 'h', category: '健康数据类', chartType: 'duration', intro: '良好的睡眠是身体恢复和健康的基础。', range: '7-9 小时' },
  { key: 'period', name: '经期记录', icon: '🩸', unit: '天', category: '健康数据类', chartType: 'block', intro: '女性生理周期的健康记录，有助于了解生殖健康波动。' },
  { key: 'calories', name: '消耗', icon: '🔥', unit: 'kcal', category: '健康数据类', chartType: 'bar', intro: '通过各种活动（包括基础代谢和运动）消耗掉的能量。' },
  { key: 'vo2max', name: '最大摄氧量', icon: '🫁', unit: 'ml/kg/min', category: '健康数据类', chartType: 'line', intro: '反映心肺耐力的黄金指标，数值越高代表体能水平越佳。', range: '健康普通人: 35-45' },
  { key: 'spo2', name: '血氧饱和度', icon: '🩸', unit: '%', category: '健康数据类', chartType: 'line', intro: '血液中血氧的浓度、它是呼吸循环的重要生理参数。', range: '95%-100%' },
  
  // 4. 其他类
  { key: 'diet', name: '饮食记录', icon: '🥗', unit: 'kcal', category: '其他类', chartType: 'bar', intro: '每日摄入的热量与营养分析，帮助维持能量平衡。' },
]

const trendMetrics = [
  { key: 'weight', name: '体重', icon: '⚖️', unit: 'kg' },
  { key: 'total_walking', name: '总行走', icon: '🚶', unit: '步' },
  { key: 'hr_realtime', name: '心率', icon: '💓', unit: 'bpm' }
]

// --- State ---
const activeTab = ref('overview')
const metricData = ref<Record<string, any[]>>({})
const workoutStats = ref<any>({ totalDuration: 0, todayDuration: 0, categoryStats: {} })
const visibility = ref(JSON.parse(localStorage.getItem('dashboard_visibility') || '{"workout":true,"body":true,"heart":true}'))

const recordDialogVisible = ref(false)
const recordForm = reactive({ name: '体重', value: 0 })

const workoutDialogVisible = ref(false)
const workoutForm = reactive({ type: '跑步', duration: 30 })

const detailVisible = ref(false)
const detailTimeRange = ref('week')
const rangeSummary = ref<{ label: string, value: string | number } | null>(null)
const selectedMetric = ref<any>(null)
const selectedMetricHistory = ref<any[]>([])
const detailChartRef = ref<HTMLElement | null>(null)
let detailChart: any = null

const chartRefs = reactive<Record<string, HTMLElement>>({})
const chartInstances = reactive<Record<string, any>>({})

// --- Computed ---
const latestMetrics = computed(() => {
  const result: Record<string, any> = {}
  Object.keys(metricData.value).forEach(name => {
    const list = metricData.value[name]
    if (list.length > 0) {
      // metricData is sorted by time ascending in fetchData, 
      // so the last one is the latest.
      result[name] = list[list.length - 1]
    }
  })
  return result
})

const groupedMetrics = computed(() => {
  const categories = ['运动记录类', '身体数据类', '健康数据类', '其他类']
  const result: Record<string, any[]> = {}
  categories.forEach(cat => result[cat] = [])
  
  ALL_METRICS.forEach(m => {
    if (result[m.category]) {
      result[m.category].push(m)
    }
  })
  return result
})

const bmi = computed(() => {
  const hObj = latestMetrics.value['身高']
  const wObj = latestMetrics.value['体重']
  if (hObj && wObj) {
    const h = hObj.value
    const w = wObj.value
    const bmiVal = w / ((h / 100) * (h / 100))
    return parseFloat(bmiVal.toFixed(1))
  }
  return null
})

// --- Methods ---
const getLatestValue = (name: string) => {
  const metric = latestMetrics.value[name]
  if (!metric) return null
  
  const metricDef = ALL_METRICS.find(m => m.name === name)
  if (metricDef?.realtime) {
    // Strict real-time check: must be from the last 1 minute
    const now = new Date().getTime()
    const recordTime = new Date(metric.recordTime).getTime()
    const diffSeconds = (now - recordTime) / 1000
    if (diffSeconds > 60) {
      return null // Shows -- if older than 1 minute
    }
  }
  
  return metric.value
}

const getBMICategory = (val: number | null) => {
  if (!val) return { label: '未知', class: '' }
  if (val < 18.5) return { label: '偏瘦', class: 'underweight' }
  if (val < 24) return { label: '正常', class: 'normal' }
  if (val < 28) return { label: '偏胖', class: 'overweight' }
  return { label: '肥胖', class: 'obese' }
}

const saveVisibility = () => {
  localStorage.setItem('dashboard_visibility', JSON.stringify(visibility.value))
}

const fetchData = async () => {
  try {
    // 1. Fetch latest for overview & all
    const res: any = await request.get('/metric/latest')
    const grouped: Record<string, any[]> = {}
    const latestRes = res.data.sort((a: any, b: any) => new Date(a.recordTime).getTime() - new Date(b.recordTime).getTime())
    latestRes.forEach((m: any) => {
      if (!grouped[m.name]) grouped[m.name] = []
      grouped[m.name].push(m)
    })
    metricData.value = grouped

    // 2. Fetch specific history for Trend Metrics (7 days)
    for (const tm of trendMetrics) {
      const historyRes: any = await request.get('/metric/list', { params: { name: tm.name } })
      metricData.value[tm.name] = historyRes.data
    }

    // 3. Workout stats
    const workRes: any = await request.get('/workout/stats')
    workoutStats.value = workRes.data

    if (activeTab.value === 'trends') {
      nextTick(initTrendCharts)
    }
  } catch (e) {
    console.error('Fetch error:', e)
  }
}

// Watch activeTab to re-init charts when switching
watch(activeTab, (val) => {
  if (val === 'trends') {
    nextTick(initTrendCharts)
  }
})

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

const openWorkoutRecord = () => {
  workoutDialogVisible.value = true
}

const submitWorkout = async () => {
  try {
    await request.post('/workout/record', workoutForm)
    ElMessage.success('运动记录成功')
    workoutDialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('记录失败') }
}

const handleRangeChange = () => {
  initDetailChart(selectedMetricHistory.value)
}

const showMetricDetail = async (mod: any) => {
  selectedMetric.value = mod
  detailVisible.value = true
  detailTimeRange.value = 'week' // Default to week
  try {
    const res: any = await request.get('/metric/list', { params: { name: mod.name } })
    selectedMetricHistory.value = res.data
    nextTick(() => initDetailChart(res.data))
  } catch (e) {}
}

const initDetailChart = (data: any[]) => {
  if (!detailChartRef.value || !selectedMetric.value) return
  if (detailChart) detailChart.dispose()
  detailChart = echarts.init(detailChartRef.value)

  const { categories, values, summary } = aggregateDataByRange(data, detailTimeRange.value, selectedMetric.value)
  rangeSummary.value = summary

  const isLine = selectedMetric.value.chartType === 'line'
  const isBar = !isLine

  const option: any = {
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: { color: '#1E293B' },
      formatter: (params: any) => {
        const p = params[0]
        if (p.value === undefined || p.value === null) return ''
        return `<div style="font-weight: 800; margin-bottom: 4px;">${p.name}</div>` +
               `<div style="display: flex; justify-content: space-between; gap: 12px;">` +
               `<span>${selectedMetric.value.name}:</span>` +
               `<b>${p.value} ${selectedMetric.value.unit}</b></div>`
      }
    },
    xAxis: { 
      type: 'category',
      data: categories,
      axisLabel: { 
        fontSize: 10, 
        color: '#94A3B8',
        interval: detailTimeRange.value === 'day' ? 3 : (detailTimeRange.value === 'month' ? 4 : 0)
      },
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      splitLine: { show: false }
    },
    yAxis: { 
      type: 'value', 
      scale: true,
      axisLabel: { fontSize: 10, color: '#94A3B8' },
      splitLine: { lineStyle: { type: 'dashed', color: '#F1F5F9' } }
    },
    series: [{
      data: values,
      type: isBar ? 'bar' : 'line',
      smooth: true,
      barMaxWidth: 30,
      connectNulls: true,
      symbol: 'circle',
      symbolSize: isLine ? 8 : 4,
      color: '#6366f1',
      areaStyle: isLine ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0)' }
        ])
      } : undefined,
      itemStyle: {
        borderRadius: isBar ? [4, 4, 0, 0] : 0
      }
    }]
  }

  detailChart.setOption(option)
}

const aggregateDataByRange = (data: any[], range: string, metric: any) => {
  const chartType = metric.chartType
  const now = new Date()
  let categories: string[] = []
  let values: (number | null)[] = []
  
  // 1. Filter Data by range
  let startTime = new Date()
  if (range === 'day') startTime.setHours(0,0,0,0)
  else if (range === 'week') {
    const day = now.getDay()
    const diff = (day === 0 ? 6 : day - 1)
    startTime = new Date(now)
    startTime.setDate(now.getDate() - diff)
    startTime.setHours(0,0,0,0)
  } else if (range === 'month') {
    startTime = new Date(now.getFullYear(), now.getMonth(), 1)
  } else if (range === 'year') {
    startTime = new Date(now.getFullYear(), 0, 1)
  }
  const filtered = data.filter(d => d.recordTime && new Date(d.recordTime) >= startTime)

  // 2. Prepare Slots and Map Data
  const isSumType = chartType === 'bar' || chartType === 'block' || chartType === 'duration'
  const groups: Record<number, number[]> = {}

  if (range === 'day') {
    categories = Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)
    values = Array(24).fill(null)
    filtered.forEach(d => {
      const h = new Date(d.recordTime).getHours()
      if (!groups[h]) groups[h] = []
      groups[h].push(d.value)
    })
  } else if (range === 'week') {
    categories = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    values = Array(7).fill(null)
    filtered.forEach(d => {
      const day = new Date(d.recordTime).getDay()
      const idx = (day === 0 ? 6 : day - 1)
      if (!groups[idx]) groups[idx] = []
      groups[idx].push(d.value)
    })
  } else if (range === 'month') {
    const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
    categories = Array.from({ length: daysInMonth }, (_, i) => `${i + 1}`)
    values = Array(daysInMonth).fill(null)
    filtered.forEach(d => {
      const day = new Date(d.recordTime).getDate()
      const idx = day - 1
      if (!groups[idx]) groups[idx] = []
      groups[idx].push(d.value)
    })
  } else if (range === 'year') {
    categories = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    values = Array(12).fill(null)
    filtered.forEach(d => {
      const m = new Date(d.recordTime).getMonth()
      if (!groups[m]) groups[m] = []
      groups[m].push(d.value)
    })
  }

  // 3. Fill values
  Object.keys(groups).forEach(idxKey => {
    const idx = parseInt(idxKey)
    const vals = groups[idx]
    if (vals.length > 0) {
      const val = isSumType ? vals.reduce((a, b) => a + b, 0) : vals.reduce((a, b) => a + b, 0) / vals.length
      values[idx] = parseFloat(val.toFixed(1))
    }
  })

  // 4. Calculate Summary
  let summaryValue: any = 0
  let summaryLabel = isSumType ? '阶段总计' : '阶段平均'
  if (filtered.length > 0) {
    const rawVals = filtered.map(d => d.value)
    if (metric.key === 'hr_max') {
      summaryValue = Math.max(...rawVals)
      summaryLabel = '阶段最大'
    } else if (isSumType) {
      summaryValue = rawVals.reduce((a, b) => a + b, 0).toFixed(1)
    } else {
      summaryValue = (rawVals.reduce((a, b) => a + b, 0) / rawVals.length).toFixed(1)
    }
  } else {
    summaryValue = '--'
  }

  return { categories, values, summary: { label: summaryLabel, value: summaryValue } }
}

const setChartRef = (el: HTMLElement, key: string) => {
  if (el) chartRefs[key] = el
}

const initTrendCharts = () => {
  const now = new Date()
  const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  
  trendMetrics.forEach(m => {
    const el = chartRefs[m.key]
    if (!el) return
    if (chartInstances[m.key]) chartInstances[m.key].dispose()
    const chart = echarts.init(el)
    chartInstances[m.key] = chart
    
    // Filter data for the last 7 days
    const allData = metricData.value[m.name] || []
    const trendData = allData.filter(d => new Date(d.recordTime) >= sevenDaysAgo)
    
    chart.setOption({
      grid: { left: 50, right: 20, top: 10, bottom: 25 },
      tooltip: { 
        trigger: 'axis',
        formatter: (params: any) => {
          const p = params[0]
          if (!p.value) return '--'
          const d = new Date(p.value[0])
          return `${d.getMonth()+1}/${d.getDate()}<br/>${m.name}: ${p.value[1]} ${m.unit}`
        }
      },
      xAxis: { 
        type: 'time',
        axisLabel: { 
          fontSize: 10,
          formatter: (value: any) => {
            const d = new Date(value)
            return `${d.getMonth()+1}/${d.getDate()}`
          }
        },
        splitLine: { show: false }
      },
      yAxis: { type: 'value', axisLabel: { fontSize: 10 }, scale: true, splitLine: { lineStyle: { type: 'dashed' } } },
      series: [{ 
        data: trendData.map(d => [d.recordTime, d.value]), 
        type: 'line', 
        smooth: true, 
        symbol: 'circle',
        symbolSize: 6,
        connectNulls: true,
        color: '#6366f1',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
          ])
        }
      }]
    })
  })
}

const formatDate = (row: any, column: any, cellValue: string, mini = false) => {
  const d = new Date(cellValue)
  if (mini) return `${d.getMonth()+1}/${d.getDate()}`
  return `${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

// --- Lifecycle ---
let pollTimer: any = null

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => {
    Object.values(chartInstances).forEach(c => c?.resize())
    detailChart?.resize()
  })
  
  // Start polling for real-time metrics
  pollTimer = setInterval(() => {
    fetchRealtimeMetrics()
  }, 5000) // Poll every 5 seconds
})

const fetchRealtimeMetrics = async () => {
  try {
    const res: any = await request.get('/metric/latest')
    const grouped: Record<string, any[]> = { ...metricData.value }
    
    // Sort backend results to match our intended order
    const sortedRes = res.data.sort((a: any, b: any) => new Date(a.recordTime).getTime() - new Date(b.recordTime).getTime())
    
    sortedRes.forEach((m: any) => {
      const metricDef = ALL_METRICS.find(def => def.name === m.name)
      if (metricDef?.realtime) {
        if (!grouped[m.name]) grouped[m.name] = []
        const last = grouped[m.name][grouped[m.name].length - 1]
        if (!last || last.id !== m.id) {
          grouped[m.name].push(m)
          if (grouped[m.name].length > 50) grouped[m.name].shift()
        }
      } else {
        // Also update non-realtime latest if changed
        if (!grouped[m.name]) grouped[m.name] = []
        const last = grouped[m.name][grouped[m.name].length - 1]
        if (!last || new Date(m.recordTime) > new Date(last.recordTime)) {
          grouped[m.name].push(m)
        }
      }
    })
    metricData.value = grouped
  } catch (e) {
    console.error('Polling error:', e)
  }
}

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  Object.values(chartInstances).forEach(c => c?.dispose())
  detailChart?.dispose()
})
</script>

<style scoped>
.summary-page {
  padding: 24px;
  background: #F8FAFC;
  min-height: 100%;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 12px; }

.page-title { font-size: 32px; font-weight: 900; color: #1E293B; margin: 0; letter-spacing: -0.5px; }
.page-subtitle { font-size: 14px; color: #64748B; margin: 8px 0 0; }

.manage-btn {
  border: 1px solid #E2E8F0;
  color: #64748B;
  background: white;
  transition: all 0.2s;
}
.manage-btn:hover {
  background: #F8FAFC;
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.popover-manage {
  padding: 4px;
}
.manage-title {
  font-size: 14px;
  font-weight: 800;
  color: #1E293B;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #F1F5F9;
}

.summary-tabs {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: var(--shadow-premium);
}

.modern-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.modern-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  height: 50px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.overview-section {
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.section-icon { font-size: 24px; }
.section-title { font-size: 18px; font-weight: 800; color: #1E293B; flex: 1; }

/* Workout Module */
.workout-main-stats {
  display: flex;
  align-items: center;
  background: #F8FAFC;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 16px;
}
.stat-item { flex: 1; text-align: center; }
.stat-divider { width: 1px; height: 40px; background: #E2E8F0; margin: 0 20px; }
.stat-label { font-size: 12px; color: #94A3B8; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 900; color: #1E293B; }
.unit { font-size: 12px; font-weight: 400; color: #94A3B8; }
.stat-item.highlight .stat-value { color: var(--el-color-primary); }

.workout-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.category-pill {
  background: white;
  border: 1px solid #F1F5F9;
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 13px;
  display: flex;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.cat-type { color: #64748B; font-weight: 600; }
.cat-duration { color: var(--el-color-primary); font-weight: 800; }
.empty-hint { font-size: 13px; color: #CBD5E1; font-style: italic; }

/* Body Metrics Module */
.metrics-row {
  display: flex;
  gap: 16px;
}
.metric-box {
  flex: 1;
  background: white;
  border: 1px solid #F1F5F9;
  padding: 16px;
  border-radius: 12px;
  text-align: center;
}
.mb-label { font-size: 12px; color: #94A3B8; margin-bottom: 8px; }
.mb-value { font-size: 24px; font-weight: 800; color: #1E293B; }

.bmi-box { 
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border-left: 4px solid #F1F5F9; 
}
.bmi-tag { 
  font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 4px; 
  margin-top: 4px; color: white;
  background: #CBD5E1;
}
.underweight .bmi-tag { background: #60A5FA; }
.normal .bmi-tag { background: #34D399; }
.overweight .bmi-tag { background: #FBBF24; }
.obese .bmi-tag { background: #F87171; }

/* Heart Rate Module */
.hr-stats {
  display: flex;
  gap: 24px;
  background: #FFF1F2;
  padding: 24px;
  border-radius: 16px;
}
.hr-main { flex: 1; border-right: 1px solid rgba(0,0,0,0.05); }
.hr-curr-label { font-size: 13px; color: #F43F5E; font-weight: 600; margin-bottom: 4px; }
.hr-curr-value { font-size: 36px; font-weight: 900; color: #E11D48; }

.hr-others { 
  flex: 1; display: flex; flex-direction: column; justify-content: space-around;
  padding-left: 12px;
}
.hr-sub { display: flex; justify-content: space-between; align-items: center; }
.sub-label { font-size: 12px; color: #FDA4AF; }
.sub-value { font-size: 18px; font-weight: 800; }
.sub-value.max { color: #E11D48; }
.sub-value.resting { color: #BE123C; }

/* Manage Section */
.manage-switches {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #F8FAFC;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

/* Trends Tab */
.trends-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.trend-card { padding: 20px; }
.trend-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.trend-title { font-weight: 800; color: #1E293B; }
.latest-val { font-size: 18px; font-weight: 900; color: var(--el-color-primary); }
.trend-chart-mini { height: 200px; width: 100%; }

/* All Tab */
.all-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.metric-card {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.metric-card:hover { transform: translateY(-3px); border-color: var(--el-color-primary); }
.m-icon { font-size: 28px; }
.m-info { flex: 1; }
.m-name { font-size: 12px; color: #94A3B8; }
.m-val { font-size: 18px; font-weight: 800; color: #1E293B; }
.m-unit { font-size: 11px; font-weight: 400; color: #CBD5E1; }
.m-arrow { color: #CBD5E1; }

/* All Tab Categorized */
.all-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}
.category-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.category-title {
  font-size: 16px;
  font-weight: 800;
  color: #64748B;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.category-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: var(--el-color-primary);
  border-radius: 2px;
}

/* Pulse Icon for Real-time */
.pulse-icon {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #F43F5E;
  border-radius: 50%;
  margin-left: 8px;
  position: relative;
  vertical-align: middle;
}
.pulse-icon::after {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: #F43F5E;
  border-radius: 50%;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(3); opacity: 0; }
}

/* Detail Dialog Enhanced Styles */
.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.inner-title {
  font-size: 16px;
  font-weight: 800;
  color: #1E293B;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}
.range-summary-box {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: baseline;
  gap: 8px;
  background: white;
  padding: 4px 16px;
  border-radius: 100px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.1);
}
.range-summary-label {
  font-size: 11px;
  color: #64748B;
  font-weight: 600;
}
.range-summary-value {
  font-size: 20px;
  font-weight: 900;
  color: #6366f1;
}
.range-summary-unit {
  font-size: 11px;
  color: #94A3B8;
}
.detail-chart-canvas {
  height: 300px;
  width: 100%;
  background: white;
  border-radius: 12px;
  padding: 12px;
}

.intro-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.intro-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}
.intro-label {
  font-weight: 800;
  color: #64748B;
  white-space: nowrap;
}
.intro-text {
  color: #1E293B;
}

.source-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.source-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
  font-size: 12px;
  color: #94A3B8;
  transition: all 0.2s;
}
.source-item .el-icon {
  font-size: 24px;
}
.source-item.active {
  background: #F0F9FF;
  border-style: solid;
  border-color: #0EA5E9;
  color: #0EA5E9;
}
.source-item.disabled {
  background: #F8FAFC;
  opacity: 0.6;
  cursor: not-allowed;
}

:deep(.detail-dialog) {
  border-radius: 20px;
  overflow: hidden;
}
:deep(.detail-dialog .el-dialog__header) {
  margin-right: 0;
  padding-bottom: 20px;
  border-bottom: 1px solid #F1F5F9;
}
:deep(.detail-dialog .el-dialog__body) {
  padding: 24px;
  background: #F8FAFC;
}

/* Detail Chart */
.detail-chart { height: 300px; width: 100%; }
</style>
