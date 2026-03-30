<template>
  <div class="content-admin admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">社区内容治理</h2>
        <p class="ph-desc">维护社区健康环境，审查及清理违规帖子与评论内容</p>
      </div>
    </div>
    
    <div class="admin-tabs-container">
      <el-tabs v-model="activeTab" class="premium-tabs" @tab-change="handleTabChange">
        <!-- 1. Post Management -->
        <el-tab-pane label="帖子审核 (Posts)" name="posts">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="posts" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="帖子标题" min-width="250">
                <template #default="sc">
                   <div class="post-title-cell">
                     <span class="p-title">{{ sc.row.title }}</span>
                     <span class="p-author">用户ID: {{ sc.row.userId }}</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column label="板块" width="120">
                <template #default="sc">
                  <el-tag size="small" effect="plain">{{ sc.row.category || '综合' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="likeCount" label="获赞" width="100" />
              <el-table-column label="发布时间" width="180">
                <template #default="sc">
                  <span class="date-text">{{ new Date(sc.row.createTime).toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="160" align="right">
                <template #default="sc">
                  <el-button type="primary" size="small" link @click="previewContent(sc.row)">
                    <el-icon><View /></el-icon> 预览体验
                  </el-button>
                  <el-popconfirm title="确定删除该社区内容吗？" @confirm="handleDeletePost(sc.row.id)">
                    <template #reference>
                      <el-button type="danger" size="small" link>删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 2. Comment Management -->
        <el-tab-pane label="评论清理 (Comments)" name="comments">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="comments" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="content" label="评论正文" min-width="300" />
              <el-table-column label="来源信息" width="200">
                <template #default="sc">
                   <div class="comment-meta">
                     <span class="c-user">UID: {{ sc.row.userId }}</span>
                     <span class="c-target">所属 {{ sc.row.targetType }} {{ sc.row.targetId }}</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column label="评论时间" width="180">
                <template #default="sc">
                  <span class="date-text">{{ new Date(sc.row.createTime).toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="right">
                <template #default="sc">
                  <el-popconfirm title="确定彻底移除该评论吗？" @confirm="handleDeleteComment(sc.row.id)">
                    <template #reference>
                      <el-button type="danger" size="small" link>删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="训练资源入库审核" name="submissions">
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
              <el-table-column label="提交时间" width="180">
                <template #default="sc">
                  <span class="date-text">{{ sc.row.createTime ? new Date(sc.row.createTime).toLocaleString() : '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" align="right">
                <template #default="sc">
                  <el-button type="info" size="small" link @click="previewResource(sc.row)">
                     <el-icon><View /></el-icon> 完整预览审核
                  </el-button>
                  <el-button v-if="sc.row.status === 'PENDING'" type="success" size="small" link @click="approveSubmission(sc.row.id)">
                     <el-icon><Check /></el-icon> 通过
                  </el-button>
                  <el-button v-if="sc.row.status === 'PENDING'" type="danger" size="small" link @click="rejectSubmission(sc.row.id)">
                     <el-icon><Close /></el-icon> 驳回
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && submissions.length === 0" description="暂无入库审核任务" :image-size="90" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="活动管理" name="activities">
          <div style="display:flex; justify-content: flex-end; margin-top: 12px;">
            <el-button type="primary" round @click="openNewActivity">新增活动</el-button>
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
              <el-table-column label="要求/置顶" width="130">
                <template #default="sc">
                  <div class="act-tags">
                    <el-tag v-if="sc.row.pinned === 1" type="success" size="small" effect="plain">置顶</el-tag>
                    <el-tag type="info" size="small" effect="plain">{{ sc.row.requiredDays }}天</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="sc">
                  <el-tag :type="sc.row.status === 'ONLINE' ? 'info' : 'danger'" size="small">{{ sc.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="260" align="right">
                <template #default="sc">
                  <el-button size="small" link @click="editActivity(sc.row)">编辑</el-button>
                  <el-button size="small" link type="warning" @click="togglePin(sc.row.id, sc.row.pinned)">
                    {{ sc.row.pinned === 1 ? '取消置顶' : '置顶' }}
                  </el-button>
                  <el-button v-if="sc.row.status === 'ONLINE'" size="small" link type="danger" @click="offlineActivity(sc.row.id)">下线</el-button>
                  <el-button size="small" link @click="showAnalytics(sc.row.id)">统计</el-button>
                  <el-popconfirm title="确定删除该活动吗？" @confirm="deleteActivity(sc.row.id)">
                    <template #reference>
                      <el-button size="small" type="danger" link>删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && activities.length === 0" description="暂无活动" :image-size="90" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    
    <!-- Resource Submission Full Preview Drawer -->
    <el-drawer v-model="resourcePreviewVisible" title="入库资源全览体验" size="600px" destroy-on-close>
       <div v-if="loadingResource" v-loading="loadingResource" style="height:200px"></div>
       <div v-else-if="currentResource" class="resource-preview-container">
          <div class="rp-header">
             <div class="rp-tag"><el-tag :type="currentSubmission.resourceType === 'PLAN' ? 'primary' : 'success'">{{ currentSubmission.resourceType }}</el-tag></div>
             <div class="rp-title">{{ currentResource.title || currentResource.name }}</div>
          </div>
          <div class="rp-desc">{{ currentResource.description || currentResource.category || '暂无详细描述' }}</div>
          
          <div class="rp-section-title">内容排期 / 动作详情</div>
          
          <!-- Shared Preview Logic Similar to Explore.vue -->
          <div v-if="currentSubmission.resourceType === 'PLAN'" class="rp-struct">
              <div v-for="(act, idx) in parsedResourceActions" :key="idx" class="rp-day-card">
                 <div class="rp-day-lbl">Day {{ act.day || idx + 1 }} - {{ act.type }}</div>
                 <div class="rp-day-title">{{ act.title }}</div>
                 <div class="rp-acts">
                    <span class="rp-act-badge" v-for="(a, aidx) in act.actions" :key="aidx">{{ a.name }} [{{ a.sets }}]</span>
                 </div>
              </div>
          </div>
          <div v-else class="rp-struct">
              <el-timeline>
                 <el-timeline-item v-for="(act, idx) in parsedResourceActions" :key="idx" type="primary">
                    <div class="rp-timeline-card">
                       <span class="rtc-name">{{ act.name }}</span>
                       <span class="rtc-sets">推荐: {{ act.sets }} / 休息: {{ act.rest }}</span>
                    </div>
                 </el-timeline-item>
              </el-timeline>
          </div>
          
          <el-divider>管理员审核决断</el-divider>
          <div class="cd-actions" style="display:flex; gap:16px;">
             <el-button style="flex:1" type="danger" round plain @click="rejectSubmission(currentSubmission.id); resourcePreviewVisible=false">判定违规/驳回</el-button>
             <el-button style="flex:1" type="success" round @click="approveSubmission(currentSubmission.id); resourcePreviewVisible=false">核准入库 (全局可见)</el-button>
          </div>
       </div>
       <div v-else>
          <el-empty description="无法拉取到该私有资源，用户可能已彻底修改或损毁" />
       </div>
    </el-drawer>

<!-- Preview Dialog -->
        <el-drawer v-model="previewVisible" title="社区内容全览" size="500px" class="community-drawer" destroy-on-close>
      <div v-if="currentPost" class="cd-container">
        <div class="cd-header">
           <div class="cd-title">{{ currentPost.title }}</div>
           <div class="cd-meta">发布者UID: {{ currentPost.userId }} · 板块分类: <el-tag size="small">{{ currentPost.category || '综合交流' }}</el-tag></div>
        </div>
        <div class="cd-body">
           <p class="cd-text">{{ currentPost.content }}</p>
        </div>
        <div class="cd-metrics">
           <div class="metric-badge"><el-icon><ThumbsUp /></el-icon> 获赞 {{ currentPost.likeCount || 0 }}</div>
           <div class="metric-badge"><el-icon><Calendar /></el-icon> {{ new Date(currentPost.createTime).toLocaleString() }}</div>
        </div>
        
        <el-divider border-style="dashed">治理操作</el-divider>
        <div class="cd-actions">
           <el-button type="danger" round style="width:100%" @click="handleDeletePost(currentPost.id); previewVisible=false">违规一键清理 (彻底移除帖子)</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="activityDialogVisible" :title="activityForm.id ? '编辑活动' : '新增活动'" width="720px" class="premium-dialog">
      <el-form :model="activityForm" label-position="top">
        <el-form-item label="活动标题"><el-input v-model="activityForm.title" /></el-form-item>
        <el-form-item label="封面图 (URL)"><el-input v-model="activityForm.coverImage" placeholder="https://..." /></el-form-item>
        <el-form-item label="活动详情 HTML"><el-input v-model="activityForm.descriptionHtml" type="textarea" :rows="6" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间"><el-date-picker v-model="activityForm.startTime" type="datetime" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间"><el-date-picker v-model="activityForm.endTime" type="datetime" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="模板类型">
              <el-select v-model="activityForm.templateType" style="width:100%">
                <el-option label="计划(PLAN)" value="PLAN" />
                <el-option label="课程(COURSE)" value="COURSE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板ID"><el-input-number v-model="activityForm.templateId" :min="1" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="连续天数"><el-input-number v-model="activityForm.requiredDays" :min="1" :max="365" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-switch v-model="activityForm.pinned" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="activityForm.status" style="width:100%">
                <el-option label="在线" value="ONLINE" />
                <el-option label="下线" value="OFFLINE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="saveActivity" round>保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="analyticsDialogVisible" title="活动统计" width="520px" class="premium-dialog">
      <div class="analytics-grid">
        <div class="analytics-item">
          <div class="ai-label">参与人数</div>
          <div class="ai-value">{{ analyticsData.totalParticipants }}</div>
        </div>
        <div class="analytics-item">
          <div class="ai-label">日活跃</div>
          <div class="ai-value">{{ analyticsData.dailyActive }}</div>
        </div>
        <div class="analytics-item">
          <div class="ai-label">完成人数</div>
          <div class="ai-value">{{ analyticsData.completedParticipants }}</div>
        </div>
        <div class="analytics-item">
          <div class="ai-label">完成率</div>
          <div class="ai-value">{{ analyticsData.completionRate }}%</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="analyticsDialogVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const activeTab = ref('posts')
const posts = ref<any[]>([])
const comments = ref<any[]>([])
const submissions = ref<any[]>([])
const activities = ref<any[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const currentPost = ref<any>(null)
const activityDialogVisible = ref(false)
const analyticsDialogVisible = ref(false)

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

const analyticsData = ref<any>({
  totalParticipants: 0,
  dailyActive: 0,
  completedParticipants: 0,
  completionRate: 0
})

const handleTabChange = () => {
  if (activeTab.value === 'posts') fetchPosts()
  if (activeTab.value === 'comments') fetchComments()
  if (activeTab.value === 'submissions') fetchSubmissions()
  if (activeTab.value === 'activities') fetchActivities()
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/posts')
    posts.value = res.data
  } finally { loading.value = false }
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/comments')
    comments.value = res.data
  } finally { loading.value = false }
}

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/resource/admin/submissions', { params: { status: 'PENDING' } })
    submissions.value = res.data || []
  } finally { loading.value = false }
}

const approveSubmission = async (id: number) => {
  await request.post(`/resource/admin/submissions/${id}/approve`)
  ElMessage.success('已通过并入库')
  fetchSubmissions()
}

const rejectSubmission = async (id: number) => {
  try {
    await request.post(`/resource/admin/submissions/${id}/reject`, { note: '管理驳回' })
    ElMessage.success('已驳回处理完成')
    fetchSubmissions()
  } catch(e){}
}

const formatDateTime = (val: any) => {
  if (!val) return '-'
  return new Date(val).toLocaleString()
}

const fetchActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/activities')
    activities.value = res.data || []
  } finally {
    loading.value = false
  }
}

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
  analyticsData.value.totalParticipants = res?.data?.totalParticipants ?? 0
  analyticsData.value.dailyActive = res?.data?.dailyActive ?? 0
  analyticsData.value.completedParticipants = res?.data?.completedParticipants ?? 0
  analyticsData.value.completionRate = res?.data?.completionRate ?? 0
  analyticsDialogVisible.value = true
}

const deleteActivity = async (id: number) => {
  await request.delete(`/admin/activities/${id}`)
  ElMessage.success('活动已删除')
  fetchActivities()
}

const handleDeletePost = async (id: number) => {
  await request.delete(`/admin/post/${id}`)
  ElMessage.success('帖子已彻底删除')
  fetchPosts()
}

const handleDeleteComment = async (id: number) => {
  await request.delete(`/admin/comment/${id}`)
  ElMessage.success('评论已彻底移除')
  fetchComments()
}

const previewContent = (post: any) => {
  currentPost.value = post
  previewVisible.value = true
}

onMounted(fetchPosts)
</script>

<style scoped>
.content-admin {
  animation: fadeIn 0.4s ease-out;
}

.page-header {
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

.admin-tabs-container :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.admin-tabs-container :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-muted);
  transition: all 0.3s;
}

.admin-tabs-container :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.table-card {
  padding: 0;
  margin-top: 16px;
}

.post-title-cell {
  display: flex;
  flex-direction: column;
}

.p-title {
  font-weight: 700;
  color: var(--text-main);
}

.p-author {
  font-size: 12px;
  color: var(--text-light);
}

.comment-meta {
  display: flex;
  flex-direction: column;
  font-size: 12px;
}

.c-user {
  font-weight: 600;
  color: var(--text-muted);
}

.c-target {
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
  color: var(--text-muted);
}
.act-tags {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.analytics-item {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px;
}
.ai-label {
  font-size: 12px;
  color: var(--text-light);
}
.ai-value {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-main);
}

.date-text {
  font-size: 13px;
  color: var(--text-muted);
}

.preview-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-main);
  font-size: 15px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Community Posts Stylings */
.cd-container { padding: 8px; }
.cd-header { margin-bottom: 24px; }
.cd-title { font-size: 22px; font-weight:800; color: #1e293b; margin-bottom: 8px;}
.cd-meta { font-size: 13px; color: #64748b; }
.cd-body { background: #f8fafc; padding: 20px; border-radius: 12px; font-size: 15px; color: #334155; line-height: 1.8; margin-bottom: 24px;}
.cd-metrics { display:flex; gap: 16px; margin-bottom: 24px; }
.metric-badge { display:flex; align-items:center; gap:6px; background: #eff6ff; color: #3b82f6; padding: 6px 16px; border-radius:100px; font-size:13px; font-weight:600;}

/* Resource Preview Stylings */
.resource-preview-container { display: flex; flex-direction: column; gap: 16px; padding:0 8px; }
.rp-header { display:flex; gap:12px; align-items:center;}
.rp-title { font-size: 24px; font-weight: 800; color: #0f172a;}
.rp-desc { font-size: 14px; color: #64748b; line-height:1.6; background: #f8fafc; padding:12px; border-radius:8px;}
.rp-section-title { font-weight: 700; color:#1e293b; border-left: 4px solid #3b82f6; padding-left: 8px; margin-top:20px; margin-bottom: 12px;}
.rp-day-card { background: white; border: 1px solid #e2e8f0; padding:16px; border-radius: 8px; margin-bottom: 12px;}
.rp-day-lbl { font-size: 13px; font-weight: 700; color: #3b82f6;}
.rp-day-title { font-size: 15px; color: #1e293b; margin: 4px 0 12px;}
.rp-acts { display:flex; flex-wrap:wrap; gap:8px;}
.rp-act-badge { font-size: 12px; background: #f1f5f9; padding: 4px 10px; border-radius:4px; color: #475569;}
.rp-timeline-card { background: #f8fafc; padding: 12px; border-radius:8px; display:flex; flex-direction:column; gap:6px;}
.rtc-name { font-weight:600; color: #1e293b;}
.rtc-sets { font-size:13px; color: #64748b;}

</style>
