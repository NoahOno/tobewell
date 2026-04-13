<template>
  <div class="library-audit admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">训练共享审核</h2>
        <p class="ph-desc">处理用户提交的训练资源入库申请，核准后将向全站公开</p>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <div class="audit-summary">
        <div class="summary-item">
          <span class="summary-label">待审核申请</span>
          <strong class="summary-value">{{ submissions.length }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">计划申请</span>
          <strong class="summary-value">{{ planSubmissionCount }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">课程申请</span>
          <strong class="summary-value">{{ courseSubmissionCount }}</strong>
        </div>
      </div>

      <el-table :data="submissions" style="width: 100%" table-layout="fixed">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="100">
          <template #default="sc">
            <el-tag size="small" effect="plain">{{ sc.row.resourceType === 'PLAN' ? '训练计划' : '训练课程' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="资源信息" min-width="220" show-overflow-tooltip>
          <template #default="sc">
            <div class="resource-cell">
              <div class="resource-title">{{ sc.row.resourceTitle || `资源 #${sc.row.resourceId}` }}</div>
              <div class="resource-meta">资源ID {{ sc.row.resourceId }} · 提交人 {{ sc.row.submitterId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请说明" min-width="220" show-overflow-tooltip>
          <template #default="sc">
            <span class="submission-note">{{ sc.row.note || '未填写说明' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="sc">
            {{ formatDateTime(sc.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="sc">
            <el-tag size="small" :type="sc.row.status === 'PENDING' ? 'warning' : (sc.row.status === 'APPROVED' ? 'success' : 'danger')">
              {{ sc.row.status === 'PENDING' ? '待审核' : (sc.row.status === 'APPROVED' ? '已通过' : '已驳回') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="right">
          <template #default="sc">
            <el-button type="info" size="small" link @click="previewResource(sc.row)">
               编辑
            </el-button>
            <el-button v-if="sc.row.status === 'PENDING'" type="success" size="small" link @click="approveSubmission(sc.row.id)">
               通过
            </el-button>
            <el-button v-if="sc.row.status === 'PENDING'" type="danger" size="small" link @click="rejectSubmission(sc.row.id)">
               驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && submissions.length === 0" description="暂无入库审核任务" :image-size="90" />
    </div>

    <!-- Resource Preview Drawer -->
    <el-drawer v-model="drawerVisible" title="资源入库终审" size="1040px" destroy-on-close>
       <div v-if="loadingResource" v-loading="loadingResource" style="height:200px"></div>
       <div v-else-if="currentResource" class="audit-drawer-body">
          <div class="audit-drawer-meta">
            <div>
              <div class="drawer-kicker">{{ currentSubmission.resourceType === 'PLAN' ? '训练计划申请' : '训练课程申请' }}</div>
              <div class="drawer-title">{{ currentResource.title || `资源 #${currentSubmission.resourceId}` }}</div>
            </div>
            <div class="drawer-note">{{ currentSubmission.note || '申请人未填写补充说明' }}</div>
          </div>

          <TrainingResourceViewer
            :item="currentResource"
            :type="currentSubmission.resourceType === 'PLAN' ? 'plan' : 'course'"
          />

          <el-divider border-style="dashed" />
          <div class="drawer-actions">
             <el-button style="flex:1" type="danger" round plain @click="rejectSubmission(currentSubmission.id); drawerVisible=false">判定违规/驳回</el-button>
             <el-button style="flex:1" type="success" round @click="approveSubmission(currentSubmission.id); drawerVisible=false">核准入库</el-button>
          </div>
       </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import TrainingResourceViewer from '../../components/TrainingResourceViewer.vue'

const submissions = ref<any[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const currentSubmission = ref<any>(null)
const currentResource = ref<any>(null)
const loadingResource = ref(false)

const planSubmissionCount = computed(() => submissions.value.filter(item => item.resourceType === 'PLAN').length)
const courseSubmissionCount = computed(() => submissions.value.filter(item => item.resourceType === 'COURSE').length)

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/resource/admin/submissions', { params: { status: 'PENDING' } })
    const rawList = res.data || []
    const [planRes, courseRes] = await Promise.all([
      request.get('/admin/plans'),
      request.get('/admin/courses')
    ])
    const planMap = new Map((((planRes as any).data) || []).map((item: any) => [item.id, item]))
    const courseMap = new Map((((courseRes as any).data) || []).map((item: any) => [item.id, item]))
    submissions.value = rawList.map((item: any) => {
      const source = item.resourceType === 'PLAN' ? planMap.get(item.resourceId) : courseMap.get(item.resourceId)
      return {
        ...item,
        resourceTitle: source?.title || '',
        resourceCover: source?.coverImage || source?.cover_image || ''
      }
    })
  } finally { loading.value = false }
}

const previewResource = async (sub: any) => {
  currentSubmission.value = sub
  drawerVisible.value = true
  loadingResource.value = true
  try {
     const endpoint = sub.resourceType === 'PLAN' ? '/admin/plans' : '/admin/courses'
     const res: any = await request.get(endpoint)
     const list = res.data || []
     currentResource.value = list.find((i: any) => i.id === sub.resourceId)
  } finally { loadingResource.value = false }
}

const approveSubmission = async (id: number) => {
  await request.post(`/resource/admin/submissions/${id}/approve`)
  ElMessage.success('已通过审核')
  fetchSubmissions()
}

const rejectSubmission = async (id: number) => {
  await request.post(`/resource/admin/submissions/${id}/reject`, { note: '管理驳回' })
  ElMessage.success('已驳回申请')
  fetchSubmissions()
}

const formatDateTime = (value: string) => {
  if (!value) return '--'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(fetchSubmissions)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.audit-summary { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 20px; }
.summary-item { padding: 16px 18px; border-radius: 14px; background: #f8fafc; border: 1px solid #e2e8f0; }
.summary-label { display: block; color: #64748b; font-size: 13px; margin-bottom: 8px; }
.summary-value { font-size: 24px; color: #0f172a; font-weight: 900; }
.resource-cell { display: flex; flex-direction: column; gap: 6px; }
.resource-title { font-size: 14px; font-weight: 800; color: #0f172a; }
.resource-meta { font-size: 12px; color: #64748b; }
.submission-note { color: #475569; line-height: 1.6; }
.audit-drawer-body { padding: 24px; }
.audit-drawer-meta { display: flex; justify-content: space-between; gap: 24px; align-items: flex-start; margin-bottom: 20px; }
.drawer-kicker { font-size: 12px; font-weight: 800; color: #3b82f6; margin-bottom: 6px; }
.drawer-title { font-size: 24px; font-weight: 900; color: #0f172a; }
.drawer-note { max-width: 320px; padding: 12px 14px; border-radius: 12px; background: #f8fafc; color: #475569; line-height: 1.6; }
.drawer-actions { display: flex; gap: 16px; margin-top: 32px; }

@media (max-width: 960px) {
  .audit-summary { grid-template-columns: 1fr; }
  .audit-drawer-meta { flex-direction: column; }
  .drawer-note { max-width: none; width: 100%; }
  .drawer-actions { flex-direction: column; }
}
</style>
