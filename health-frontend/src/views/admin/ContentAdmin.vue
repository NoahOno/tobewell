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
                  <el-button type="primary" size="small" link @click="previewContent(sc.row)">预览正文</el-button>
                  <el-popconfirm title="确定下架并删除该帖子吗？" @confirm="handleDeletePost(sc.row.id)">
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
                  <el-button v-if="sc.row.status === 'PENDING'" type="success" size="small" link @click="approveSubmission(sc.row.id)">通过</el-button>
                  <el-popconfirm v-if="sc.row.status === 'PENDING'" title="确定驳回该入库申请吗？" @confirm="rejectSubmission(sc.row.id)">
                    <template #reference>
                      <el-button type="danger" size="small" link>驳回</el-button>
                    </template>
                  </el-popconfirm>
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

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" :title="'标题预览: ' + currentPost?.title" width="650px" class="premium-dialog">
      <div v-if="currentPost" class="post-preview-content">
         <p class="preview-text">{{ currentPost.content }}</p>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false" round>关闭预览</el-button>
      </template>
    </el-dialog>

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
  await request.post(`/resource/admin/submissions/${id}/reject`, { note: '' })
  ElMessage.success('已驳回')
  fetchSubmissions()
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
</style>
