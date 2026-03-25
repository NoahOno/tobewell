<template>
  <div class="training-admin admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">训练资源中心</h2>
        <p class="ph-desc">维护系统标准化动作库、官方训练计划及单次精选课程</p>
      </div>
      <div class="ph-right">
        <el-button type="primary" class="add-btn-premium" @click="handleAddButtonClick" round>
          <el-icon><Plus /></el-icon> {{ addBtnText }}
        </el-button>
      </div>
    </div>
    
    <div class="admin-tabs-container">
      <el-tabs v-model="activeModule" class="premium-tabs" @tab-change="handleTabChange">
        <!-- 1. Exercise (Action) Library CRUD -->
        <el-tab-pane label="动作图鉴 (Exercises)" name="exercises">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="exercises" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="动作信息" min-width="200">
                <template #default="sc">
                   <div class="ex-cell">
                     <span class="ex-name">{{ sc.row.name }}</span>
                     <span class="ex-muscle">{{ sc.row.muscle }} · {{ sc.row.type }}</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column prop="equipment" label="所需器械" width="120" />
              <el-table-column label="难度" width="100">
                <template #default="sc">
                  <el-tag :type="difficultyTypeMap[sc.row.difficulty]" size="small" effect="light">{{ sc.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="演示状态" width="100">
                 <template #default="sc">
                    <el-icon v-if="sc.row.imageUrl" color="var(--primary-color)"><CircleCheckFilled /></el-icon>
                    <el-icon v-else color="var(--text-light)"><CircleCloseFilled /></el-icon>
                 </template>
              </el-table-column>
              <el-table-column label="管理" width="150" align="right">
                <template #default="sc">
                  <el-button size="small" link @click="editExercise(sc.row)">编辑</el-button>
                  <el-popconfirm title="确定将其从系统库中移除吗？" @confirm="deleteExercise(sc.row.id)">
                    <template #reference>
                      <el-button size="small" type="danger" link>删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 2. Public Plans CRUD -->
        <el-tab-pane label="官方计划 (Plans)" name="plans">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="plans" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="计划标题" min-width="250">
                <template #default="sc">
                  <div class="plan-cell">
                    <span class="p-name">{{ sc.row.title }}</span>
                    <span class="p-meta">{{ sc.row.category }} · {{ sc.row.duration }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column label="管理" width="150" align="right">
                <template #default="sc">
                  <el-button size="small" link @click="editPlan(sc.row)">详情/编辑</el-button>
                  <el-popconfirm title="下架该官方训练计划？" @confirm="deletePlan(sc.row.id)">
                    <template #reference>
                      <el-button size="small" type="danger" link>下架</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 3. Public Courses CRUD -->
        <el-tab-pane label="精品课程 (Courses)" name="courses">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="courses" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="课程信息" min-width="250">
                <template #default="sc">
                   <div class="course-cell">
                     <span class="c-name">{{ sc.row.title }}</span>
                     <span class="c-meta">{{ sc.row.category }} · {{ sc.row.durationMinutes }}分钟</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column label="难度" width="120">
                <template #default="sc">
                  <el-tag :type="difficultyTypeMap[sc.row.difficulty]" size="small">{{ sc.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="150" align="right">
                <template #default="sc">
                  <el-button size="small" link @click="editCourse(sc.row)">编辑</el-button>
                  <el-popconfirm title="彻底下架该精选课程？" @confirm="deleteCourse(sc.row.id)">
                    <template #reference>
                      <el-button size="small" type="danger" link>下架</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- Modals (Dialogs) -->
    <el-dialog v-model="exerciseDialogVisible" :title="formEx.id ? '编辑系统动作' : '入库新动作'" width="580px" class="premium-dialog">
      <el-form :model="formEx" label-position="top">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="动作名称"><el-input v-model="formEx.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标肌群"><el-input v-model="formEx.muscle" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="训练指导说明">
          <el-input v-model="formEx.instruction" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="图片演示链接 (URL)">
           <el-input v-model="formEx.imageUrl" placeholder="HTTPS://..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exerciseDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="saveExercise" round>同步到云端</el-button>
      </template>
    </el-dialog>

     <el-dialog v-model="planCourseDialogVisible" :title="activeModule === 'plans' ? '管理训练计划' : '管理单次课程'" width="580px" class="premium-dialog">
       <el-form v-if="activeModule === 'plans'" :model="formPlan" label-position="top">
         <el-form-item label="计划标题"><el-input v-model="formPlan.title" /></el-form-item>
         <el-form-item label="计划简介"><el-input v-model="formPlan.description" type="textarea" :rows="3" /></el-form-item>
         <el-form-item label="分类标签 (逗号分隔)"><el-input v-model="formPlan.category" /></el-form-item>
       </el-form>
       <el-form v-else :model="formCourse" label-position="top">
         <el-form-item label="课程标题"><el-input v-model="formCourse.title" /></el-form-item>
         <el-form-item label="课程分类"><el-input v-model="formCourse.category" /></el-form-item>
         <el-form-item label="设定难度">
           <el-radio-group v-model="formCourse.difficulty">
             <el-radio-button label="初级" />
             <el-radio-button label="中级" />
             <el-radio-button label="高级" />
           </el-radio-group>
         </el-form-item>
       </el-form>
       <template #footer>
         <el-button @click="planCourseDialogVisible = false" round>离开</el-button>
         <el-button type="primary" @click="savePlanCourse" round>确认发布</el-button>
       </template>
     </el-dialog>

    <!-- Activity Dialog -->
    <el-dialog
      v-model="activityDialogVisible"
      :title="formActivity.id ? '编辑活动' : '发布活动'"
      width="620px"
      class="premium-dialog"
    >
      <el-form :model="formActivity" label-position="top">
        <el-form-item label="活动标题">
          <el-input v-model="formActivity.title" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="formActivity.coverImage" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="活动描述 (HTML 字符串)">
          <el-input v-model="formActivity.descriptionHtml" type="textarea" :rows="4" />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="formActivity.startTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="formActivity.endTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="完成活动的要求(天)">
          <el-input-number v-model="formActivity.requiredDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>

        <el-form-item label="任务模板类型">
          <el-radio-group v-model="formActivity.templateType">
            <el-radio-button label="PLAN" />
            <el-radio-button label="COURSE" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择模板">
          <el-select
            v-if="formActivity.templateType === 'PLAN'"
            v-model="formActivity.templateId"
            placeholder="请选择训练计划"
            style="width: 100%"
          >
            <el-option v-for="p in plans" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
          <el-select
            v-else
            v-model="formActivity.templateId"
            placeholder="请选择精选课程"
            style="width: 100%"
          >
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="置顶">
          <el-radio-group v-model="formActivity.pinned">
            <el-radio :label="1">置顶</el-radio>
            <el-radio :label="0">不置顶</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="活动状态">
          <el-radio-group v-model="formActivity.status">
            <el-radio label="ONLINE">在线</el-radio>
            <el-radio label="OFFLINE">下线</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="saveActivity" round>确认发布</el-button>
      </template>
    </el-dialog>

    <!-- Activity Analytics Dialog -->
    <el-dialog v-model="analyticsDialogVisible" title="活动数据看板" width="420px" class="premium-dialog">
      <el-descriptions border :column="1" size="small">
        <el-descriptions-item label="总参与人数">{{ analyticsData.totalParticipants }}</el-descriptions-item>
        <el-descriptions-item label="日活跃人数 (完成任意任务去重)">{{ analyticsData.dailyActive }}</el-descriptions-item>
        <el-descriptions-item label="完成人数">{{ analyticsData.completedParticipants }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ formatPercent(analyticsData.completionRate) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="analyticsDialogVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const activeModule = ref('exercises')
const exercises = ref<any[]>([])
const plans = ref<any[]>([])
const courses = ref<any[]>([])
const activities = ref<any[]>([])
const loading = ref(false)

const difficultyTypeMap: any = {
  '初级': 'success',
  '中级': 'warning',
  '高级': 'danger'
}

const addBtnText = computed(() => {
  if (activeModule.value === 'exercises') return '动作入库'
  if (activeModule.value === 'plans') return '发布官方计划'
  if (activeModule.value === 'courses') return '新增精选课'
  return '发布活动'
})

// Exercise Logic
const exerciseDialogVisible = ref(false)
const formEx = reactive<any>({ id: null, name: '', muscle: '', type: '力量', equipment: '', instruction: '', recommendedSets: '', imageUrl: '' })

const fetchExercises = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/exercise/list')
        exercises.value = res.data
    } finally { loading.value = false }
}
const editExercise = (row: any) => {
    Object.assign(formEx, row)
    exerciseDialogVisible.value = true
}
const deleteExercise = async (id: number) => {
    await request.delete(`/exercise/${id}`)
    ElMessage.success('已从库中移除')
    fetchExercises()
}
const saveExercise = async () => {
    await request.post('/exercise/save', formEx)
    ElMessage.success('系统库已同步')
    exerciseDialogVisible.value = false
    fetchExercises()
}

// Plan & Course Logic
const planCourseDialogVisible = ref(false)
const formPlan = reactive<any>({ id: null, title: '', description: '', category: '', isPublic: true })
const formCourse = reactive<any>({ id: null, title: '', difficulty: '初级', category: '', isPublic: true })

// Activity Admin Logic
const activityDialogVisible = ref(false)
const analyticsDialogVisible = ref(false)
const analyticsData = reactive<any>({
  totalParticipants: 0,
  dailyActive: 0,
  completedParticipants: 0,
  completionRate: 0
})
const formActivity = reactive<any>({
  id: null,
  title: '',
  coverImage: '',
  descriptionHtml: '',
  startTime: '',
  endTime: '',
  templateType: 'PLAN',
  templateId: null,
  requiredDays: 7,
  pinned: 0,
  status: 'ONLINE'
})

const formatDateTime = (t: any) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatPercent = (v: any) => {
  const num = Number(v)
  if (!isFinite(num)) return '0%'
  return (num * 100).toFixed(2) + '%'
}

const fetchPlans = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/admin/plans')
        plans.value = res.data
    } finally { loading.value = false }
}
const fetchCourses = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/admin/courses')
        courses.value = res.data
    } finally { loading.value = false }
}

