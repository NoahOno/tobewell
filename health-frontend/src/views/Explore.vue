<template>
  <div class="explore-page">
    <!-- Top: Title + Search Bar -->
    <div class="explore-topbar">
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
        <el-button type="primary" round @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <!-- Cross Layout Body -->
    <div class="explore-body">
      <!-- Removed Horizontal Nav, replaced with dropdown -->

      <!-- Main: Module Content Card Grid -->
      <main class="explore-main" v-loading="loading">
        <!-- Training Plans (default module) -->
        <template v-if="activeModuleKey === 'training'">
          <div class="library-toolbar">
            <div style="display: flex; gap: 16px; align-items: center;">
              <h3 class="section-title">健身运动库 | {{ activeTrainingTab === 'plans' ? '系列计划' : '单次课程' }}</h3>
            </div>
            <!-- Expandable Filters -->
            <div class="filters-container">
              <el-select v-model="activeModuleKey" placeholder="类别" size="small" class="filter-item" @change="switchModuleByKey">
                <el-option v-for="mod in healthModules" :key="mod.key" :label="mod.name" :value="mod.key" />
              </el-select>
              <template v-if="activeModuleKey === 'training'">
                <el-select v-model="filterForm.goal" placeholder="目标" clearable size="small" class="filter-item">
                  <el-option label="减脂" value="减脂" />
                  <el-option label="增肌" value="增肌" />
                  <el-option label="体能" value="体能" />
                  <el-option label="康复" value="康复" />
                </el-select>
                <el-select v-model="filterForm.difficulty" placeholder="难度" clearable size="small" class="filter-item">
                  <el-option label="初级" value="初级" />
                  <el-option label="中级" value="中级" />
                  <el-option label="高级" value="高级" />
                </el-select>
                <el-select v-model="filterForm.duration" placeholder="时长" clearable size="small" class="filter-item">
                  <el-option label="2周" value="2周" />
                  <el-option label="4周" value="4周" />
                  <el-option label="8周" value="8周" />
                </el-select>
                <el-select v-model="filterForm.frequency" placeholder="频率" clearable size="small" class="filter-item">
                  <el-option label="每周2-3天" value="每周2-3天" />
                  <el-option label="每周4-5天" value="每周4-5天" />
                  <el-option label="每周6+天" value="每周6+天" />
                </el-select>
                <el-select v-model="filterForm.scene" placeholder="场景" clearable size="small" class="filter-item">
                  <el-option label="居家" value="居家" />
                  <el-option label="健身房" value="健身房" />
                  <el-option label="无器械" value="无器械" />
                </el-select>
                <el-select v-model="filterForm.part" placeholder="部位" clearable size="small" class="filter-item">
                  <el-option label="全身" value="全身" />
                  <el-option label="核心" value="核心" />
                  <el-option label="上肢" value="上肢" />
                  <el-option label="下肢" value="下肢" />
                </el-select>
              </template>
            </div>
          </div>

          <!-- Switch between Plans Grid and Courses Grid -->
          <template v-if="activeTrainingTab === 'plans'">
            <div v-if="filteredPlans.length === 0 && !loading" class="empty-module">
              <el-empty description="暂无符合条件的计划" />
            </div>

            <div class="card-grid">
              <div v-for="plan in filteredPlans" :key="plan.id" class="module-card premium-card training-card" @click="openDetail(plan)">
                <div class="card-header-tags">
                  <el-tag v-if="isActive(plan)" size="small" type="success" effect="dark" style="border:none">正在训练</el-tag>
                  <el-tag v-if="plan.goal" size="small" effect="dark" :color="getGoalColor(plan.goal)" style="border:none">{{ plan.goal }}</el-tag>
                  <el-tag v-if="plan.difficulty" size="small" type="info">{{ plan.difficulty }}</el-tag>
                </div>
                <h3 class="card-title">{{ plan.title }}</h3>
                <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
                
                <div class="plan-specs">
                  <div class="spec-item"><el-icon><Calendar /></el-icon> {{ plan.duration || '4周' }}</div>
                  <div class="spec-item"><el-icon><RefreshRight /></el-icon> {{ plan.frequency || '每周4天' }}</div>
                  <div class="spec-item"><el-icon><Location /></el-icon> {{ plan.scene || '居家' }}</div>
                  <div class="spec-item"><el-icon><UserFilled /></el-icon> {{ plan.audience || '新手适合' }}</div>
                </div>

                <div class="card-actions">
                  <el-button size="small" :type="isCollected(plan) ? 'warning' : 'default'" @click.stop="toggleCollect(plan)">
                    <el-icon><Star v-if="!isCollected(plan)" /><StarFilled v-else /></el-icon> {{ isCollected(plan) ? '已想练' : '想练' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    round
                    :disabled="isSubscribed(plan)"
                    @click.stop="openSubscribeConfig(plan)"
                  >
                    {{ isSubscribed(plan) ? '已加入训练' : '加入训练计划' }}
                  </el-button>
                </div>
              </div>
            </div>
          </template>

          <template v-else-if="activeTrainingTab === 'courses'">
            <div v-if="filteredCourses.length === 0 && !loading" class="empty-module">
              <el-empty description="暂无符合条件的单次课程" />
            </div>

            <div class="card-grid">
              <div v-for="course in filteredCourses" :key="course.id" class="module-card premium-card training-card" @click="openCourseDetail(course)">
                <div class="card-header-tags">
                  <el-tag v-if="isCourseSubscribed(course)" size="small" type="success" effect="dark" style="border:none">我的课程</el-tag>
                  <el-tag v-if="course.category" size="small" effect="dark" color="#10b981" style="border:none">{{ course.category }}</el-tag>
                  <el-tag v-if="course.difficulty" size="small" type="info">{{ course.difficulty }}</el-tag>
                </div>
                <h3 class="card-title">{{ course.title }}</h3>
                <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
                
                <div class="plan-specs">
                  <div class="spec-item"><el-icon><Timer /></el-icon> {{ course.durationMinutes }} 分钟</div>
                  <div class="spec-item"><el-icon><Location /></el-icon> 自由开练</div>
                </div>

                <div class="card-actions">
                  <el-button size="small" :type="isCollected(course) ? 'warning' : 'default'" @click.stop="toggleCollect(course)">
                    <el-icon><Star v-if="!isCollected(course)" /><StarFilled v-else /></el-icon> {{ isCollected(course) ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    round
                    :disabled="isCourseSubscribed(course)"
                    @click.stop="openCourseSchedule(course)"
                  >
                    {{ isCourseSubscribed(course) ? '已加入训练' : '加入训练计划' }}
                  </el-button>
                </div>
              </div>
            </div>
          </template>
        </template>

        <!-- Other Health Modules -->
        <template v-else-if="activeModuleKey === 'services'">
          <div class="card-grid">
            <div
              v-for="svc in serviceCards"
              :key="svc.key"
              class="module-card premium-card training-card service-card"
              @click="openServiceChat(svc)"
            >
              <div class="card-header-tags">
                <el-tag size="small" effect="dark" :color="svc.tagColor" style="border:none">
                  {{ svc.tag }}
                </el-tag>
                <el-tag v-if="svc.styleLabel" size="small" type="info" effect="plain">
                  {{ svc.styleLabel }}
                </el-tag>
              </div>
              <h3 class="card-title">{{ svc.title }}</h3>
              <p class="card-desc">{{ svc.description }}</p>
              <div class="plan-specs">
                <div class="spec-item"><el-icon><Aim /></el-icon> {{ svc.presetTitle }}</div>
                <div class="spec-item"><el-icon><MagicStick /></el-icon> 内置提示词</div>
              </div>
              <div class="card-actions">
                <el-button size="small" type="primary" plain round>
                  进入对话
                </el-button>
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

    <!-- Plan Detail Dialog (Rich Preview) -->
    <el-dialog v-model="showPlanDetail" width="650px" class="preview-dialog" align-center destroy-on-close>
      <div v-if="detailedPlan" class="preview-content">
        <!-- Header Visual -->
        <div class="preview-header" :class="getCardTheme(detailedPlan.goal)">
          <div class="preview-tags">
            <span class="preview-tag">{{ detailedPlan.goal || '通用' }}</span>
            <span class="preview-tag">{{ detailedPlan.difficulty || '初级' }}</span>
          </div>
          <h2>{{ detailedPlan.title }}</h2>
          <div class="preview-meta">
            <span><el-icon><Timer /></el-icon> {{ detailedPlan.duration || '4周' }}</span>
            <span><el-icon><RefreshRight /></el-icon> {{ detailedPlan.frequency || '每周4天' }}</span>
            <span><el-icon><Aim /></el-icon> {{ detailedPlan.audience || '新手适合' }}</span>
          </div>
        </div>

        <!-- Body -->
        <div class="preview-body">
          <div class="section-title">计划目标</div>
          <p class="preview-desc">{{ detailedPlan.description || '帮助您在有限的时间内达到最佳的训练效果。' }}</p>

          <div v-if="!detailedPlan.isCourse" class="section-title">周结构预览 (示例)</div>
          <div v-if="!detailedPlan.isCourse" class="week-preview">
             <div class="day-row" v-for="(day, idx) in defaultWeekPreview" :key="idx">
               <div class="day-label">{{ day.name }}</div>
               <div class="day-content" :class="{'is-rest': day.type === '休息'}">{{ day.desc }}</div>
             </div>
          </div>

          <div class="section-title">
            {{ detailedPlan.isCourse ? '具体动作' : '动作示例' }}
          </div>
          <div class="demo-actions">
            <div 
              v-for="(action, index) in parseActions(detailedPlan.actions).slice(0, 3)" 
              :key="index" 
              class="demo-action-card"
            >
              <div class="d-placeholder"><el-icon><VideoCamera /></el-icon></div>
              <div class="d-info">
                <div class="d-name">{{ action.name || '示例动作' }}</div>
                <div class="d-sets">{{ action.sets || '3组 x 12次' }}</div>
              </div>
            </div>
            <div v-if="parseActions(detailedPlan.actions).length === 0" class="demo-action-card">
              <div class="d-placeholder"><el-icon><VideoCamera /></el-icon></div>
              <div class="d-info">
                <div class="d-name">深蹲</div>
                <div class="d-sets">示例</div>
              </div>
            </div>
            <div v-if="parseActions(detailedPlan.actions).length === 0" class="demo-action-card">
              <div class="d-placeholder"><el-icon><VideoCamera /></el-icon></div>
              <div class="d-info">
                <div class="d-name">平板支撑</div>
                <div class="d-sets">示例</div>
              </div>
            </div>
            <div v-if="parseActions(detailedPlan.actions).length === 0" class="demo-action-card">
              <div class="d-placeholder"><el-icon><VideoCamera /></el-icon></div>
              <div class="d-info">
                <div class="d-name">开合跳</div>
                <div class="d-sets">示例</div>
              </div>
            </div>
          </div>
        </div>
      </div>

    <template #footer>
        <div class="preview-footer">
          <el-button size="large" :type="isCollected(detailedPlan) ? 'warning' : 'default'" @click="toggleCollect(detailedPlan)">
            <el-icon><Star v-if="!isCollected(detailedPlan)" /><StarFilled v-else /></el-icon> {{ isCollected(detailedPlan) ? '已加入想练' : '记入想练' }}
          </el-button>

          <template v-if="detailedPlan.isCourse">
            <el-button
              type="primary"
              size="large"
              plain
              round
              :disabled="isCourseSubscribed(detailedPlan)"
              @click="openCourseSchedule(detailedPlan)"
            >
              {{ isCourseSubscribed(detailedPlan) ? '已加入训练' : '加入训练计划' }}
            </el-button>
          </template>
          <template v-else>
            <el-button
              type="primary"
              size="large"
              round
              :disabled="isSubscribed(detailedPlan)"
              @click="openSubscribeConfig(detailedPlan)"
            >
              {{ isSubscribed(detailedPlan) ? '已加入训练' : '加入训练计划' }}
            </el-button>
          </template>
        </div>
      </template>
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
import { Search, Plus, Calendar, Timer, User, RefreshRight, Location, UserFilled, Aim, MagicStick, VideoCamera, Star, StarFilled, InfoFilled } from '@element-plus/icons-vue'
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

const filterForm = reactive({
  goal: '',
  difficulty: '',
  duration: '',
  frequency: '',
  scene: '',
  part: ''
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
    if (filterForm.goal && p.goal !== filterForm.goal) return false;
    if (filterForm.difficulty && p.difficulty !== filterForm.difficulty) return false;
    if (filterForm.duration && p.duration !== filterForm.duration) return false;
    if (filterForm.frequency && p.frequency !== filterForm.frequency) return false;
    if (filterForm.scene && p.scene !== filterForm.scene) return false;
    if (filterForm.part && p.part !== filterForm.part) return false; // assuming 'part' might exist
    return true;
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
    if (filterForm.goal && (!c.category || !c.category.includes(filterForm.goal))) return false;
    if (filterForm.difficulty && c.difficulty !== filterForm.difficulty) return false;
    return true;
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
  background: white;
  border-bottom: 1px solid #F1F5F9;
  flex-shrink: 0;
  gap: 24px;
}

.explore-title-area { min-width: 160px; }
.module-title { font-size: 20px; font-weight: 900; color: #1E293B; margin: 0 0 2px; }
.module-subtitle { font-size: 13px; color: #94A3B8; margin: 0; }

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
  padding: 20px 24px;
  overflow-y: auto;
  background: #F8FAFC;
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
</style>
