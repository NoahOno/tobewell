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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const activeTab = ref('posts')
const posts = ref<any[]>([])
const comments = ref<any[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const currentPost = ref<any>(null)

const handleTabChange = () => {
  if (activeTab.value === 'posts') fetchPosts()
  if (activeTab.value === 'comments') fetchComments()
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
