<template>
  <div class="explore-page">
    <!-- Top: Title + Search Bar -->
    <div v-if="activeModuleKey !== 'training' && activeModuleKey !== 'services'" class="explore-topbar">
        <div class="explore-title-area">
          <h2 class="module-title">{{ activeModule.name }}</h2>
          <p class="module-subtitle">{{ activeModule.subtitle }}</p>
        </div>
        <div class="search-area">
          <el-input
            v-model="searchQ"
            :placeholder="`搜索${activeModule.name}...`"
            class="explore-search"
            clearable
            @clear="clearSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button class="btn-cta btn-cta-primary" type="primary" round @click="handleSearch">搜索</el-button>
        </div>
    </div>

    <!-- Cross Layout Body -->
    <div class="explore-body">
      <!-- Removed Horizontal Nav, replaced with dropdown -->

      <!-- Main: Module Content Card Grid -->
      <main class="explore-main" v-loading="loading">
        <!-- Training Plans (default module) -->
        <template v-if="activeModuleKey === 'training'">
          <div class="training-library">
            <div class="header">
              <div class="header-left">
                <h2>{{ activeTrainingTab === 'plans' ? '训练计划' : '单次课程' }}</h2>
                <p>像动作库一样筛选与搜索，快速找到适合你的训练内容</p>
              </div>
              <div class="header-right">
                <el-input
                  v-model="searchQ"
                  :placeholder="activeTrainingTab === 'plans' ? '搜索训练计划...' : '搜索单次课程...'"
                  class="search-input"
                  clearable
                  @keyup.enter="handleSearch"
                  @clear="handleSearch"
                >
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </div>
            </div>

            <div class="filters-card premium-card">
              <template v-if="activeTrainingTab === 'plans'">
                <div class="filter-row">
                  <span class="filter-label">目标</span>
                  <div class="filter-options">
                    <el-tag v-for="g in planGoals" :key="g" :effect="filterForm.goal === g ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('goal', g)">{{ g }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">难度</span>
                  <div class="filter-options">
                    <el-tag v-for="d in planDifficulties" :key="d" :effect="filterForm.difficulty === d ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('difficulty', d)">{{ d }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">时长</span>
                  <div class="filter-options">
                    <el-tag v-for="t in planDurations" :key="t" :effect="filterForm.duration === t ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('duration', t)">{{ t }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">频率</span>
                  <div class="filter-options">
                    <el-tag v-for="f in planFrequencies" :key="f" :effect="filterForm.frequency === f ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('frequency', f)">{{ f }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">场景</span>
                  <div class="filter-options">
                    <el-tag v-for="s in planScenes" :key="s" :effect="filterForm.scene === s ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('scene', s)">{{ s }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">部位</span>
                  <div class="filter-options">
                    <el-tag v-for="p in planParts" :key="p" :effect="filterForm.part === p ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('part', p)">{{ p }}</el-tag>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="filter-row">
                  <span class="filter-label">分类</span>
                  <div class="filter-options">
                    <el-tag v-for="c in courseCategories" :key="c" :effect="filterForm.courseCategory === c ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('courseCategory', c)">{{ c }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">难度</span>
                  <div class="filter-options">
                    <el-tag v-for="d in planDifficulties" :key="d" :effect="filterForm.difficulty === d ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('difficulty', d)">{{ d }}</el-tag>
                  </div>
                </div>
                <div class="filter-row">
                  <span class="filter-label">时长</span>
                  <div class="filter-options">
                    <el-tag v-for="t in courseDurations" :key="t" :effect="filterForm.courseDuration === t ? 'dark' : 'plain'" class="filter-tag" @click="setFilter('courseDuration', t)">{{ t }}</el-tag>
                  </div>
                </div>
              </template>
            </div>

            <template v-if="activeTrainingTab === 'plans'">
              <div v-if="filteredPlans.length === 0 && !loading" class="empty-state">
                <el-empty description="没有找到匹配的训练计划" />
              </div>
              <div v-else class="exercise-grid">
                <div v-for="plan in filteredPlans" :key="plan.id" class="ex-card premium-card" @click="openDetail(plan)">
                  <div class="ex-image-placeholder">
                    <el-icon class="play-icon"><VideoCamera /></el-icon>
                    <div class="difficulty-badge" :class="plan.difficulty || '初级'">{{ plan.difficulty || '初级' }}</div>
                  </div>
                  <div class="ex-info">
                    <h3 class="ex-name">{{ plan.title }}</h3>
                    <div class="ex-tags">
                      <el-tag size="small" type="info">{{ plan.goal || '通用' }}</el-tag>
                      <el-tag size="small" type="info">{{ plan.scene || '居家' }}</el-tag>
                      <el-tag size="small" type="info">{{ plan.duration || '4周' }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <div v-if="filteredCourses.length === 0 && !loading" class="empty-state">
                <el-empty description="没有找到匹配的单次课程" />
              </div>
              <div v-else class="exercise-grid">
                <div v-for="course in filteredCourses" :key="course.id" class="ex-card premium-card" @click="openCourseDetail(course)">
                  <div class="ex-image-placeholder">
                    <el-icon class="play-icon"><VideoCamera /></el-icon>
                    <div class="difficulty-badge" :class="course.difficulty || '初级'">{{ course.difficulty || '初级' }}</div>
                  </div>
                  <div class="ex-info">
                    <h3 class="ex-name">{{ course.title }}</h3>
                    <div class="ex-tags">
                      <el-tag size="small" type="info">{{ course.category || '训练' }}</el-tag>
                      <el-tag size="small" type="info">{{ (course.durationMinutes || 30) + '分钟' }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </template>

        <!-- Other Health Modules -->
        <template v-else-if="activeModuleKey === 'services'">
          <div class="services-library">
            <div class="header">
              <div class="header-left">
                <h2>健康服务</h2>
                <p>选择一个服务，直接进入对话</p>
              </div>
            </div>
            <div class="exercise-grid">
              <div v-for="svc in serviceCards" :key="svc.key" class="ex-card premium-card" @click="openServiceChat(svc)">
                <div class="ex-image-placeholder">
                  <el-icon class="play-icon"><MagicStick /></el-icon>
                  <div class="difficulty-badge">{{ svc.styleLabel }}</div>
                </div>
                <div class="ex-info">
                  <h3 class="ex-name">{{ svc.title }}</h3>
                  <div class="ex-tags">
                    <el-tag size="small" type="info">{{ svc.tag }}</el-tag>
                    <el-tag size="small" type="info">进入对话</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="module-placeholder">
            <div class="placeholder-content premium-card">
              <div class="big-icon">{{ activeModule.icon }}</div>
              <h2>{{ activeModule.name }}</h2>
                            <p>{{ activeModule.description }}</p>
              <el-tag type="info" round>即将上线</el-tag>
            </div>
          </div>
        </template>
      </main>
    </div>

    <!-- Unified Training Detail Dialogue -->
    <el-dialog 
      v-model="showPlanDetail" 
      width="1100px" 
      style="border-radius: 24px; overflow: hidden;"
      class="premium-resource-dialog" 
      align-center 
      destroy-on-close 
    >
      <div v-if="detailedPlan">
        <TrainingResourceViewer
          :item="detailedPlan"
          :type="detailedPlan.isCourse ? 'course' : 'plan'"
        >
          <template #left-actions>
            <div class="integrated-actions">
              <el-button 
                class="btn-main" 
                type="primary" 
                size="large" 
                :disabled="detailedPlan.isCourse ? isCourseSubscribed(detailedPlan) : isSubscribed(detailedPlan)"
                @click="detailedPlan.isCourse ? openCourseSchedule(detailedPlan) : openSubscribeConfig(detailedPlan)"
              >
                {{ (detailedPlan.isCourse ? isCourseSubscribed(detailedPlan) : isSubscribed(detailedPlan)) ? '✓ 已安排' : (detailedPlan.isCourse ? '立即开始训练课程' : '立即加入计划') }}
              </el-button>
              
              <el-button 
                class="btn-sec" 
                :type="isCollected(detailedPlan) ? 'warning' : 'info'" 
                plain
                circle
                size="large"
                @click="toggleCollect(detailedPlan)"
              >
                <el-icon><Star v-if="!isCollected(detailedPlan)" /><StarFilled v-else /></el-icon>
              </el-button>
            </div>
          </template>
        </TrainingResourceViewer>
      </div>
    </el-dialog>

    <!-- Subscribe Config Dialog -->
    <el-dialog v-model="subscribeDialogVisible" title="配置您的专属计划" width="450px" align-center>
      <el-form :model="subscribeForm" label-position="top">
        <el-form-item label="开始日期">
          <el-date-picker 
            v-model="subscribeForm.startDate" 
            type="date" 
            placeholder="选择开始日期" 
            style="width: 100%" 
            :disabled-date="(time: Date) => time.getTime() < Date.now() - 8.64e7"
          />
        </el-form-item>
        <el-form-item label="建议与安排：" style="margin-bottom: 24px;">
          <div style="background: #F8FAFC; padding: 12px; border-radius: 8px; font-size: 13px; color: #64748B; width: 100%;">
            系统依据计划强度，建议 <strong>{{ targetSubscribePlan?.frequency || '每周 3 天' }}</strong> 的训练频率。已为您默认划定最佳训练日，您可以根据自己的作息进行微调。
          </div>
        </el-form-item>
        <el-form-item label="微调每周训练日" style="margin-bottom: 24px;">
          <el-checkbox-group v-model="subscribeForm.weeklyDays">
            <el-checkbox label="MONDAY">周一</el-checkbox>
            <el-checkbox label="TUESDAY">周二</el-checkbox>
            <el-checkbox label="WEDNESDAY">周三</el-checkbox>
            <el-checkbox label="THURSDAY">周四</el-checkbox>
            <el-checkbox label="FRIDAY">周五</el-checkbox>
            <el-checkbox label="SATURDAY">周六</el-checkbox>
            <el-checkbox label="SUNDAY">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="排期概览 (预览未来四周)">
          <el-calendar v-model="subscribeForm.startDate" class="mini-preview-cal">
            <template #date-cell="{ data }">
              <div class="cal-cell-inner" :class="{ 'is-training-day': isTrainingDayPreview(data.day) }">
                <span class="cal-day-num">{{ data.day.split('-').pop() }}</span>
                <span v-if="isTrainingDayPreview(data.day)" class="cal-dot"></span>
              </div>
            </template>
          </el-calendar>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subscribeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubscribe">生成个人计划实例</el-button>
      </template>
    </el-dialog>
    <!-- Course Schedule Dialog -->
    <el-dialog v-model="courseScheduleDialogVisible" title="预约训练日期" width="450px" align-center>
      <el-form label-position="top">
        <el-form-item label="选择预约日期（可多选）">
          <el-date-picker
            v-model="courseScheduleDates"
            type="dates"
            placeholder="选择一个或多个日期"
            style="width: 100%"
            :disabled-date="(time: Date) => time.getTime() < Date.now() - 8.64e7"
          />
        </el-form-item>
        <div style="background: #F0FDFA; padding: 12px; border-radius: 8px; font-size: 13px; color: #0D9488;">
          <el-icon><InfoFilled /></el-icon> 该课程将作为单次任务添加至您所选日期的训练日历中。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="courseScheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCourseSchedule" :loading="schedulingCourse">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Plus, Calendar, Timer, User, RefreshRight, Location, UserFilled, Aim, MagicStick, VideoCamera, Star, StarFilled, InfoFilled, List } from '@element-plus/icons-vue'
import TrainingResourceViewer from '../components/TrainingResourceViewer.vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const route = useRoute()

const healthModules = [
  { key: 'training', name: '运动训练', icon: '🏋️', subtitle: '发现并订阅专业训练计划', description: '包含各类有氧、力量、柔韧性训练计划' },
  { key: 'services', name: '健康服务', icon: '🩺', subtitle: '为你提供专业健康支持', description: '健康咨询、康复评估、营养指导等服务入口即将上线' },
  { key: 'diet', name: '饮食管理', icon: '🥗', subtitle: '科学的饮食建议与营养知识', description: '卡路里追踪、营养均衡、饮食计划制定' },
  { key: 'sleep', name: '睡眠管理', icon: '😴', subtitle: '改善睡眠质量与作息规律', description: '睡眠数据分析、助眠建议、昼夜节律调整' },
  { key: 'weight', name: '体重管理', icon: '⚖️', subtitle: '科学减重或增重计划', description: 'BMI追踪、体脂率分析、体重趋势预测' },
  { key: 'heart', name: '心率血压', icon: '❤️', subtitle: '心血管健康监测与管理', description: '心率追踪、血压记录、心血管风险评估' },
  { key: 'mental', name: '心理健康', icon: '🧘', subtitle: '心理健康评估与正念练习', description: '压力管理、情绪追踪、冥想引导练习' },
]

const serviceCards = [
  {
    key: 'mental_counseling',
    title: '心理咨询',
    description: '情绪疏导、压力管理、正念引导，让你更稳定地面对当下。',
    presetKey: 'mental_counseling',
    presetTitle: '心理咨询（情绪疏导）',
    styleLabel: '温和共情风',
    tag: '情绪支持',
    tagColor: '#60A5FA'
  },
  {
    key: 'fitness_coach',
    title: '健身带教',
    description: '训练指导、动作要点、安全替代方案，按你的目标做可执行计划。',
    presetKey: 'fitness_coach',
    presetTitle: '健身带教（训练指导）',
    styleLabel: '科学教练风',
    tag: '训练指导',
    tagColor: '#34D399'
  },
  {
    key: 'rehab_coach',
    title: '康复训练指导',
    description: '循序渐进、安全边界、何时就医的提醒，让恢复更稳。',
    presetKey: 'rehab_coach',
    presetTitle: '康复评估（循序恢复）',
    styleLabel: '循序渐进风',
    tag: '恢复管理',
    tagColor: '#F59E0B'
  },
  {
    key: 'nutrition_coach',
    title: '营养指导',
    description: '饮食策略、替换建议与可坚持的规划，理性不夸张。',
    presetKey: 'nutrition_coach',
    presetTitle: '营养指导（饮食策略）',
    styleLabel: '理性规划风',
    tag: '饮食策略',
    tagColor: '#A78BFA'
  }
]

const activeModuleKey = ref('training')
const activeModule = computed(() => healthModules.find(m => m.key === activeModuleKey.value) || healthModules[0])

const loading = ref(false)
const plans = ref<any[]>([])
const myPlans = ref<any[]>([])

const activeTrainingTab = computed(() => (route.query.tab as string) || 'plans')
const courses = ref<any[]>([])
const myCourses = ref<any[]>([])

const showPlanDetail = ref(false)
const detailedPlan = ref<any>(null)
const searchQ = ref('')

const subscribeDialogVisible = ref(false)
const targetSubscribePlan = ref<any>(null)
const intentActive = ref(false)
const subscribeForm = reactive({
  startDate: new Date(),
  weeklyDays: ['MONDAY', 'WEDNESDAY', 'FRIDAY'] // Default Mon/Wed/Fri
})

const courseScheduleDialogVisible = ref(false)
const courseScheduleDates = ref<Date[]>([])
const schedulingCourse = ref(false)
const targetScheduleCourse = ref<any>(null)

const planGoals = ['全部', '减脂', '增肌', '体能', '康复']
const planDifficulties = ['全部', '初级', '中级', '高级']
const planDurations = ['全部', '2周', '4周', '8周']
const planFrequencies = ['全部', '每周2-3天', '每周4-5天', '每周6+天']
const planScenes = ['全部', '居家', '健身房', '无器械']
const planParts = ['全部', '全身', '核心', '上肢', '下肢']
const courseDurations = ['全部', '≤15分钟', '15-30分钟', '30-45分钟', '≥45分钟']

const filterForm = reactive({
  goal: '全部',
  difficulty: '全部',
  duration: '全部',
  frequency: '全部',
  scene: '全部',
  part: '全部',
  courseCategory: '全部',
  courseDuration: '全部'
})

const setFilter = (key: keyof typeof filterForm, val: string) => {
  filterForm[key] = val as any
}

const courseCategories = computed(() => {
  const set = new Set<string>()
  for (const c of courses.value) {
    if (c?.category) set.add(String(c.category))
  }
  return ['全部', ...Array.from(set)]
})

const defaultWeekPreview = [
  { name: 'Day 1', desc: '上肢力量', type: '训练' },
  { name: 'Day 2', desc: '有氧训练', type: '训练' },
  { name: 'Day 3', desc: '休息恢复', type: '休息' },
  { name: 'Day 4', desc: '下肢训练', type: '训练' },
  { name: 'Day 5', desc: '核心增强', type: '训练' },
  { name: 'Day 6', desc: '全身拉伸', type: '训练' },
  { name: 'Day 7', desc: '休息恢复', type: '休息' },
]

const switchModuleByKey = (key: string) => {
  activeModuleKey.value = key
  if (key === 'training') {
    fetchPlans()
    fetchMyPlans()
    fetchCourses()
    fetchMyCourses()
  }
}

const syncModuleWithRouteTab = (tab?: string) => {
  if (tab === 'services') {
    activeModuleKey.value = 'services'
  } else if (tab === 'courses' || tab === 'plans') {
    activeModuleKey.value = 'training'
  } else {
    // Default to training if no tab is provided
    activeModuleKey.value = 'training'
  }
}

const openServiceChat = (svc: any) => {
  router.push({
    path: '/app/explore/webai',
    query: {
      preset: svc.presetKey,
      style: svc.styleLabel
    }
  })
}

const fetchMyPlans = async () => {
  try {
    const res: any = await request.get('/training/list')
    myPlans.value = res.data || []
  } catch(e) {}
}

// Collections logic
const collectedPlanIds = ref<number[]>([])
const collectedCourseIds = ref<number[]>([])

const fetchCollections = async () => {
  try {
    const resPlan: any = await request.get('/interaction/collections?type=PLAN')
    collectedPlanIds.value = resPlan.data.map((c: any) => c.targetId)
    
    const resCourse: any = await request.get('/interaction/collections?type=COURSE')
    collectedCourseIds.value = resCourse.data.map((c: any) => c.targetId)
  } catch(e) {}
}

const isCollected = (item: any) => {
  if (item.isCourse || item.actionsJson) {
      return collectedCourseIds.value.includes(item.id)
  }
  return collectedPlanIds.value.includes(item.id)
}

const toggleCollect = async (item: any) => {
  const isCourseType = item.isCourse || item.actionsJson
  const targetType = isCourseType ? 'COURSE' : 'PLAN'
  const targetIdList = isCourseType ? collectedCourseIds : collectedPlanIds
  
  try {
    if (isCollected(item)) {
      await request.delete(`/interaction/collect?targetId=${item.id}&targetType=${targetType}`)
      targetIdList.value = targetIdList.value.filter(id => id !== item.id)
      ElMessage.success(isCourseType ? '已移出收藏' : '已移出想练')
    } else {
      await request.post('/interaction/collect', { targetId: item.id, targetType: targetType })
      targetIdList.value.push(item.id)
      ElMessage.success(isCourseType ? '收藏成功' : '已加入想练单')
    }
  } catch (e) {}
}

const getMyPlanMatch = (plan: any) => {
  return myPlans.value.find(p => p.sourceId === plan.id || p.title === plan.title)
}

const isSubscribed = (plan: any) => {
  const match = getMyPlanMatch(plan)
  return match && match.isSubscribed !== false && (match.status === 'ACTIVE' || match.status === 'PLANNING')
}

const isActive = (plan: any) => {
  const match = getMyPlanMatch(plan)
  return match && match.status === 'ACTIVE'
}


const parsedArrangement = computed(() => {
  if (!detailedPlan.value) return []
  if (detailedPlan.value.isCourse) {
     return parseActions(detailedPlan.value.actionsJson || detailedPlan.value.actions)
  } else {
     return parseActions(detailedPlan.value.actions)
  }
})

// Insert before fetchPlans

const fetchPlans = async () => {
  loading.value = true
  try {
    const endpoint = '/training/library'
    const res: any = await request.get(endpoint, {
      params: searchQ.value ? { keyword: searchQ.value } : {}
    })
    
    // Inject mock data for plan structure if missing attributes to showcase the new design
    plans.value = res.data.map((p: any) => ({
      ...p,
      goal: p.category && p.category.includes('减脂') ? '减脂' : (p.category && p.category.includes('增肌') ? '增肌' : '体能'),
      difficulty: '初级',
      duration: '4周',
      frequency: '每周4天',
      scene: '居家',
      audience: '新手适合'
    }))
  } catch (e) {} finally {
    loading.value = false
  }
}

const filteredPlans = computed(() => {
  return plans.value.filter(p => {
    if (filterForm.goal !== '全部' && p.goal !== filterForm.goal) return false
    if (filterForm.difficulty !== '全部' && p.difficulty !== filterForm.difficulty) return false
    if (filterForm.duration !== '全部' && p.duration !== filterForm.duration) return false
    if (filterForm.frequency !== '全部' && p.frequency !== filterForm.frequency) return false
    if (filterForm.scene !== '全部' && p.scene !== filterForm.scene) return false
    if (filterForm.part !== '全部' && p.part && p.part !== filterForm.part) return false
    return true
  })
})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/course/library', {
      params: searchQ.value ? { keyword: searchQ.value } : {}
    })
    courses.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const fetchMyCourses = async () => {
  try {
    const res: any = await request.get('/course/my')
    myCourses.value = res.data || []
  } catch(e) {}
}

const isCourseSubscribed = (course: any) => {
  return myCourses.value.some(c => c.title === course.title)
}

const filteredCourses = computed(() => {
  return courses.value.filter(c => {
    if (filterForm.courseCategory !== '全部' && String(c?.category || '') !== filterForm.courseCategory) return false
    if (filterForm.difficulty !== '全部' && String(c?.difficulty || '') !== filterForm.difficulty) return false
    if (filterForm.courseDuration !== '全部') {
      const mins = Number(c?.durationMinutes || 0)
      if (filterForm.courseDuration === '≤15分钟' && !(mins <= 15)) return false
      if (filterForm.courseDuration === '15-30分钟' && !(mins > 15 && mins <= 30)) return false
      if (filterForm.courseDuration === '30-45分钟' && !(mins > 30 && mins <= 45)) return false
      if (filterForm.courseDuration === '≥45分钟' && !(mins >= 45)) return false
    }
    return true
  })
})

const handleSearch = () => {
  fetchPlans()
  fetchCourses()
}
const clearSearch = () => { searchQ.value = ''; handleSearch() }

const isTrainingDayPreview = (dateStr: string) => {
  const d = new Date(dateStr)
  d.setHours(0,0,0,0)
  const start = new Date(subscribeForm.startDate)
  start.setHours(0,0,0,0)
  
  if (d.getTime() < start.getTime()) return false;
  // Let's assume default 28 days for preview
  if (d.getTime() > start.getTime() + 27 * 86400000) return false;
  
  const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
  return subscribeForm.weeklyDays.includes(dayNames[d.getDay()]);
}

const openSubscribeConfig = (plan: any) => {
  targetSubscribePlan.value = plan
  subscribeDialogVisible.value = true
  
  // Set default weekly days based on frequency heuristic
  const freq = plan.frequency || ''
  if (freq.includes('2') || freq.includes('3')) {
      subscribeForm.weeklyDays = ['MONDAY', 'WEDNESDAY', 'FRIDAY']
  } else if (freq.includes('5') || freq.includes('6')) {
      subscribeForm.weeklyDays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']
  } else {
      subscribeForm.weeklyDays = ['MONDAY', 'WEDNESDAY', 'FRIDAY', 'SUNDAY']
  }
}

const confirmSubscribe = async () => {
  try {
    await request.post(`/training/subscribe/${targetSubscribePlan.value.id}`, {
      startDate: subscribeForm.startDate,
      weeklyDays: subscribeForm.weeklyDays,
      activate: true // The user wants to set it as active immediately and go to Training
    })
    ElMessage.success('已成功加入训练计划！')
    subscribeDialogVisible.value = false
    showPlanDetail.value = false
    router.push('/app/training')
  } catch (e) {}
}

const openDetail = (plan: any) => {
  detailedPlan.value = plan
  showPlanDetail.value = true
}

const openCourseDetail = (course: any) => {
  // We can reuse the same modal for preview with slight logic adjustments
  detailedPlan.value = {
    ...course,
    isCourse: true,
    duration: `${course.durationMinutes} 分钟`,
    frequency: '单次训练',
    audience: '自由安排',
    goal: course.category
  }
  showPlanDetail.value = true
}

const subscribeCourse = async (course: any) => {
  try {
    // Legacy support, but we now prefer scheduling to daily
    await request.post(`/course/subscribe/${course.id}`)
    ElMessage.success('已加入您的个人课程库！')
    fetchMyCourses()
  } catch (e) {}
}

const openCourseSchedule = (course: any) => {
  targetScheduleCourse.value = course
  courseScheduleDates.value = []
  courseScheduleDialogVisible.value = true
}

const confirmCourseSchedule = async () => {
  if (courseScheduleDates.value.length === 0) {
    ElMessage.warning('请至少选择一个日期')
    return
  }
  schedulingCourse.value = true
  try {
    // Ensure the course exists in user's personal course library
    const subRes: any = await request.post(`/course/subscribe/${targetScheduleCourse.value.id}`)
    const courseCloneId = subRes?.data ?? targetScheduleCourse.value.id
    const formattedDates = courseScheduleDates.value.map(d => {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    })
    await request.post('/daily/course', {
      courseId: courseCloneId,
      dates: formattedDates
    })
    ElMessage.success('已成功预约，在日历中查看！')
    courseScheduleDialogVisible.value = false
    showPlanDetail.value = false
    router.push('/app/training')
  } catch (e) {} finally {
    schedulingCourse.value = false
  }
}

// Helpers for preview
const parseTags = (str: string) => str ? str.split(',') : ['综合']
const parseActions = (jsonStr: string) => {
  if (!jsonStr) return []
  try {
    const res = JSON.parse(jsonStr)
    return Array.isArray(res) ? res : []
  } catch (e) { return [] }
}
const getGoalColor = (goal: string) => {
  switch (goal) {
    case '减脂': return '#f59e0b';
    case '增肌': return '#ef4444';
    case '体能': return '#3b82f6';
    case '康复': return '#10b981';
    default: return '#64748b';
  }
}
const getCardTheme = (cateStr: string) => {
   if (!cateStr) return 'bg-1'
   return `bg-${(cateStr.length % 4) + 1}`
}

onMounted(() => {
  fetchMyPlans()
  fetchMyCourses()
  fetchCollections()
})

watch(() => route.query.tab, (newTab) => {
  syncModuleWithRouteTab(newTab as string | undefined)
  if (newTab === 'plans' || !newTab) {
    fetchPlans()
  } else if (newTab === 'courses') {
    fetchCourses()
  }
}, { immediate: true })
</script>

<style scoped>
.explore-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* Top Bar */
.explore-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  background:
    radial-gradient(at 0% 0%, rgba(56, 189, 248, 0.14) 0px, transparent 55%),
    radial-gradient(at 100% 0%, rgba(251, 146, 60, 0.12) 0px, transparent 55%),
    rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid #F1F5F9;
  flex-shrink: 0;
  gap: 24px;
}

.explore-title-area { min-width: 160px; }
.module-title { font-size: 20px; font-weight: 900; color: #1E293B; margin: 0 0 2px; }
.module-subtitle { font-size: 13px; color: #94A3B8; margin: 0; }

.header-left h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 1000;
  color: #0F172A;
}
.header-left p {
  margin: 0;
  color: #64748B;
  font-size: 14px;
}
.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-input {
  width: 360px;
  max-width: 44vw;
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
  max-width: 480px;
}

.explore-search :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* Layout */
.explore-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

/* Removed Nav pill styles */

/* Main */
.explore-main {
  padding: 0;
  overflow-y: auto;
  background: transparent;
}

.training-library,
.services-library {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h2 { margin: 0 0 8px; color: #1e293b; }
.header p { margin: 0; color: #64748b; font-size: 14px; }
.header-right { display: flex; gap: 12px; }

.search-input {
  width: 360px;
  max-width: 44vw;
}

.filters-card {
  padding: 16px 24px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.filter-label {
  font-weight: 600;
  color: #475569;
  width: 48px;
  flex-shrink: 0;
  line-height: 28px;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.filter-tag {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 100px;
  padding: 0 16px;
}

.exercise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.ex-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.ex-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.ex-image-placeholder {
  height: 140px;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.play-icon {
  font-size: 48px;
  color: white;
  opacity: 0.8;
}

.difficulty-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  background: #3b82f6;
}
.difficulty-badge.高级 { background: #ef4444; }
.difficulty-badge.初级 { background: #10b981; }

.ex-info {
  padding: 16px;
  flex: 1;
}

.ex-name {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.ex-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-state {
  padding: 60px 0;
}

.mode-header {
  margin-bottom: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 800;
  color: #1E293B;
  margin: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.module-card {
  padding: 20px;
  transition: all 0.25s;
  cursor: pointer;
}

.module-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 32px rgba(0,0,0,0.1);
}

.card-header-tags { display: flex; gap: 6px; margin-bottom: 12px; }
.card-title { font-size: 16px; font-weight: 800; color: #1E293B; margin: 0 0 8px; line-height: 1.4; }
.card-desc { font-size: 13px; color: #64748B; line-height: 1.5; margin: 0 0 12px; }

.plan-specs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 16px;
}
.spec-item {
  font-size: 12px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions { display: flex; gap: 8px; }

.module-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60%;
  padding: 24px;
}

.placeholder-content {
  text-align: center;
  padding: 48px;
  max-width: 360px;
}

.big-icon { font-size: 64px; margin-bottom: 16px; }
.placeholder-content h2 { color: #1E293B; margin: 0 0 8px; }
.placeholder-content p { color: #64748B; margin: 0 0 16px; font-size: 14px; line-height: 1.6; }

.empty-module { padding: 40px 0; text-align: center; }

/* Rich Preview Dialog Styles */
.preview-dialog :deep(.el-dialog__header) { 
  display: block; 
  padding: 0;
  margin: 0;
  border-bottom: none;
}
.preview-dialog :deep(.el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
  z-index: 10;
}
.preview-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 24px;
  text-shadow: 0 1px 4px rgba(0,0,0,0.3);
}
.preview-dialog :deep(.el-dialog__body) { padding: 0; }
.preview-header {
  padding: 32px; color: white; border-radius: 8px 8px 0 0;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); /* fallback */
}
.bg-1.preview-header { background: linear-gradient(135deg, #FF9A9E 0%, #FECFEF 100%); }
.bg-2.preview-header { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
.bg-3.preview-header { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
.bg-4.preview-header { background: linear-gradient(135deg, #fccb90 0%, #d57eeb 100%); }

.preview-tags { margin-bottom: 12px; }
.preview-tag {
  background: rgba(255,255,255,0.3); padding: 4px 10px;
  border-radius: 20px; font-size: 12px; font-weight: 600; margin-right: 8px;
}
.preview-header h2 { margin: 0 0 8px; font-size: 28px; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.preview-meta { display: flex; gap: 16px; opacity: 0.9; font-size: 14px; }
.preview-meta span { display: flex; align-items: center; gap: 6px; }

.preview-body { padding: 24px; max-height: 50vh; overflow-y: auto; background: #fff; }
.section-title { font-size: 16px; font-weight: 800; color: #1E293B; margin-bottom: 12px; margin-top: 16px; display: flex; align-items: center; justify-content: space-between; }
.section-title:first-child { margin-top: 0; }
.preview-desc { color: #475569; line-height: 1.6; margin-bottom: 16px; background: #F8FAFC; padding: 16px; border-radius: 8px; }

.week-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.day-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #F8FAFC;
  border-radius: 6px;
}
.day-label {
  font-weight: 700;
  color: #334155;
  font-size: 13px;
  width: 48px;
  flex-shrink: 0;
}
.day-content {
  font-size: 13px;
  color: #475569;
}
.day-content.is-rest {
  color: #94A3B8;
  font-style: italic;
}

.demo-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.demo-action-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
  border: 1px solid #F1F5F9;
}
.d-placeholder {
  width: 48px;
  height: 48px;
  background: #E2E8F0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94A3B8;
  font-size: 24px;
}
.d-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.d-name {
  font-weight: 600;
  font-size: 14px;
  color: #1E293B;
  margin-bottom: 4px;
}
.d-sets {
  font-size: 12px;
  color: #64748B;
}

.preview-footer { display: flex; justify-content: center; gap: 16px; padding: 20px; border-top: 1px solid #F1F5F9; background: #fff; border-radius: 0 0 8px 8px; }

/* Mini Preview Calendar */
.mini-preview-cal {
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
}
.mini-preview-cal :deep(.el-calendar__header) {
  padding: 8px 12px;
}
.mini-preview-cal :deep(.el-calendar__body) {
  padding: 0 12px 12px;
}
.mini-preview-cal :deep(.el-calendar-table .el-calendar-day) {
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cal-cell-inner {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}
.is-training-day {
  background: rgba(16, 185, 129, 0.1);
  border-radius: 4px;
}
.is-training-day .cal-day-num {
  font-weight: bold;
  color: #10B981;
}
.cal-dot {
  width: 4px;
  height: 4px;
  background: #10B981;
  border-radius: 50%;
  position: absolute;
  bottom: 4px;
}

/* Detailed Viewer Styles */
.actionable-day {
  display: flex !important;
  gap: 16px;
  background: white;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  padding: 16px !important;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.day-num-badge {
  font-weight: 800;
  color: #3b82f6;
  background: #eff6ff;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 13px;
  height: fit-content;
}
.day-content.flex-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.day-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}
.day-title.is-rest { color: #10b981; }
.mini-action-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.mini-action-badge {
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
}
/* Course Timeline Styles */
.course-timeline-preview {
  margin-top: 16px;
  padding-left: 8px;
}
.c-action-node {
  display: flex;
  background: white;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  padding: 16px;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.ca-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ca-details { flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 4px;}
.ca-name { font-weight: 700; color: #1e293b; font-size: 16px; }
.ca-specs { display: flex; gap: 12px; font-size: 13px; color: #64748b; align-items: center; }
.ca-sets { font-weight: 600; color: #334155; }
.ca-rest { display: flex; align-items: center; gap: 4px; color: #f59e0b; }

/* Premium Training Detail Dialog Styles */
:deep(.premium-resource-dialog) {
  background: white;
  border-radius: 24px;
  overflow: hidden;
}

:deep(.premium-resource-dialog .el-dialog__header) {
  display: none;
}

:deep(.premium-resource-dialog .el-dialog__body) {
  padding: 0 !important;
}

.training-detail-container {
  display: flex;
  flex-direction: column;
  height: 80vh;
  max-height: 850px;
  background: white;
}

.td-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.td-media-side {
  flex: 1.3;
  background: #0f172a;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.media-window {
  position: relative;
  flex: 1;
  background: #1e293b;
  border-radius: 24px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20px 50px rgba(0,0,0,0.3);
}

.main-demo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #475569;
}

.floating-stats {
  position: absolute;
  bottom: 24px;
  left: 24px;
  display: flex;
  gap: 12px;
}

/* Premium Training Detail Dialog Styles */
:deep(.premium-resource-dialog) {
  background: white;
  border-radius: 24px;
  overflow: hidden;
}

:deep(.premium-resource-dialog .el-dialog__header) {
  display: none;
}

:deep(.premium-resource-dialog .el-dialog__body) {
  padding: 0 !important;
}

/* Integrated Actions inside TRV Left Side */
.integrated-actions {
  display: flex;
  gap: 16px;
  margin-top: auto;
  padding-top: 40px;
}

.btn-main {
  flex: 1;
  height: 52px !important;
  font-weight: 800 !important;
  font-size: 15px !important;
  border-radius: 14px !important;
  background: #3b82f6 !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1) !important;
}

.btn-main:hover {
  background: #2563eb !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3) !important;
}

.btn-sec {
  width: 52px !important;
  height: 52px !important;
  border-radius: 14px !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  background: rgba(255, 255, 255, 0.05) !important;
  color: white !important;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s !important;
}

.btn-sec:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
}

/* Responsive */
@media (max-width: 1024px) {
  :deep(.premium-resource-dialog) {
     width: 95% !important;
  }
}
</style>
