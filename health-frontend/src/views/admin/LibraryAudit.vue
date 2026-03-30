<template>
  <div class="library-audit admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">训练共享审核</h2>
        <p class="ph-desc">处理用户提交的训练资源入库申请，核准后将向全站公开</p>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table :data="submissions" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="120">
          <template #default="sc">
            <el-tag size="small" effect="plain">{{ sc.row.resourceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourceId" label="资源ID" width="100" />
        <el-table-column prop="submitterId" label="提交人" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="sc">
            <el-tag size="small" :type="sc.row.status === 'PENDING' ? 'warning' : (sc.row.status === 'APPROVED' ? 'success' : 'danger')">
              {{ sc.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="right">
          <template #default="sc">
            <el-button type="info" size="small" link @click="previewResource(sc.row)">
               完整预览审核
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
    <el-drawer v-model="drawerVisible" title="资源入库终审" size="550px" destroy-on-close>
       <div v-if="loadingResource" v-loading="loadingResource" style="height:200px"></div>
       <div v-else-if="currentResource" style="padding: 24px;">
          <div class="audit-header">
             <el-tag :type="currentSubmission.resourceType === 'PLAN' ? 'primary' : 'success'">{{ currentSubmission.resourceType }}</el-tag>
             <h3 class="audit-title">{{ currentResource.title || currentResource.name }}</h3>
          </div>
          <p class="audit-desc">{{ currentResource.description || currentResource.category || '暂无详细描述' }}</p>
          
          <el-divider>结构排期</el-divider>
          <div v-if="currentSubmission.resourceType === 'PLAN'" class="audit-struct">
             <div v-for="(act, idx) in parsedActions" :key="idx" class="audit-day">
                <span class="day-lbl">Day {{ idx + 1 }} · {{ act.type }}</span>
                <span v-for="(a, aidx) in act.actions" :key="aidx" class="a-tag">{{ a.name }}</span>
             </div>
          </div>
          <div v-else class="audit-struct">
             <el-timeline>
                <el-timeline-item v-for="(act, idx) in parsedActions" :key="idx" type="primary">
                   <strong>{{ act.name }}</strong> ({{ act.sets }})
                </el-timeline-item>
             </el-timeline>
          </div>

          <el-divider border-style="dashed" />
          <div style="display:flex; gap:16px; margin-top: 32px;">
             <el-button style="flex:1" type="danger" round plain @click="rejectSubmission(currentSubmission.id); drawerVisible=false">判定违规/驳回</el-button>
             <el-button style="flex:1" type="success" round @click="approveSubmission(currentSubmission.id); drawerVisible=false">核准入库</el-button>
          </div>
       </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const submissions = ref<any[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const currentSubmission = ref<any>(null)
const currentResource = ref<any>(null)
const loadingResource = ref(false)
const parsedActions = ref<any[]>([])

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/resource/admin/submissions', { params: { status: 'PENDING' } })
    submissions.value = res.data || []
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
     if (currentResource.value) {
        const field = sub.resourceType === 'PLAN' ? 'actions' : 'actionsJson'
        try { parsedActions.value = JSON.parse(currentResource.value[field]) } catch(e) { parsedActions.value = [] }
     }
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

onMounted(fetchSubmissions)
</script>

<style scoped>
.page-header { margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.audit-header { display:flex; gap:12px; align-items:center; margin-bottom: 16px; }
.audit-title { font-size: 20px; font-weight: 800; margin: 0; }
.audit-desc { font-size: 14px; color: #64748b; line-height: 1.6; }
.audit-day { border-bottom: 1px solid #f1f5f9; padding: 12px 0; display:flex; flex-direction:column; gap:8px;}
.day-lbl { font-size: 12px; font-weight: 800; color: #3b82f6; }
.a-tag { background: #f8fafc; padding: 4px 8px; border-radius: 4px; font-size: 12px; width: fit-content; }
</style>
