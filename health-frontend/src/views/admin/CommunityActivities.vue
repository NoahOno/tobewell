<template>
  <div class="community-activities admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">活动资源管理</h2>
        <p class="ph-desc">创建并维护社区官方打卡活动</p>
      </div>
      <div class="ph-right">
        <el-button type="primary" round @click="openNewActivity">新增活动</el-button>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table :data="activities" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="活动信息" min-width="260">
          <template #default="sc">
            <div class="act-cell">
              <span class="act-title">{{ sc.row.title }}</span>
              <span class="act-meta">{{ activityTrainingSummary(sc.row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" min-width="220">
          <template #default="sc">
            <div class="act-time">{{ formatDateTime(sc.row.startTime) }} - {{ formatDateTime(sc.row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="sc">
            <el-tag :type="getDisplayStatusType(sc.row)" size="small">{{ getDisplayStatus(sc.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理" width="280" align="right">
          <template #default="sc">
            <el-button size="small" link @click="editActivity(sc.row)">编辑</el-button>
            <el-button 
              size="small" 
              link 
              :type="sc.row.status === 'ONLINE' ? 'danger' : 'success'"
              v-if="sc.row.status !== 'DRAFT'"
              @click="toggleOnlineStatus(sc.row)"
            >
              {{ sc.row.status === 'ONLINE' ? '下架' : '上线' }}
            </el-button>
            <el-button 
              size="small" 
              link 
              type="primary"
              v-if="sc.row.status === 'DRAFT'"
              @click="toggleOnlineStatus(sc.row)"
            >上线</el-button>
            <el-button size="small" link type="warning" @click="togglePin(sc.row.id, sc.row.pinned)">
              {{ sc.row.pinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button size="small" link @click="showAnalytics(sc.row.id)">统计</el-button>
            <el-popconfirm title="确定删除该活动吗？" @confirm="deleteActivity(sc.row.id)">
              <template #reference>
                 <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Activity Dialog -->
    <el-drawer v-model="activityDialogVisible" :title="activityForm.id ? '编辑活动' : '新增活动'" size="850px">
      <div style="padding: 24px;">
        <el-form :model="activityForm" label-position="top">
          <el-row :gutter="32">
            <!-- 左侧基础信息 -->
            <el-col :span="10">
              <el-form-item label="封面图 (图片上传)">
                 <el-upload
                   class="premium-uploader"
                   style="height: 200px; width: 100%; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; position: relative"
                   action="/api/file/upload"
                   :headers="uploadHeaders"
                   :show-file-list="false"
                   :on-success="handleUploadSuccess"
                 >
                   <img v-if="activityForm.coverImage" :src="activityForm.coverImage" style="width: 100%; height: 100%; object-fit: cover" />
                   <div v-else style="color: #909399; display: flex; flex-direction: column; align-items: center">
                     <el-icon :size="28"><Plus /></el-icon>
                     <span style="font-size: 13px; margin-top: 8px">上传封面图</span>
                   </div>
                 </el-upload>
              </el-form-item>
              
              <el-form-item label="活动标题"><el-input v-model="activityForm.title" placeholder="如：30天春日燃烧计划" /></el-form-item>
              <el-row :gutter="16">
                <el-col :span="12"><el-form-item label="开始时间"><el-date-picker v-model="activityForm.startTime" type="datetime" style="width:100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结束时间"><el-date-picker v-model="activityForm.endTime" type="datetime" style="width:100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="活动类型">
                 <el-radio-group v-model="activityForm.activityType" style="display: flex; gap: 8px; flex-wrap: wrap;">
                   <el-radio :label="1" border style="margin-right: 0">打卡类</el-radio>
                   <el-radio :label="2" border style="margin-right: 0">挑战类</el-radio>
                   <el-radio :label="3" border style="margin-right: 0">话题类</el-radio>
                 </el-radio-group>
              </el-form-item>
            </el-col>

            <!-- 右侧详情与联动规则 -->
            <el-col :span="14">
              <el-divider>规则与联动</el-divider>
              
              <!-- 挑战类：关联训练（弹窗选择） -->
              <el-form-item label="关联训练" v-if="activityForm.activityType === 2">
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                  <template v-if="activityForm.templateId">
                    <el-tag type="primary" effect="plain">{{ activityForm.templateType === 'PLAN' ? '训练计划' : '单次课程' }}</el-tag>
                    <span style="font-weight: 600; color: #334155">{{ linkedTrainingTitle }}</span>
                  </template>
                  <span v-else style="color: #94a3b8; font-size: 13px">未选择关联训练</span>
                  <el-button type="primary" plain round size="small" @click="openTrainingPicker">选择训练</el-button>
                  <el-button v-if="activityForm.templateId" link type="danger" size="small" @click="clearLinkedTraining">清除</el-button>
                </div>
              </el-form-item>
              
              <!-- 打卡类统计维度 -->
              <el-form-item label="统计维度" v-if="activityForm.activityType === 1">
                <el-radio-group v-model="activityForm.countMode" style="display: flex; gap: 8px;">
                  <el-radio label="DAYS" border style="margin-right: 0">按天统计</el-radio>
                  <el-radio label="COUNT" border style="margin-right: 0">按次数统计</el-radio>
                </el-radio-group>
                <div style="font-size: 12px; color: #94a3b8; margin-top: 4px;">
                  按天：每个自然日最多计 1 次有效打卡<br/>
                  按次数：累计打卡次数达到目标即完成
                </div>
              </el-form-item>

              <!-- 挑战类统计维度选择 -->
              <el-form-item label="统计维度" v-if="activityForm.activityType === 2">
                <el-radio-group v-model="activityForm.countMode" style="display: flex; gap: 8px;">
                  <el-radio label="DAYS" border style="margin-right: 0">按参与天数统计</el-radio>
                  <el-radio label="COUNT" border style="margin-right: 0">按参与次数统计</el-radio>
                </el-radio-group>
                <div style="font-size: 12px; color: #94a3b8; margin-top: 4px;">
                  按天数：统计用户在活动期间有多少天完成了训练<br/>
                  按次数：统计用户总共完成了多少次训练
                </div>
              </el-form-item>

              <!-- 仅话题类显示 -->
              <el-form-item label="关联社区话题" v-if="activityForm.activityType === 3">
                 <el-input v-model="activityForm.topicName" placeholder="如：#我的减脂期日记" />
                 <div style="font-size: 12px; color: #94a3b8; margin-top: 4px;">发帖时带上该话题即视为参与活动</div>
              </el-form-item>
              
              <!-- 话题类统计方式选择 -->
              <el-form-item label="统计方式" v-if="activityForm.activityType === 3">
                <el-radio-group v-model="activityForm.topicStatMode" style="display: flex; gap: 8px; flex-wrap: wrap;">
                  <el-radio label="SHARED" border style="margin-right: 0">是否分享</el-radio>
                  <el-radio label="DAYS" border style="margin-right: 0">分享天数</el-radio>
                  <el-radio label="COUNT" border style="margin-right: 0">分享次数</el-radio>
                </el-radio-group>
                <div style="font-size: 12px; color: #94a3b8; margin-top: 4px;">
                  是否分享：用户是否发过话题帖（是/否）<br/>
                  分享天数：用户累计有多少天发了话题帖<br/>
                  分享次数：用户总共发了多少条话题帖
                </div>
              </el-form-item>

              <el-row :gutter="16">
                 <el-col :span="12">
                    <el-form-item :label="goalFieldLabel">
                       <el-input-number v-model="activityForm.requiredDays" :min="1" :disabled="activityForm.activityType === 3 && activityForm.topicStatMode === 'SHARED'" style="width:100%" />
                       <div v-if="activityForm.activityType === 3 && activityForm.topicStatMode === 'SHARED'" style="font-size: 12px; color: #94a3b8; margin-top: 4px;">是否分享类活动仅需完成 1 次带话题发帖，目标固定为 1</div>
                    </el-form-item>
                 </el-col>
                 <el-col :span="12">
                    <el-form-item label="完成奖励积分">
                       <el-input-number v-model="activityForm.rewardPoints" :min="0" style="width:100%" />
                    </el-form-item>
                 </el-col>
              </el-row>
              
              <el-divider>活动介绍</el-divider>
              <el-form-item label="活动说明（纯文本）">
                <el-input v-model="activityForm.descriptionHtml" type="textarea" :rows="5" placeholder="用简洁的语言介绍活动内容、参与方式和注意事项..." />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <template #footer>
         <div style="display:flex; justify-content: flex-end; padding:16px;">
           <el-button @click="activityDialogVisible = false" round>取消</el-button>
           <el-button type="primary" @click="saveActivity" round>保存</el-button>
         </div>
      </template>
    </el-drawer>

    <el-dialog v-model="trainingPickerVisible" title="选择关联训练" width="640px" align-center destroy-on-close>
      <el-input v-model="trainingSearch" placeholder="搜索计划或课程名称..." clearable style="margin-bottom: 12px" />
      <el-table
        :data="filteredTrainingOptions"
        max-height="380"
        highlight-current-row
        @row-click="pickTrainingRow"
        style="width: 100%; cursor: pointer"
      >
        <el-table-column label="类型" width="100">
          <template #default="sc">
            <el-tag size="small" :type="sc.row.kind === 'PLAN' ? 'success' : 'warning'" effect="plain">
              {{ sc.row.kind === 'PLAN' ? '计划' : '课程' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="名称" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
      </el-table>
      <template #footer>
        <el-button round @click="trainingPickerVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Analytics Dialog -->
    <el-dialog v-model="analyticsDialogVisible" title="活动统计数据" width="450px" align-center>
       <div class="analytics-grid">
         <div class="analytics-item"><span>总参与</span><strong>{{ analyticsData.totalParticipants }}</strong></div>
         <div class="analytics-item"><span>日活跃</span><strong>{{ analyticsData.dailyActive }}</strong></div>
         <div class="analytics-item"><span>完成数</span><strong>{{ analyticsData.completedParticipants }}</strong></div>
         <div class="analytics-item"><span>完成率</span><strong>{{ analyticsData.completionRate }}%</strong></div>
       </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../api/request'

const activities = ref<any[]>([])
const loading = ref(false)
const activityDialogVisible = ref(false)
const analyticsDialogVisible = ref(false)
const analyticsData = ref<any>({ totalParticipants: 0, dailyActive: 0, completedParticipants: 0, completionRate: 0 })
const trainingPlans = ref<any[]>([])
const courses = ref<any[]>([])
const trainingPickerVisible = ref(false)
const trainingSearch = ref('')

/** 必须先于所有引用 activityForm 的 computed / watch 声明，否则会触发 TDZ 导致整页白屏 */
const activityForm = ref<any>({
  id: null,
  title: '',
  coverImage: '',
  descriptionHtml: '',
  startTime: '',
  endTime: '',
  activityType: 1,
  templateType: 'PLAN',
  templateId: null,
  topicName: '',
  countMode: 'DAYS',
  topicStatMode: 'COUNT',
  rewardPoints: 0,
  rewardAvatarFrame: '',
  requiredDays: 7,
  pinned: 0,
  status: 'DRAFT'
})

const trainingOptions = computed(() => [
  ...trainingPlans.value.map((p: any) => ({ ...p, kind: 'PLAN' as const })),
  ...courses.value.map((c: any) => ({ ...c, kind: 'COURSE' as const }))
])

const filteredTrainingOptions = computed(() => {
  const q = trainingSearch.value.trim().toLowerCase()
  if (!q) return trainingOptions.value
  return trainingOptions.value.filter(
    (r: any) =>
      String(r.title || '').toLowerCase().includes(q) || String(r.category || '').toLowerCase().includes(q)
  )
})

const linkedTrainingTitle = computed(() => {
  const f = activityForm.value
  if (!f.templateId) return ''
  const list = f.templateType === 'PLAN' ? trainingPlans.value : courses.value
  const hit = list.find((x: any) => x.id === f.templateId)
  return hit?.title || `ID ${f.templateId}`
})

const goalFieldLabel = computed(() => {
  const f = activityForm.value
  if (f.activityType === 1) return f.countMode === 'COUNT' ? '目标打卡次数' : '目标打卡天数'
  if (f.activityType === 2) return f.countMode === 'COUNT' ? '目标训练次数' : '目标训练天数'
  if (f.activityType === 3) {
    if (f.topicStatMode === 'SHARED') return '分享目标'
    if (f.topicStatMode === 'DAYS') return '目标分享天数'
    return '目标分享次数'
  }
  return '目标'
})

watch(
  () => [activityForm.value.activityType, activityForm.value.topicStatMode],
  () => {
    const f = activityForm.value
    if (f.activityType === 3 && f.topicStatMode === 'SHARED') {
      f.requiredDays = 1
    }
    if (f.activityType === 1 && (f.countMode === undefined || f.countMode === null || f.countMode === '')) {
      f.countMode = 'DAYS'
    }
  },
  { deep: true }
)

const openTrainingPicker = () => {
  trainingSearch.value = ''
  fetchTrainingPlans()
  fetchCourses()
  trainingPickerVisible.value = true
}

const pickTrainingRow = (row: any) => {
  activityForm.value.templateType = row.kind
  activityForm.value.templateId = row.id
  trainingPickerVisible.value = false
  ElMessage.success('已选择：' + row.title)
}

const clearLinkedTraining = () => {
  activityForm.value.templateId = null
}

const activityTrainingSummary = (row: any) => {
  if (row.activityType !== 2) return '—'
  if (!row.templateId) return '未关联训练'
  return `${row.templateType === 'PLAN' ? '训练计划' : '单次课程'} · ID ${row.templateId}`
}

const uploadHeaders = computed(() => ({
  'satoken': localStorage.getItem('token') || ''
}))

const getDisplayStatus = (row: any) => {
  if (row.status === 'DRAFT') return '草稿'
  if (row.status === 'OFFLINE') return '已下线'
  
  const now = new Date().getTime()
  const st = row.startTime ? new Date(row.startTime).getTime() : NaN
  const et = row.endTime ? new Date(row.endTime).getTime() : NaN
  
  if (Number.isFinite(st) && now < st) return '未开始'
  if (Number.isFinite(et) && now > et) return '已结束'
  return '进行中'
}
const getDisplayStatusType = (row: any) => {
  const status = getDisplayStatus(row)
  switch (status) {
    case '草稿': return 'info'
    case '未开始': return 'warning'
    case '进行中': return 'success'
    case '已结束': return 'info'
    case '已下线': return 'danger'
    default: return 'info'
  }
}

const formatDateTime = (val: any) => val ? new Date(val).toLocaleString() : '-'

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    activityForm.value.coverImage = res.url
    ElMessage.success('上传成功')
  }
}

const fetchTrainingPlans = async () => {
  try {
    const res: any = await request.get('/admin/activities/training-plans')
    trainingPlans.value = res.data || []
  } catch (e) {}
}

const fetchCourses = async () => {
  try {
    const res: any = await request.get('/admin/activities/courses')
    courses.value = res.data || []
  } catch (e) {}
}

const openNewActivity = () => {
  activityForm.value = {
    id: null,
    title: '',
    coverImage: '',
    descriptionHtml: '',
    startTime: '',
    endTime: '',
    activityType: 1,
    templateType: 'PLAN',
    templateId: null,
    topicName: '',
    countMode: 'DAYS',
    topicStatMode: 'COUNT',
    rewardPoints: 0,
    rewardAvatarFrame: '',
    requiredDays: 7,
    pinned: 0,
    status: 'DRAFT'
  }
  fetchTrainingPlans()
  fetchCourses()
  activityDialogVisible.value = true
}

const editActivity = (row: any) => {
  activityForm.value = { ...row }
  if (activityForm.value.activityType === 1 && !activityForm.value.countMode) {
    activityForm.value.countMode = 'DAYS'
  }
  fetchTrainingPlans()
  fetchCourses()
  activityDialogVisible.value = true
}

const fetchActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/activities')
    activities.value = res.data || []
  } finally { loading.value = false }
}

const saveActivity = async () => {
  const payload = { ...activityForm.value }
  if (payload.activityType === 3 && payload.topicStatMode === 'SHARED') {
    payload.requiredDays = 1
  }
  await request.post('/admin/activities', payload)
  ElMessage.success('活动已保存')
  activityDialogVisible.value = false
  fetchActivities()
}

const toggleOnlineStatus = async (row: any) => {
  const nextStatus = row.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE'
  await request.post(`/admin/activities`, { ...row, status: nextStatus })
  ElMessage.success(nextStatus === 'ONLINE' ? '已上线' : '已下线')
  fetchActivities()
}

const togglePin = async (id: number, pinned: number) => {
  const nextPinned = pinned === 1 ? 0 : 1
  await request.post(`/admin/activities/${id}/pin`, { pinned: nextPinned })
  fetchActivities()
}

const showAnalytics = async (id: number) => {
  const res: any = await request.get(`/admin/activities/${id}/analytics`)
  analyticsData.value = {
     totalParticipants: res?.data?.totalParticipants ?? 0,
     dailyActive: res?.data?.dailyActive ?? 0,
     completedParticipants: res?.data?.completedParticipants ?? 0,
     completionRate: res?.data?.completionRate ?? 0
  }
  analyticsDialogVisible.value = true
}

const deleteActivity = async (id: number) => {
  await request.delete(`/admin/activities/${id}`)
  ElMessage.success('活动已删除')
  fetchActivities()
}

onMounted(fetchActivities)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.act-cell { display: flex; flex-direction: column; }
.act-title { font-weight: 700; color: var(--text-main); }
.act-meta { font-size: 12px; color: var(--text-light); }
.analytics-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; padding: 20px; }
.analytics-item { display: flex; flex-direction: column; background: #f8fafc; padding: 16px; border-radius: 12px; }
.analytics-item span { font-size: 12px; color: var(--text-light); }
.analytics-item strong { font-size: 20px; color: var(--text-main); margin-top: 4px; }
</style>