const handleTabChange = () => {
    if (activeModule.value === 'exercises') fetchExercises()
    if (activeModule.value === 'plans') fetchPlans()
    if (activeModule.value === 'courses') fetchCourses()
    if (activeModule.value === 'activities') {
      fetchActivities()
      // activity form depends on template library lists
      if (plans.value.length === 0) fetchPlans()
      if (courses.value.length === 0) fetchCourses()
    }
}

const handleAddButtonClick = () => {
  if (activeModule.value === 'exercises') {
    Object.assign(formEx, { id: null, name: '', muscle: '', type: '力量', equipment: '', instruction: '', recommendedSets: '', imageUrl: '' })
    exerciseDialogVisible.value = true
  } else if(activeModule.value === 'plans') {
    Object.assign(formPlan, { id: null, title: '', description: '', category: '', isPublic: true })
    planCourseDialogVisible.value = true
  } else if (activeModule.value === 'activities') {
    Object.assign(formActivity, {
      id: null,
      title: '',
      coverImage: '',
      descriptionHtml: '',
      startTime: '',
      endTime: '',
      templateType: 'PLAN',
      templateId: null,
      requiredDays: 7,
      pinned: 0,
      status: 'ONLINE'
    })
    activityDialogVisible.value = true
  } else {
    Object.assign(formCourse, { id: null, title: '', difficulty: '初级', category: '', isPublic: true })
    planCourseDialogVisible.value = true
  }
}

