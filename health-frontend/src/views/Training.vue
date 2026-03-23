<template>
  <div class="training-dashboard">
    <!-- Main Content Area -->
    <div class="main-body" v-loading="loading">
      
      <!-- Training Diary (Calendar View) -->
      <template v-if="activeMenu === 'calendar'">
        <div class="calendar-layout">
          <div class="calendar-left">
            <h3 class="view-title">日程安排 ({{ currentMonth }})</h3>
            <el-calendar v-model="selectedDate" class="premium-calendar">
              <template #date-cell="{ data }">
                <div class="cell-content">
                  <span class="day-text">{{ data.day.split('-').pop() }}</span>
                  <div class="cell-events" v-if="getSchedules(data.day).length > 0">
                    <div
                      v-for="s in getSchedules(data.day)" 
                      :key="s.id" 
                      class="schedule-event" 
                      :class="getScheduleCellClass(s)"
                      :title="s.title"
                    >
                      {{ s.title }}
                    </div>
                  </div>
                </div>
              </template>
            </el-calendar>
          </div>

          <div class="calendar-right">
            <h3 class="view-title">当日任务表</h3>
            <div class="selected-date-display">{{ formattedSelectedDate }}</div>
            
            <div class="daily-tasks-wrapper">
              <div v-if="selectedDaySchedules.length === 0" class="empty-tasks">
                <el-empty description="这天没有训练安排" :image-size="80" />
                <el-button type="primary" round plain @click="router.push('/app/explore')">去探索新计划</el-button>
              </div>

              <!-- Loop Through Schedules For This Day -->
              <div v-for="sched in selectedDaySchedules" :key="sched.id" class="task-card premium-card">
                <div class="tc-header">
                  <div class="tc-title">
                    <h4>{{ sched.title }}</h4>
                    <span class="tc-status" :class="getTaskStatusClass(sched)">{{ mapStatus(sched.status, sched.date) }}</span>
                  </div>
                  <!-- Cancel Plan Button (If this schedule belongs to an active plan) -->
                  <el-button size="small" type="danger" text @click="handleCancelPlan(sched.planId)">取消计划退出</el-button>
                </div>
                
                <p class="tc-desc">{{ sched.description }}</p>

                <!-- Actions List -->
                <div class="actions-list">
                  <div v-for="(act, idx) in parseActions(sched.actions)" :key="idx" class="al-item">
                    <span class="al-name">{{ idx + 1 }}. {{ act.name }}</span>
                    <span class="al-sets">{{ act.sets }}</span>
                  </div>
                </div>

                <div class="tc-footer">
                  <template v-if="sched.status === 'COMPLETED'">
                    <div class="success-msg"><el-icon><Check /></el-icon> 任务已完成打卡，真棒！</div>
                  </template>
                  <template v-else-if="sched.status === 'SKIPPED'">
                    <div class="skipped-msg">该任务已被跳过</div>
                  </template>
                  <template v-else-if="sched.date < todayStr">
                    <!-- Past date: read-only, cannot start -->
                    <div class="locked-msg"><el-icon><Lock /></el-icon> 仅当日可完成训练</div>
                  </template>
                  <template v-else-if="sched.date > todayStr">
                    <!-- Future date: not yet available -->
                    <div class="future-msg">📅 训练日程未到，请届时再来</div>
                  </template>
                  <template v-else>
                    <!-- Today: Skip and Start Options -->
                    <el-button @click="handleSkipSchedule(sched.id)">跳过</el-button>
                    <el-button type="success" @click="openImmersiveTraining(sched, 'SCHEDULE')">开始训练日程</el-button>
                  </template>
                </div>
              </div>
            </div>

            <!-- Stats Module -->
            <div class="stats-module premium-card mt-4">
              <h4>累计训练数据</h4>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="s-val">{{ totalRecords }} <span class="s-unit">次</span></div>
                  <div class="s-lbl">总计完成打卡</div>
                </div>
                <!-- Extendable with duration/records -->
              </div>
            </div>

          </div>
        </div>
      </template>

      <!-- Active Training Plans Management -->
      <template v-if="activeMenu === 'plans'">
        <div class="section-header">
          <h3>我的训练</h3>
          <el-radio-group v-model="myTrainingTab" size="small">
            <el-radio-button label="plans">系列计划</el-radio-button>
            <el-radio-button label="courses">单次课程</el-radio-button>
          </el-radio-group>
        </div>

        <template v-if="myTrainingTab === 'plans'">
          <div class="card-grid">
            <div v-for="plan in activePlans" :key="plan.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="success" effect="dark" style="border:none">推进中</el-tag></div>
               <h3 class="card-title">{{ plan.title }}</h3>
               <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
               <div class="card-actions mt-3">
                 <el-button size="small" type="danger" plain round @click="handleCancelPlan(plan.id)">移除计划</el-button>
               </div>
            </div>
            <el-empty v-if="activePlans.length === 0" description="暂无正在进行中的训练计划" />
          </div>
        </template>

        <template v-else-if="myTrainingTab === 'courses'">
          <div class="card-grid">
            <div v-for="course in myCourses" :key="course.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="primary" effect="dark" style="border:none">单次课</el-tag></div>
               <h3 class="card-title">{{ course.title }}</h3>
               <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
               <div class="plan-specs mt-2">
                  <div class="spec-item"><el-icon><Timer /></el-icon> {{ course.durationMinutes }} 分钟</div>
               </div>
               <div class="card-actions mt-3">
                 <el-button size="small" type="danger" text @click="handleRemoveCourse(course.id)">移除</el-button>
                 <el-button size="small" type="success" round @click="openImmersiveTraining(course, 'COURSE')">立即开练</el-button>
               </div>
            </div>
            <el-empty v-if="myCourses.length === 0" description="暂无收藏的单次课程" />
          </div>
        </template>
      </template>

      <!-- Favorites / Want to Train -->
      <template v-if="activeMenu === 'favorites'">
        <div class="section-header">
          <h3>想练及收藏</h3>
          <el-radio-group v-model="favoriteTab" size="small">
            <el-radio-button label="plans">计划</el-radio-button>
            <el-radio-button label="courses">课程</el-radio-button>
          </el-radio-group>
        </div>

        <template v-if="favoriteTab === 'plans'">
          <div class="card-grid">
            <div v-for="plan in favoritePlans" :key="plan.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="warning" effect="dark" style="border:none">想练</el-tag></div>
               <h3 class="card-title">{{ plan.title }}</h3>
               <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
               <div class="card-actions mt-3">
                 <el-button size="small" type="warning" plain round @click="unfavorite(plan, 'PLAN')">移出想练</el-button>
               </div>
            </div>
            <el-empty v-if="favoritePlans.length === 0" description="暂无添加到想练的计划" />
          </div>
        </template>

        <template v-else>
          <div class="card-grid">
            <div v-for="course in favoriteCourses" :key="course.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="primary" effect="dark" style="border:none">单次课收藏</el-tag></div>
               <h3 class="card-title">{{ course.title }}</h3>
               <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
               <div class="card-actions mt-3">
                 <el-button size="small" type="warning" plain round @click="unfavorite(course, 'COURSE')">移出收藏</el-button>
               </div>
            </div>
            <el-empty v-if="favoriteCourses.length === 0" description="暂无收藏的单次课程" />
          </div>
        </template>
      </template>

      <!-- My Created -->
      <template v-if="activeMenu === 'created'">
        <div class="section-header">
           <!-- Would normally contain a "Create New" flow, but leaving as placeholder per typical UX steps -->
          <h3>我的创建 (自定义计划)</h3>
          <el-button type="primary" round><el-icon><Plus/></el-icon> 创建方案</el-button>
        </div>
        <div class="card-grid">
          <div v-for="plan in userCreatedPlans" :key="plan.id" class="module-card premium-card">
             <div class="card-header-tags"><el-tag size="small" effect="dark" style="border:none">原创</el-tag></div>
             <h3 class="card-title">{{ plan.title }}</h3>
             <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
          </div>
          <el-empty v-if="userCreatedPlans.length === 0" description="您还没有自己创建任何训练方案哦" />
        </div>
      </template>

    </div>

    <!-- Immersive Training Panel Dialog -->
    <el-dialog v-model="immersiveVisible" :title="activeSessionTitle" width="800px" top="5vh" class="immersive-dialog" destroy-on-close>
      <div class="immersive-layout">
        <!-- Left: Action Checklist -->
        <div class="immersive-left">
          <h4 style="margin: 0 0 16px;">今日动作列表</h4>
          <div class="action-check-list">
            <el-checkbox-group v-model="completedActions">
              <div v-for="(act, idx) in activeSessionActions" :key="idx" class="action-check-item">
                <el-checkbox :label="idx">
                  <span class="ac-name">{{ act.name }}</span>
                  <span class="ac-sets">{{ act.sets }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
          <div class="progress-wrap">
             <el-progress :percentage="trainingProgress" :status="trainingProgress === 100 ? 'success' : ''" :stroke-width="12" />
          </div>
        </div>

        <!-- Right: Timer -->
        <div class="immersive-right">
          <div class="timer-display">
            <div class="t-lbl">当前用时</div>
            <div class="t-val">{{ formattedTimer }}</div>
            <el-button type="primary" plain size="small" @click="toggleTimer">{{ timerRunning ? '暂停' : '继续' }}</el-button>
          </div>
          <div class="tip-area mt-4">
            <h4 style="margin: 0 0 12px; font-size: 15px;">💡 训练说明</h4>
            <p style="font-size: 13px; color: #64748B; line-height: 1.6;">请对照左侧动作列表逐一完成。完成后点击下方按钮进行打卡记录。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button @click="closeImmersive" type="info" plain>中止退出</el-button>
          <el-button type="success" size="large" @click="handleImmersiveComplete" :loading="checkingIn" :disabled="trainingProgress < 100">
            <el-icon><Check /></el-icon> 完成训练
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Feedback / Check-in Dialog -->
    <el-dialog v-model="checkInVisible" title="训练反馈记录" width="450px" align-center destroy-on-close>
      <div class="checkin-success-header">
        <el-icon :color="completedMarked ? '#10b981' : '#94a3b8'" size="48">
          <Trophy v-if="completedMarked" />
          <Edit v-else />
        </el-icon>
        <div class="success-text">{{ completedMarked ? '训练已完成！' : '记录本次训练' }}</div>
        <p>请填写本次训练的感受</p>
      </div>

      <el-form :model="feedbackForm" label-position="top">
        <el-form-item label="阶段用时：">
           <b style="font-size: 20px; color: #1e293b;">{{ formattedTimer }}</b>
        </el-form-item>
        <el-form-item label="身体状态评定：">
          <el-radio-group v-model="feedbackForm.difficulty">
            <el-radio-button label="TOO_EASY">轻松</el-radio-button>
            <el-radio-button label="GOOD">适中</el-radio-button>
            <el-radio-button label="TOO_HARD">吃力</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="感受/备忘录 (选填)：">
          <el-input v-model="feedbackForm.feeling" type="textarea" :rows="3" placeholder="写下今天的一点感悟..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" size="large" @click="submitImmersiveCheckIn" :loading="checkingIn" style="width: 100%; border-radius: 12px;">提交反馈</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, Star, Edit, Plus, Check, Trophy, Lightning, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const route = useRoute()
const activeMenu = computed(() => (route.query.tab as string) || 'calendar')
const loading = ref(false)
const myTrainingTab = ref('plans') // internal switcher for My Training

// Month Data State
const selectedDate = ref(new Date())
const monthlySchedules = ref<any[]>([])
const allMyPlans = ref<any[]>([])
const myCourses = ref<any[]>([])
const favoritePlans = ref<any[]>([])
const favoriteCourses = ref<any[]>([])
const favoriteTab = ref('plans') // internal switcher for favorites
const totalRecords = ref(0)

// Today's date string for calendar restriction
const todayStr = new Date().toISOString().split('T')[0]

const currentMonth = computed(() => {
  const d = selectedDate.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const formattedSelectedDate = computed(() => {
  const d = selectedDate.value
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dy = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${dy}`
})

const selectedDaySchedules = computed(() => {
  return getSchedules(formattedSelectedDate.value)
})

const userCreatedPlans = computed(() => {
  // Plans where isPublic is false and sourceId is null means user created from scratch
  return allMyPlans.value.filter(p => !p.isPublic && p.sourceId == null)
})

const activePlans = computed(() => {
  return allMyPlans.value.filter(p => p.isSubscribed !== false && (p.status === 'ACTIVE' || p.status === 'PLANNING'))
})

// Data Fetching
const fetchMonthSchedules = async (dateObj: Date) => {
  loading.value = true
  try {
    // Get beginning and end of month +- buffer
    const d = new Date(dateObj)
    const y = d.getFullYear()
    const m = d.getMonth()
    const start = new Date(y, m - 1, 20).toISOString().split('T')[0]
    const end = new Date(y, m + 1, 10).toISOString().split('T')[0]
    
    const res: any = await request.get(`/daily/range?start=${start}&end=${end}`)
    monthlySchedules.value = res.data || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const fetchAllMyPlans = async () => {
    try {
        const res: any = await request.get('/training/list')
        allMyPlans.value = res.data || []
    } catch(e) {}
}

const fetchMyCourses = async () => {
    try {
        const res: any = await request.get('/course/my')
        myCourses.value = res.data || []
    } catch(e) {}
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    // Plans
    const resP: any = await request.get('/interaction/collections?type=PLAN')
    const pIds = resP.data.map((c: any) => c.targetId)
    const libResP: any = await request.get('/training/library')
    favoritePlans.value = libResP.data.filter((p: any) => pIds.includes(p.id))

    // Courses
    const resC: any = await request.get('/interaction/collections?type=COURSE')
    const cIds = resC.data.map((c: any) => c.targetId)
    const libResC: any = await request.get('/course/library')
    favoriteCourses.value = libResC.data.filter((c: any) => cIds.includes(c.id))
  } catch(e) {} finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res: any = await request.get('/daily/records')
    totalRecords.value = (res.data || []).length
  } catch(e) {}
}

// Schedule Operations
const mapStatus = (s: string, dateStr: string) => {
  if(s==='COMPLETED') return '已完成'
  if(s==='SKIPPED') return '已跳过'
  const today = new Date().toISOString().split('T')[0]
  if (dateStr < today) return '未完成'
  return '待执行'
}

const getTaskStatusClass = (sched: any) => {
  if (sched.status === 'COMPLETED') return 'status-COMPLETED'
  if (sched.status === 'SKIPPED') return 'status-SKIPPED'
  const today = new Date().toISOString().split('T')[0]
  if (sched.date < today) return 'status-MISSED'
  return 'status-PENDING'
}

const getScheduleCellClass = (s: any) => {
  if (s.status === 'COMPLETED') return 'completed'
  if (s.status === 'SKIPPED') return 'skipped'
  const today = new Date().toISOString().split('T')[0]
  if (s.date < today) return 'missed'
  return 'pending'
}

const getSchedules = (dateStr: string) => {
  return monthlySchedules.value.filter(s => s.date === dateStr)
}

const parseActions = (json: string) => {
  if (!json) return []
  try { return JSON.parse(json) } catch { return [] }
}

const handleSkipSchedule = async (id: number, silent=false) => {
  try {
    if (!silent) {
        await ElMessageBox.confirm('确定要手动跳过今天的这项训练任务吗？将会标记为“已跳过”', '提示', { type: 'info' })
    }
    await request.post(`/daily/${id}/skip`)
    if (!silent) ElMessage.success('已跳过')
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleCompleteScheduleDirectly = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要将此项任务标记为“已完成”吗？', '确认', { type: 'success' })
    await request.post(`/daily/${id}/complete`, { status: 'COMPLETED' })
    ElMessage.success('任务已标记完成')
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleCancelPlan = async (planId: number) => {
  try {
    await ElMessageBox.confirm('这会导致正在跟踪的整套计划与余下日程被取消退订，确定吗？', '取消退订确认', { type: 'danger' })
    await request.post(`/training/unsubscribe/${planId}`) // Or delete API if fully implemented
    ElMessage.success('训练计划已取消并退订')
    fetchMonthSchedules(selectedDate.value)
    fetchAllMyPlans()
  } catch(e) {}
}

const handleRemoveCourse = async (courseId: number) => {
  try {
    await ElMessageBox.confirm('确定要从我的课程中移除该单次课吗？', '移除确认', { type: 'warning' })
    await request.delete(`/course/${courseId}`)
    ElMessage.success('单次课程已移除')
    fetchMyCourses()
    fetchMonthSchedules(selectedDate.value)
    fetchStats()
  } catch(e) {}
}

const unfavorite = async (item: any, type: string) => {
  try {
    await request.delete(`/interaction/collect?targetId=${item.id}&targetType=${type}`)
    ElMessage.success('已移出列表')
    fetchFavorites()
  } catch(e) {}
}

// Immersive Check-in Dialog
const immersiveVisible = ref(false)
const checkingIn = ref(false)
const activeSession = ref<any>(null)
const activeSessionType = ref<'SCHEDULE' | 'COURSE'>('SCHEDULE')
const activeSessionTitle = computed(() => {
  if (!activeSession.value) return ''
  return activeSessionType.value === 'COURSE' ? `[单次课] ${activeSession.value.title}` : `[日常排期] ${activeSession.value.title}`
})
const activeSessionActions = computed(() => {
  if (!activeSession.value) return []
  return parseActions(activeSessionType.value === 'COURSE' ? activeSession.value.actionsJson : activeSession.value.actions)
})

const completedActions = ref<number[]>([])
const trainingProgress = computed(() => {
  if (activeSessionActions.value.length === 0) return 100 // fallback if no actions
  return Math.round((completedActions.value.length / activeSessionActions.value.length) * 100)
})

// Timer logic
const timerSeconds = ref(0)
const timerRunning = ref(false)
let timerInterval: any = null

const formattedTimer = computed(() => {
  const m = Math.floor(timerSeconds.value / 60).toString().padStart(2, '0')
  const s = (timerSeconds.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const toggleTimer = () => {
  if (timerRunning.value) {
    clearInterval(timerInterval)
    timerRunning.value = false
  } else {
    timerRunning.value = true
    timerInterval = setInterval(() => { timerSeconds.value++ }, 1000)
  }
}

const feedbackForm = reactive({
  difficulty: 'GOOD',
  feeling: ''
})

const checkInVisible = ref(false)

const openImmersiveTraining = (session: any, type: 'SCHEDULE' | 'COURSE') => {
  activeSession.value = session
  activeSessionType.value = type
  completedActions.value = []
  timerSeconds.value = 0
  
  // reset feedback
  feedbackForm.difficulty = 'GOOD'
  feedbackForm.feeling = ''

  immersiveVisible.value = true
  
  // start timer instantly
  toggleTimer()
}

const closeImmersiveQuietly = () => {
  if (timerRunning.value) {
    clearInterval(timerInterval)
    timerRunning.value = false
  }
  immersiveVisible.value = false
}

const completedMarked = ref(false)
// Track whether the immersive session was completed (vs. aborted) for feedback routing
const sessionWasCompleted = ref(false)

const openCheckIn = (wasCompleted: boolean) => {
  completedMarked.value = wasCompleted
  sessionWasCompleted.value = wasCompleted
  if (timerRunning.value) toggleTimer()
  checkInVisible.value = true
}

const handleImmersiveComplete = async () => {
  // 1. Mark the schedule as COMPLETED first
  checkingIn.value = true
  try {
    if (activeSessionType.value === 'SCHEDULE' && activeSession.value?.id) {
      await request.post(`/daily/${activeSession.value.id}/complete`, { status: 'COMPLETED' })
      ElMessage.success('训练已标记完成')
      await fetchMonthSchedules(selectedDate.value)
    }
  } catch(e) {} finally {
    checkingIn.value = false
  }
  // 2. Close panel and open feedback (no further status changes — feedback uses /feedback endpoint)
  closeImmersiveQuietly()
  openCheckIn(true)
}

const closeImmersive = async () => {
  try {
      await ElMessageBox.confirm('确定要中止本次训练吗？', '确认退出', {
          confirmButtonText: '确定中止',
          cancelButtonText: '继续训练',
          type: 'warning'
      })
      // Close the immersive panel first
      closeImmersiveQuietly()
      // Open feedback but mark as NOT completed (abort flow)
      openCheckIn(false)
  } catch (e) {
      // User clicked '继续训练'
  }
}

const submitImmersiveCheckIn = async () => {
  checkingIn.value = true
  const duration = Math.max(1, Math.floor(timerSeconds.value / 60))
  const payload = {
    completeDuration: duration,
    difficulty: feedbackForm.difficulty,
    feeling: feedbackForm.feeling
  }
  
  try {
    if (activeSessionType.value === 'COURSE') {
      // Course: use the existing complete endpoint (courses don't revert status here)
      await request.post(`/course/${activeSession.value.id}/complete`, payload)
    } else {
      // SCHEDULE: use dedicated feedback endpoints that do NOT touch schedule status
      if (activeSession.value?.id) {
        await request.post(`/daily/${activeSession.value.id}/feedback`, payload)
      } else {
        // No linked schedule (edge case)
        await request.post('/daily/feedback', payload)
      }
    }
    ElMessage.success('反馈已记录至健康档案 🎉')
    checkInVisible.value = false
    await fetchStats()
  } catch(e) {
    ElMessage.error('提交失败，请重试')
  } finally {
    checkingIn.value = false
  }
}

// Global hook checking for "start" params directly from explore redirection
watch(() => route.query, (q) => {
  if (q.tab === 'favorites') fetchFavorites()
  if (q.tab === 'plans') {
     if (q.sub === 'courses') myTrainingTab.value = 'courses'
  }
  
  if (q.start && myCourses.value.length > 0) {
    const courseToStart = myCourses.value.find(c => String(c.id) === String(q.start))
    if (courseToStart) {
      myTrainingTab.value = 'courses'
      // Auto open immersive panel
      openImmersiveTraining(courseToStart, 'COURSE')
      // remove query to prevent loop
      router.replace({ query: { tab: 'plans' } })
    }
  }
}, { immediate: true })

// Watch date change to fetch new month data if needed
watch(selectedDate, (newVal, oldVal) => {
  if (newVal.getMonth() !== oldVal.getMonth()) {
    fetchMonthSchedules(newVal)
  }
})

watch(() => route.query.tab, (newTab) => {
  if (newTab === 'favorites') fetchFavorites()
}, { immediate: true })

onMounted(async () => {
  await fetchAllMyPlans()
  await fetchMyCourses()
  fetchMonthSchedules(selectedDate.value)
  fetchStats()
  
  // process start query param if exists post fetch
  if (route.query.start) {
     const courseToStart = myCourses.value.find(c => String(c.id) === String(route.query.start))
     if (courseToStart) {
       myTrainingTab.value = 'courses'
       openImmersiveTraining(courseToStart, 'COURSE')
       router.replace({ query: { tab: 'plans' } })
     }
  }
})

</script>

<style scoped>
.training-dashboard {
  display: flex;
  height: 100%;
  background: #F8FAFC;
}

.main-body {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.section-header h3 {
  font-size: 22px;
  margin: 0;
  color: #1E293B;
}

/* Calendar Layout */
.calendar-layout {
  display: flex;
  gap: 32px;
}
.calendar-left {
  flex: 2;
  min-width: 600px;
}
.calendar-right {
  flex: 1;
  min-width: 320px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.view-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #334155;
}

/* Calendar Styling adjustments */
.premium-calendar {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}
.cell-content {
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}
.day-text {
  font-weight: 600;
  color: #334155;
  margin-bottom: 4px;
  display: block;
  text-align: left;
  padding: 4px;
}
.cell-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 0 4px;
  flex: 1;
  overflow-y: auto;
}
.cell-events::-webkit-scrollbar { display: none; }
.schedule-event {
  font-size: 11px;
  padding: 3px 6px;
  border-radius: 4px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
  line-height: 1.2;
}
.schedule-event.pending { background: rgba(59, 130, 246, 0.85); }
.schedule-event.completed { background: rgba(16, 185, 129, 0.85); }
.schedule-event.skipped { background: rgba(148, 163, 184, 0.85); }
.schedule-event.missed { background: rgba(239, 68, 68, 0.85); }

.selected-date-display {
  font-size: 14px;
  color: #94A3B8;
  margin-bottom: 16px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 8px;
}

/* Task Card */
.task-card {
  padding: 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;
}
.tc-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.tc-title h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #1E293B;
}
.tc-status {
  font-size: 12px;
  font-weight: 700;
}
.status-PENDING { color: #3B82F6; }
.status-COMPLETED { color: #10B981; }
.status-SKIPPED { color: #94A3B8; }
.status-MISSED { color: #EF4444; font-weight: 800; }

.tc-desc {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 12px;
}

.actions-list {
  background: white;
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 16px;
}
.al-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  padding: 4px 0;
}

.tc-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.success-msg { color: #10B981; font-weight: 700; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.skipped-msg { color: #94A3B8; font-style: italic; font-size: 14px; }
.locked-msg {
  color: #94A3B8;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #F1F5F9;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px dashed #CBD5E1;
}
.future-msg {
  color: #3B82F6;
  font-size: 13px;
  background: #EFF6FF;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px dashed #BFDBFE;
}

/* General Grids */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.module-card {
  padding: 20px;
  transition: all 0.25s;
}
.card-title { font-size: 16px; font-weight: 800; color: #1E293B; margin: 0 0 8px; line-height: 1.4; }
.card-desc { font-size: 13px; color: #64748B; line-height: 1.5; margin: 0; }

.stats-module h4 {
  margin: 0 0 12px;
}
.stats-grid {
  display: flex;
  gap: 16px;
}
.stat-item {
  background: #F8FAFC;
  padding: 16px;
  border-radius: 8px;
  flex: 1;
  text-align: center;
}
.s-val { font-size: 24px; font-weight: 900; color: #3B82F6; }
.s-unit { font-size: 12px; font-weight: normal; color: #94A3B8; }
.s-lbl { font-size: 12px; color: #64748B; margin-top: 4px; }
.mt-4 { margin-top: 24px; }
.mt-3 { margin-top: 16px; }
.mt-2 { margin-top: 8px; }

/* Immersive Dialog Styling */
.immersive-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #E2E8F0;
  margin-right: 0;
}
.immersive-layout {
  display: flex;
  gap: 32px;
  min-height: 400px;
}
.immersive-left {
  flex: 2;
  border-right: 1px solid #E2E8F0;
  padding-right: 32px;
}
.immersive-right {
  flex: 1;
}

.action-check-list {
  margin-bottom: 24px;
}
.action-check-item {
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}
.action-check-item:hover { background: #F1F5F9; }
.ac-name { font-weight: 700; color: #1E293B; margin-left: 8px; font-size: 15px; }
.ac-sets { color: #64748B; font-size: 13px; margin-left: 12px; }

.progress-wrap {
  margin-top: auto;
}

.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F8FAFC;
  padding: 24px;
  border-radius: 12px;
}
.t-lbl { color: #64748B; font-size: 14px; margin-bottom: 8px; }
.t-val { font-size: 48px; font-weight: 900; color: #1E293B; font-variant-numeric: tabular-nums; line-height: 1; margin-bottom: 16px; font-family: 'Courier New', monospace; letter-spacing: -2px; }

.checkin-success-header {
  text-align: center;
  padding: 10px 0 20px;
}
.checkin-success-header p {
  margin: 4px 0 0;
  color: #64748B;
  font-size: 14px;
}
.success-text {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-top: 12px;
}
.tip-area {
  background: #F8FAFC;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #3B82F6;
}

</style>
