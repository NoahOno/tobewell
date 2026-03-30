<template>
  <div class="community-posts admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">帖子与评论管理</h2>
        <p class="ph-desc">维护社区健康环境，审查及清理违规帖子与评论内容</p>
      </div>
    </div>

    <div class="admin-tabs-container">
      <el-tabs v-model="activeTab" class="premium-tabs">
        <el-tab-pane label="帖子审核" name="posts">
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
                    预览
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

        <el-tab-pane label="评论清理" name="comments">
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

    <!-- Preview Drawer -->
    <el-drawer v-model="previewVisible" title="社区内容全览" size="500px" destroy-on-close>
      <div v-if="currentPost" class="cd-container">
        <div class="cd-header">
           <div class="cd-title">{{ currentPost.title }}</div>
           <div class="cd-meta">发布者UID: {{ currentPost.userId }} · 分类: {{ currentPost.category || '综合交流' }}</div>
        </div>
        <div class="cd-body">
           <p class="cd-text">{{ currentPost.content }}</p>
        </div>
        
        <el-divider border-style="dashed">治理操作</el-divider>
        <el-button type="danger" round style="width:100%" @click="handleDeletePost(currentPost.id); previewVisible=false">违规一键清理</el-button>
      </div>
    </el-drawer>
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
  ElMessage.success('帖子已删除')
  fetchPosts()
}

const handleDeleteComment = async (id: number) => {
  await request.delete(`/admin/comment/${id}`)
  ElMessage.success('评论已移除')
  fetchComments()
}

const previewContent = (post: any) => {
  currentPost.value = post
  previewVisible.value = true
}

onMounted(() => {
  fetchPosts()
  fetchComments()
})
</script>

<style scoped>
.page-header { margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0 0 8px; }
.ph-desc { font-size: 15px; color: var(--text-muted); }
.post-title-cell { display: flex; flex-direction: column; }
.p-title { font-weight: 700; color: var(--text-main); }
.p-author { font-size: 12px; color: var(--text-light); }
.date-text { font-size: 13px; color: var(--text-muted); }
.cd-title { font-size: 20px; font-weight: 800; margin-bottom: 8px; }
.cd-meta { font-size: 13px; color: #64748b; margin-bottom: 16px; }
.cd-body { background: #f8fafc; padding: 20px; border-radius: 12px; line-height: 1.6; }
</style>