const editPlan = (row: any) => {
  Object.assign(formPlan, row)
  planCourseDialogVisible.value = true
}

const editCourse = (row: any) => {
  Object.assign(formCourse, row)
  planCourseDialogVisible.value = true
}

const savePlanCourse = async () => {
  if (activeModule.value === 'plans') {
    await request.post('/admin/plan/save', formPlan)
    fetchPlans()
  } else {
    await request.post('/admin/course/save', formCourse)
    fetchCourses()
  }
  ElMessage.success('公共库已同步')
  planCourseDialogVisible.value = false
}

const deletePlan = async (id: number) => {
  await request.delete(`/admin/plan/${id}`)
  fetchPlans()
}

const deleteCourse = async (id: number) => {
  await request.delete(`/admin/course/${id}`)
  fetchCourses()
}

// Activities CRUD
const fetchActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/activities')
    activities.value = res.data
  } finally {
    loading.value = false
  }
}

const editActivity = (row: any) => {
  Object.assign(formActivity, row)
  activityDialogVisible.value = true
}

const saveActivity = async () => {
  await request.post('/admin/activities', formActivity)
  ElMessage.success('活动已保存')
  activityDialogVisible.value = false
  fetchActivities()
}

const offlineActivity = async (id: number) => {
  await request.post(`/admin/activities/${id}/offline`)
  ElMessage.success('活动已下线')
  fetchActivities()
}

const togglePin = async (id: number, pinned: number) => {
  const nextPinned = pinned === 1 ? 0 : 1
  await request.post(`/admin/activities/${id}/pin`, { pinned: nextPinned })
  fetchActivities()
}

const showAnalytics = async (id: number) => {
  const res: any = await request.get(`/admin/activities/${id}/analytics`)
  analyticsData.totalParticipants = res?.data?.totalParticipants ?? 0
  analyticsData.dailyActive = res?.data?.dailyActive ?? 0
  analyticsData.completedParticipants = res?.data?.completedParticipants ?? 0
  analyticsData.completionRate = res?.data?.completionRate ?? 0
  analyticsDialogVisible.value = true
}

const deleteActivity = async (id: number) => {
  await request.delete(`/admin/activities/${id}`)
  ElMessage.success('活动已删除')
  fetchActivities()
}

onMounted(fetchExercises)
</script>

<style scoped>
.training-admin {
  animation: fadeIn 0.4s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.ph-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.ph-desc {
  font-size: 15px;
  color: var(--text-muted);
}

.add-btn-premium {
  height: 48px;
  padding: 0 24px;
  font-weight: 700;
  box-shadow: 0 10px 15px -3px rgba(74, 222, 128, 0.2);
}

.admin-tabs-container :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.admin-tabs-container :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-muted);
}

.table-card {
  padding: 0;
  margin-top: 16px;
}

.ex-cell, .plan-cell, .course-cell {
  display: flex;
  flex-direction: column;
}

.ex-name, .p-name, .c-name {
  font-weight: 700;
  color: var(--text-main);
}

.ex-muscle, .p-meta, .c-meta {
  font-size: 12px;
  color: var(--text-light);
}

.act-cell {
  display: flex;
  flex-direction: column;
}
.act-title {
  font-weight: 700;
  color: var(--text-main);
}
.act-meta {
  font-size: 12px;
  color: var(--text-light);
}
.act-time {
  font-size: 12px;
  color: var(--text-light);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
