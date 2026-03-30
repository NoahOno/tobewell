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
              <span class="act-meta">{{ sc.row.templateType }} · 模板ID {{ sc.row.templateId }}</span>
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
            <el-tag :type="sc.row.status === 'ONLINE' ? 'success' : 'danger'" size="small">{{ sc.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理" width="260" align="right">
          <template #default="sc">
            <el-button size="small" link @click="editActivity(sc.row)">编辑</el-button>
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
    <el-drawer v-model="activityDialogVisible" :title="activityForm.id ? '编辑活动' : '新增活动'" size="550px">
      <div style="padding: 24px;">
        <el-form :model="activityForm" label-position="top">
          <el-form-item label="活动标题"><el-input v-model="activityForm.title" /></el-form-item>
          <el-form-item label="封面图 (URL)"><el-input v-model="activityForm.coverImage" /></el-form-item>
          <el-form-item label="活动详情 HTML"><el-input v-model="activityForm.descriptionHtml" type="textarea" :rows="6" /></el-form-item>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="开始时间"><el-date-picker v-model="activityForm.startTime" type="datetime" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="结束时间"><el-date-picker v-model="activityForm.endTime" type="datetime" style="width:100%" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="模板类型">
                 <el-select v-model="activityForm.templateType" style="width:100%">
                    <el-option label="计划(PLAN)" value="PLAN" />
                    <el-option label="课程(COURSE)" value="COURSE" />
                 </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="模板ID"><el-input-number v-model="activityForm.templateId" style="width:100%" /></el-form-item></el-col>
          </el-row>
          <el-form-item label="状态">
             <el-select v-model="activityForm.status" style="width:100%">
                <el-option label="在线" value="ONLINE" />
                <el-option label="下线" value="OFFLINE" />
             </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
         <div style="display:flex; justify-content: flex-end; padding:16px;">
           <el-button @click="activityDialogVisible = false" round>取消</el-button>
           <el-button type="primary" @click="saveActivity" round>保存</el-button>
         </div>
      </template>
    </el-drawer>

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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const activities = ref<any[]>([])
const loading = ref(false)
const activityDialogVisible = ref(false)
const analyticsDialogVisible = ref(false)
const analyticsData = ref<any>({ totalParticipants: 0, dailyActive: 0, completedParticipants: 0, completionRate: 0 })

const activityForm = ref<any>({
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

const fetchActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/activities')
    activities.value = res.data || []
  } finally { loading.value = false }
}

const formatDateTime = (val: any) => val ? new Date(val).toLocaleString() : '-'

const openNewActivity = () => {
  activityForm.value = {
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
  }
  activityDialogVisible.value = true
}

const editActivity = (row: any) => {
  activityForm.value = { ...row }
  activityDialogVisible.value = true
}

const saveActivity = async () => {
  await request.post('/admin/activities', activityForm.value)
  ElMessage.success('活动已保存')
  activityDialogVisible.value = false
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
