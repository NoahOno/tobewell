<template>
  <div class="community-page">
    <!-- Top Search Bar -->
    <div class="community-topbar">
      <div class="search-area">
        <el-input
          v-model="searchQ"
          placeholder="搜索帖子、内容或 #标签..."
          class="community-search"
          clearable
          @clear="clearSearch"
          @keyup.enter="doSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" round @click="doSearch">搜索</el-button>
      </div>
      <el-button type="primary" :icon="EditPen" round @click="openPostDialog">发帖</el-button>
    </div>

    <!-- Cross Layout Body -->
    <div class="community-body">
      <!-- Left Sidebar -->
      <aside class="community-sidebar">
        <el-menu
          :default-active="activeTab"
          class="sidebar-nav"
          @select="handleTabChange"
        >
          <el-menu-item index="recommend">
            <el-icon><Star /></el-icon><span>推荐</span>
          </el-menu-item>
          <el-menu-item index="following">
            <el-icon><User /></el-icon><span>关注</span>
          </el-menu-item>
          <el-menu-item index="hot">
            <el-icon><Trophy /></el-icon><span>热门</span>
          </el-menu-item>
          <el-menu-item index="mine">
            <el-icon><EditPen /></el-icon><span>我的发帖</span>
          </el-menu-item>
          <el-menu-item index="activityCenter">
            <el-icon><Management /></el-icon><span>活动中心</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- Center Post Feed -->
      <main class="community-feed" v-loading="loading">
        <template v-if="activeTab === 'activityCenter'">
          <div class="activity-list-wrap">
            <div class="activity-list-header">
              <div class="activity-list-title">活动中心</div>
              <div class="activity-list-sub">点击活动卡片查看详情，报名后从活动开始时间起插入连续训练任务</div>
            </div>

            <div v-if="activities.length === 0" class="empty-subtle">
              暂无活动
            </div>

            <div v-else class="activity-card-grid">
              <div
                v-for="act in activities"
                :key="act.id"
                class="activity-card"
              >
                <div class="activity-card-main" @click="openActivityDetail(act.id)">
                  <div class="activity-card-title">{{ act.title }}</div>
                  <div class="activity-card-meta">
                    {{ formatDateRange(act.startTime, act.endTime) }} · {{ act.requiredDays }} 天
                    <span v-if="act.templateType"> · {{ act.templateType }}</span>
                  </div>
                  <div class="activity-card-tags">
                    <el-tag v-if="act.pinned === 1" type="success" size="small" effect="plain">置顶</el-tag>
                    <el-tag :type="act.status === 'ONLINE' ? 'info' : 'danger'" size="small" effect="plain">
                      {{ act.status === 'ONLINE' ? '在线' : '下线' }}
                    </el-tag>
                  </div>
                </div>
                <div class="activity-card-actions">
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    :loading="applyActivityLoadingId === act.id"
                    @click.stop="applyActivity(act.id)"
                  >
                    报名
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <!-- Search results indicator -->
          <div v-if="isSearchMode" class="search-result-bar">
            <span>搜索「{{ lastSearchQ }}」的结果 · {{ posts.length }} 条</span>
            <el-button link @click="clearSearch">清除搜索</el-button>
          </div>

          <div v-if="posts.length === 0 && !loading" class="empty-feed">
            <el-empty :description="isSearchMode ? '没有找到相关帖子' : '暂无帖子，快来发第一帖！'" />
          </div>

          <div
            v-for="post in posts"
            :key="post.id"
            class="post-card premium-card"
            @click="openPost(post)"
          >
            <div class="post-header">
              <el-avatar :size="38" class="post-avatar" @click.stop="showUserCard(post.userId)">{{ (post.nickname || '#')[0] }}</el-avatar>
              <div class="post-author-info">
                <div class="author-row">
                  <span class="author-name" @click.stop="showUserCard(post.userId)">{{ post.nickname || ('用户 #' + post.userId) }}</span>
                </div>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
              <div class="post-header-actions">
                <el-button
                  v-if="post.userId === currentUserId"
                  size="small"
                  link
                  type="danger"
                  @click.stop="deletePost(post.id)"
                >删除</el-button>
              </div>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-excerpt">{{ (post.content || '').slice(0, 120) }}{{ (post.content?.length || 0) > 120 ? '...' : '' }}</p>
            <div class="post-tags-row" v-if="post.tags">
              <el-tag
                v-for="tag in (post.tags || '').split(',')"
                :key="tag"
                size="small"
                type="info"
                effect="plain"
                class="post-tag"
              ># {{ tag.trim() }}</el-tag>
            </div>
            <div class="post-footer">
              <!-- Like (Heart) -->
              <el-button
                link
                class="post-stat-btn like-btn"
                :class="{ 'is-active': post.isLiked }"
                @click.stop="toggleLike(post)"
              >
                <svg viewBox="0 0 1024 1024" width="18" height="18" :fill="post.isLiked ? '#F56C6C' : '#94A3B8'">
                  <path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3 0.1-35.3-7-69.6-20.9-101.9z"></path>
                </svg>
                <span class="stat-count">{{ post.likeCount || 0 }}</span>
              </el-button>

              <!-- Collect (Star) -->
              <el-button
                link
                class="post-stat-btn collect-btn"
                :class="{ 'is-active': post.isCollected }"
                @click.stop="toggleCollect(post)"
              >
                <el-icon><StarFilled v-if="post.isCollected" /><Star v-else /></el-icon>
                <span class="stat-count">{{ post.collectionCount || 0 }}</span>
              </el-button>

              <!-- Comment (Bubble) -->
              <span class="post-stat"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </template>
      </main>

      <!-- Right Sidebar -->
      <aside v-if="activeTab !== 'activityCenter'" class="community-right">
        <div class="right-card premium-card">
          <div class="right-title">🔥 热门话题</div>
          <div
            v-for="(hot, i) in hotPosts"
            :key="hot.id"
            class="hot-item"
            @click="openPost(hot)"
          >
            <span class="hot-rank" :class="`rank-${i+1}`">{{ i + 1 }}</span>
            <span class="hot-text">{{ hot.title }}</span>
          </div>
          <el-empty v-if="hotPosts.length === 0" description="暂无热门" :image-size="60" />
        </div>
        <div class="right-card premium-card activity-trending">
          <div class="right-title">🔥 热门活动</div>
          <div
            v-for="(act, i) in trendingActivities"
            :key="act.id"
            class="hot-item"
            @click="openActivityDetail(act.id)"
          >
            <span class="hot-rank" :class="`rank-${i+1}`">{{ i + 1 }}</span>
            <span class="hot-text">{{ act.title }}</span>
          </div>
          <el-empty v-if="trendingActivities.length === 0" description="暂无热门活动" :image-size="60" />
        </div>
        <div class="right-card premium-card announcement">
          <div class="right-title">📢 社区公告</div>
          <p>欢迎来到健康管理社区！发帖时使用 #话题 可以自动添加标签。请遵守社区规定，保持友善交流。</p>
        </div>
      </aside>
    </div>

    <el-dialog v-model="activityDialogVisible" :title="activityDetail?.title || '活动详情'" width="720px" align-center destroy-on-close>
      <div v-loading="activityDetailLoading" class="activity-modal">
        <div v-if="activityDetail" class="activity-detail-wrap">
          <div class="activity-detail-time">
            {{ formatDateRange(activityDetail.startTime, activityDetail.endTime) }}
          </div>

          <div class="activity-detail-meta-row">
            <el-tag v-if="activityDetail.pinned === 1" type="success" size="small" effect="plain">置顶</el-tag>
            <el-tag :type="activityDetail.status === 'ONLINE' ? 'info' : 'danger'" size="small" effect="plain">
              {{ activityDetail.status === 'ONLINE' ? '在线' : '下线' }}
            </el-tag>
            <el-tag type="primary" size="small" effect="plain">{{ activityDetail.requiredDays }} 天要求</el-tag>
            <el-tag type="warning" size="small" effect="plain">{{ activityDetail.templateType }}</el-tag>
          </div>

          <div class="activity-detail-description" v-if="activityDetail.descriptionHtml" v-html="activityDetail.descriptionHtml" />

          <div class="activity-actions">
            <el-button v-if="activityDetail.id && !isJoined(activityDetail.id)" type="success" round :loading="applyActivityLoadingId === activityDetail.id" @click="applyActivity(activityDetail.id)">立即报名</el-button>
            <el-button v-else type="primary" round plain disabled>已报名</el-button>
          </div>

          <div class="activity-completed-block">
            <div class="completed-block-title">活动内容区（完成用户）</div>

            <div v-if="activityCompletedItems.length === 0" class="empty-subtle">
              暂无完成记录
            </div>

            <div
              v-for="item in activityCompletedItems"
              :key="item.participationId"
              class="completed-item"
            >
              <div class="completed-top">
                <div class="completed-nick">{{ item.nickname || ('用户 #' + item.userId) }}</div>
                <div class="completed-time">{{ formatTime(item.completedTime) }}</div>
              </div>
              <div class="completed-dynamic" v-html="item.content" />
              <div class="completed-actions">
                <el-button size="small" type="primary" plain @click="handleForwardActivityDynamic(item)">转发</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="activityDialogVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>

    <!-- Post Detail Dialog -->
    <el-dialog v-model="postDialogVisible" :title="selectedPost?.title" width="680px" align-center destroy-on-close>
      <div v-if="selectedPost" class="post-detail">
        <div class="detail-meta">
          <span class="detail-author" style="cursor: pointer" @click="showUserCard(selectedPost.userId)">
            {{ selectedPost.nickname || ('用户 #' + selectedPost.userId) }}
          </span>
          <span class="detail-time">{{ formatTime(selectedPost.createTime) }}</span>
        </div>
        <div class="detail-tags" v-if="selectedPost.tags">
          <el-tag v-for="tag in (selectedPost.tags||'').split(',')" :key="tag" size="small" type="info" effect="plain"># {{ tag.trim() }}</el-tag>
        </div>
        <p class="detail-content">{{ selectedPost.content }}</p>
        <div class="detail-actions">
            <el-button 
              :type="selectedPost.isLiked ? 'danger' : 'default'" 
              round 
              @click="toggleLike(selectedPost)"
            >
              <svg viewBox="0 0 1024 1024" width="16" height="16" :fill="selectedPost.isLiked ? '#fff' : '#606266'" style="margin-right: 6px; vertical-align: middle;">
                <path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3 0.1-35.3-7-69.6-20.9-101.9z"></path>
              </svg>
              <span>{{ selectedPost.isLiked ? '已赞' : '点赞' }}</span>
              <span v-if="selectedPost.likeCount" style="margin-left: 4px">({{ selectedPost.likeCount }})</span>
            </el-button>
          <el-button 
            :type="selectedPost.isCollected ? 'warning' : 'default'" 
            round 
            @click="toggleCollect(selectedPost)"
          >
            <el-icon><StarFilled v-if="selectedPost.isCollected" /><Star v-else /></el-icon> 
            <span>{{ selectedPost.isCollected ? '已收藏' : '收藏' }}</span>
            <span v-if="selectedPost.collectionCount" style="margin-left: 4px">({{ selectedPost.collectionCount }})</span>
          </el-button>
          <el-button 
            v-if="selectedPost.userId === currentUserId" 
            type="danger" 
            plain 
            round 
            @click="deletePost(selectedPost.id)"
          >删除</el-button>
        </div>
        <el-divider />
        <div class="comments-area">
          <div class="comments-title">评论 ({{ postComments.length }})</div>
          <div class="comment-input-row">
            <el-input v-model="commentText" placeholder="写下你的想法..." type="textarea" :rows="2" />
            <el-button type="primary" round :disabled="!commentText" @click="submitComment">发布</el-button>
          </div>
          <div v-for="c in postComments" :key="c.id" class="comment-item">
            <div class="c-header">
              <span class="c-user" style="cursor: pointer" @click="showUserCard(c.userId)">
                {{ c.nickname || ('用户 #' + c.userId) }}
              </span>
              <span class="c-time">{{ formatTime(c.createTime) }}</span>
            </div>
            <div class="c-content">{{ c.content }}</div>
          </div>
          <div v-if="postComments.length === 0" class="no-comments">还没有评论，快来发表看法！</div>
        </div>
      </div>
    </el-dialog>

    <!-- User Card Dialog -->
    <el-dialog v-model="userCardVisible" width="320px" :show-header="false" align-center class="user-profile-dialog">
      <div v-if="cardUser" class="user-card-content">
        <el-avatar :size="64" class="card-avatar">{{ (cardUser.nickname || '#')[0] }}</el-avatar>
        <h3 class="card-nickname">{{ cardUser.nickname || ('用户 #' + cardUser.id) }}</h3>
        <div class="card-stats">
          <div class="stat-item"><b>{{ cardUser.followingCount || 0 }}</b><span>关注</span></div>
          <div class="stat-item"><b>{{ cardUser.followerCount || 0 }}</b><span>粉丝</span></div>
        </div>
        <div class="card-actions" v-if="cardUser.id !== currentUserId">
          <el-button 
            :type="cardUser.isFollowing ? 'default' : 'primary'" 
            round 
            block 
            @click="toggleFollow(cardUser)"
            :loading="followLoading"
          >
            {{ cardUser.isFollowing ? '已关注' : '关注' }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- Create Post Dialog -->
    <el-dialog v-model="createDialogVisible" title="发表新帖子" width="600px" align-center destroy-on-close>
      <el-form :model="postForm" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="给帖子一个吸引人的标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input 
            v-model="postForm.content" 
            type="textarea" 
            :rows="8" 
            placeholder="分享你的健康心得... 使用 #关键词 自动生成标签" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Star, User, Trophy, EditPen, ChatDotRound, StarFilled, Management } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const loading = ref(false)
const posts = ref<any[]>([])
const hotPosts = ref<any[]>([])
const postComments = ref<any[]>([])
const activeTab = ref('recommend')
const activeCategory = ref('')
const searchQ = ref('')
const lastSearchQ = ref('')
const isSearchMode = ref(false)

const postDialogVisible = ref(false)
const createDialogVisible = ref(false)
const userCardVisible = ref(false)
const followLoading = ref(false)
const selectedPost = ref<any>(null)
const cardUser = ref<any>(null)
const commentText = ref('')

// Activities (Community + Admin)
const activities = ref<any[]>([])
const trendingActivities = ref<any[]>([])
const activeActivityId = ref<number | null>(null)
const activityCompletedItems = ref<any[]>([])
const applyActivityLoadingId = ref<number | null>(null)
const activityDialogVisible = ref(false)
const activityDetailLoading = ref(false)
const activityDetail = ref<any>(null)

// Mocking current user ID (should ideally come from store)
const currentUserId = ref(0) 

const postForm = reactive({
  title: '',
  content: '',
  category: '综合' // Kept for backend compatibility but hidden in UI
})

const activeActivity = computed(() => {
  if (activeActivityId.value == null) return null
  return activities.value.find(a => String(a.id) === String(activeActivityId.value)) || null
})

const fetchUserInfo = async () => {
  try {
    const res: any = await request.get('/auth/info')
    currentUserId.value = res.data.id
  } catch (e) {}
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/community/posts', {
      params: { tab: activeTab.value }
    })
    // Initialize interaction states
    posts.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchHotPosts = async () => {
  try {
    const res: any = await request.get('/community/hot')
    hotPosts.value = res.data
  } catch (e) {}
}

const fetchActivities = async () => {
  try {
    const res: any = await request.get('/activity/list')
    activities.value = res.data || []
  } catch (e) {}
}

const fetchTrendingActivities = async () => {
  try {
    const res: any = await request.get('/activity/trending')
    trendingActivities.value = res.data || []
  } catch (e) {}
}

const fetchActivityCompleted = async (activityId: number) => {
  try {
    const res: any = await request.get(`/activity/${activityId}/completed`)
    const items = res?.data?.items || []
    activityCompletedItems.value = items
  } catch (e) {
    activityCompletedItems.value = []
  }
}

const openActivityDetail = async (activityId: number) => {
  activityDialogVisible.value = true
  activityDetailLoading.value = true
  activeActivityId.value = activityId
  try {
    if (activities.value.length === 0) await fetchActivities()
    const res: any = await request.get(`/activity/${activityId}`)
    activityDetail.value = res.data
    await fetchActivityCompleted(activityId)
  } catch (e) {
    activityDetail.value = null
    activityCompletedItems.value = []
  } finally {
    activityDetailLoading.value = false
  }
}

const applyActivity = async (activityId: number) => {
  applyActivityLoadingId.value = activityId
  try {
    await request.post(`/activity/${activityId}/apply`)
    // Reload completion area
    await fetchActivityCompleted(activityId)
    ElMessage.success('报名成功')
    // Keep local activity list fresh (optional)
    await fetchActivities()
  } catch (e) {
    // backend error will already be shown by request interceptor
  } finally {
    applyActivityLoadingId.value = null
  }
}

const handleForwardActivityDynamic = (item: any) => {
  const title = `我完成了活动《${activityDetail.value?.title || activeActivity.value?.title || '活动'}》`
  const content = `${item.content}\n\n#活动打卡 #健康管理`
  openPostDialog({ title, content })
}

const formatDateRange = (start: any, end: any) => {
  if (!start) return ''
  const s = new Date(start)
  const e = end ? new Date(end) : null
  const sm = `${s.getMonth() + 1}`.padStart(2, '0')
  const sd = `${s.getDate()}`.padStart(2, '0')
  if (!e) return `${sm}-${sd}`
  const em = `${e.getMonth() + 1}`.padStart(2, '0')
  const ed = `${e.getDate()}`.padStart(2, '0')
  return `${sm}-${sd} ~ ${em}-${ed}`
}

const doSearch = async () => {
  if (!searchQ.value.trim()) return
  loading.value = true
  isSearchMode.value = true
  lastSearchQ.value = searchQ.value
  try {
    const res: any = await request.get('/community/search', {
      params: { q: searchQ.value }
    })
    posts.value = res.data
  } catch (e) {} finally {
    loading.value = false
  }
}

const clearSearch = () => {
  isSearchMode.value = false
  searchQ.value = ''
  fetchPosts()
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  isSearchMode.value = false
  if (tab === 'activityCenter') {
    activeActivityId.value = null
    fetchActivities()
    fetchTrendingActivities()
    return
  }
  activeActivityId.value = null
  fetchPosts()
}

const openPost = async (post: any) => {
  selectedPost.value = post
  postDialogVisible.value = true
  try {
    const res: any = await request.get(`/community/post/${post.id}/comments`)
    postComments.value = res.data
  } catch (e) {}
}

const toggleLike = async (post: any) => {
  const originalState = post.isLiked
  const originalCount = post.likeCount || 0
  
  // Optimistic UI Update
  post.isLiked = !originalState
  post.likeCount = originalState ? Math.max(0, originalCount - 1) : originalCount + 1

  try {
    if (originalState) {
      await request.delete(`/community/post/${post.id}/like`)
    } else {
      await request.post(`/community/post/${post.id}/like`)
      ElMessage.success('已点赞')
    }
  } catch (e) {
    // Rollback
    post.isLiked = originalState
    post.likeCount = originalCount
    ElMessage.error('操作失败')
  }
}

const toggleCollect = async (post: any) => {
  const originalState = post.isCollected
  const originalCount = post.collectionCount || 0

  // Optimistic UI Update
  post.isCollected = !originalState
  post.collectionCount = originalState ? Math.max(0, originalCount - 1) : originalCount + 1

  try {
    if (originalState) {
      await request.delete('/interaction/collect', {
        params: { targetId: post.id, targetType: 'POST' }
      })
      ElMessage.success('已取消收藏')
    } else {
      await request.post('/interaction/collect', {
        targetId: post.id,
        targetType: 'POST',
        targetTitle: post.title
      })
      ElMessage.success('已加入收藏')
    }
  } catch (e) {
    // Rollback
    post.isCollected = originalState
    post.collectionCount = originalCount
    ElMessage.error('操作失败')
  }
}

const showUserCard = async (userId: number) => {
  try {
    const res: any = await request.get(`/community/user/${userId}`)
    cardUser.value = res.data
    userCardVisible.value = true
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }
}

const toggleFollow = async (user: any) => {
  followLoading.value = true
  const originalState = user.isFollowing
  try {
    if (originalState) {
      await request.delete(`/community/follow/${user.id}`)
      user.isFollowing = false
      user.followerCount--
      ElMessage.success('已取消关注')
    } else {
      await request.post(`/community/follow/${user.id}`)
      user.isFollowing = true
      user.followerCount++
      ElMessage.success('关注成功')
    }
    // Refresh feed if in following tab
    if (activeTab.value === 'following') fetchPosts()
  } catch (e) {
  } finally {
    followLoading.value = false
  }
}

const deletePost = async (postId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', { type: 'warning' })
    await request.delete(`/community/post/${postId}`)
    ElMessage.success('删除成功')
    if (selectedPost.value?.id === postId) postDialogVisible.value = false
    fetchPosts()
  } catch (e) {}
}

const submitComment = async () => {
  if (!commentText.value || !selectedPost.value) return
  const originalCommentCount = selectedPost.value.commentCount || 0
  const originalComments = [...postComments.value]
  const content = commentText.value

  // Optimistic UI: Add a temporary comment and increment count
  const tempComment = {
    id: Date.now(),
    userId: currentUserId.value,
    nickname: '我', // Placeholder
    content: content,
    createTime: new Date().toISOString()
  }
  postComments.value = [tempComment, ...originalComments]
  selectedPost.value.commentCount = originalCommentCount + 1
  commentText.value = ''

  try {
    await request.post(`/community/post/${selectedPost.value.id}/comment`, {
      content: content
    })
    // Fetch actual comments to sync with database IDs
    const res: any = await request.get(`/community/post/${selectedPost.value.id}/comments`)
    postComments.value = res.data
    ElMessage.success('评论发布成功')
  } catch (e) {
    // Rollback
    postComments.value = originalComments
    selectedPost.value.commentCount = originalCommentCount
    commentText.value = content
    ElMessage.error('回复失败')
  }
}

const openPostDialog = (prefill?: { title?: string; content?: string }) => {
  Object.assign(postForm, {
    title: prefill?.title || '',
    content: prefill?.content || ''
  })
  createDialogVisible.value = true
}

const submitPost = async () => {
  if (!postForm.title || !postForm.content) return ElMessage.warning('请填写标题和内容')
  try {
    await request.post('/community/post', { ...postForm })
    createDialogVisible.value = false
    ElMessage.success('发布成功')
    fetchPosts()
  } catch (e) {}
}

const formatTime = (t: string) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  fetchUserInfo()
  fetchPosts()
  fetchHotPosts()
  fetchActivities()
  fetchTrendingActivities()
})
</script>

<style scoped>
.community-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* Top Search Bar */
.community-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #F1F5F9;
  flex-shrink: 0;
  gap: 16px;
}

.search-area {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  max-width: 640px;
}

.community-search {
  flex: 1;
}

.community-search :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.search-type-toggle {
  flex-shrink: 0;
}

/* Cross Layout */
.community-body {
  display: grid;
  grid-template-columns: 200px 1fr 260px;
  gap: 0;
  flex: 1;
  overflow: hidden;
}

/* Left Sidebar */
.community-sidebar {
  border-right: 1px solid #F1F5F9;
  background: white;
  padding: 16px 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-nav {
  border: none !important;
}

.sidebar-nav :deep(.el-menu-item) {
  border-radius: 10px;
  height: 44px;
  line-height: 44px;
  margin-bottom: 2px;
}

.sidebar-nav :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9) !important;
  color: var(--el-color-primary) !important;
}

.sidebar-divider {
  height: 1px;
  background: #F1F5F9;
  margin: 8px 0;
}

.sidebar-section-title {
  font-size: 12px;
  font-weight: 700;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0 8px;
  margin-bottom: 8px;
}

.category-tags {
  padding: 0 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-tag {
  border-radius: 8px !important;
  cursor: pointer;
  width: 100%;
  justify-content: flex-start !important;
}

.post-header-actions {
  margin-left: auto;
}

/* Center Feed */
.community-feed {
  overflow-y: auto;
  padding: 20px 24px;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-result-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #EFF6FF;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  color: #3B82F6;
}

.empty-feed {
  padding: 60px 0;
}

.post-card {
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.08);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.post-avatar {
  background: linear-gradient(135deg, #4ADE80, #3B82F6);
  color: white;
  font-size: 12px;
  flex-shrink: 0;
}

.post-author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 700;
  font-size: 14px;
  color: #1E293B;
}

.post-time {
  font-size: 12px;
  color: #94A3B8;
}

.post-category-tag {
  margin-left: auto;
}

.post-title {
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
  line-height: 1.4;
}

.post-excerpt {
  color: #64748B;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px;
}

.post-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.post-tag {
  font-size: 12px;
}

.post-footer {
  display: flex;
  gap: 20px;
  border-top: 1px solid #F1F5F9;
  padding-top: 12px;
  align-items: center;
}

.post-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #64748B;
  font-weight: 600;
}

.post-stat-btn {
  padding: 0 4px;
  height: auto;
  font-size: 18px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-count {
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
}

.post-stat-btn.is-active.like-btn {
  color: #F56C6C !important;
}

.post-stat-btn.is-active.collect-btn {
  color: #EAB308 !important;
}

/* Right Sidebar */
.community-right {
  border-left: 1px solid #F1F5F9;
  background: white;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-card {
  padding: 16px;
}

.right-title {
  font-size: 15px;
  font-weight: 800;
  color: #1E293B;
  margin-bottom: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  cursor: pointer;
  transition: color 0.2s;
  border-bottom: 1px solid #f8fafc;
}

.hot-item:hover { color: var(--el-color-primary); }

.hot-rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #F1F5F9;
  color: #64748B;
  flex-shrink: 0;
}

.rank-1 { background: #FEF3C7; color: #D97706; }
.rank-2 { background: #F3F4F6; color: #6B7280; }
.rank-3 { background: #FEE2E2; color: #DC2626; }

.hot-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement p {
  font-size: 13px;
  color: #64748B;
  line-height: 1.6;
}

/* Post Detail */
.post-detail { padding: 8px 0; }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.detail-time { font-size: 12px; color: #94A3B8; }
.detail-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; }
.detail-content { color: #334155; line-height: 1.8; white-space: pre-wrap; font-size: 15px; }
.detail-actions { margin-top: 16px; display: flex; gap: 12px; }

.comments-title { font-weight: 700; font-size: 15px; margin-bottom: 14px; }
.comment-input-row { display: flex; flex-direction: column; gap: 8px; align-items: flex-end; margin-bottom: 20px; }
.comment-item { padding: 12px; background: #F8FAFC; border-radius: 10px; margin-bottom: 10px; }
.c-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.c-user { font-weight: 700; font-size: 13px; color: #334155; }
.c-time { font-size: 12px; color: #94A3B8; }
.c-content { color: #475569; font-size: 14px; line-height: 1.5; }
.no-comments { text-align: center; color: #CBD5E1; padding: 24px; font-style: italic; }

/* User Card Styles */
.user-profile-dialog :deep(.el-dialog__body) {
  padding: 30px 20px;
}

.user-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.card-avatar {
  background: linear-gradient(135deg, #4ADE80, #3B82F6);
  color: white;
  font-size: 24px;
  margin-bottom: 12px;
}

.card-nickname {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 800;
  color: #1E293B;
}

.card-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-item b {
  font-size: 16px;
  color: #1E293B;
}

.stat-item span {
  font-size: 12px;
  color: #94A3B8;
}

.card-actions {
  width: 100%;
}

.card-actions .el-button {
  width: 100%;
}

/* Activity Center (standalone page) */
.activity-list-wrap {
  background: white;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.04);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-list-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-list-title {
  font-size: 16px;
  font-weight: 900;
  color: #0F172A;
}

.activity-list-sub {
  font-size: 13px;
  color: #64748B;
  line-height: 1.6;
}

.activity-card-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-card {
  background: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.activity-card-main {
  flex: 1;
  cursor: pointer;
}

.activity-card-title {
  font-size: 15px;
  font-weight: 900;
  color: #0F172A;
  margin-bottom: 6px;
}

.activity-card-meta {
  font-size: 12px;
  color: #64748B;
  line-height: 1.6;
}

.activity-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.activity-card-actions {
  display: flex;
  align-items: flex-start;
}

.activity-detail-wrap {
  background: white;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.04);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.activity-detail-title {
  font-size: 18px;
  font-weight: 950;
  color: #0F172A;
  text-align: right;
}

.activity-detail-time {
  font-size: 12px;
  color: #64748B;
}

.activity-detail-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.activity-detail-description {
  border: 1px solid #EFF6FF;
  background: #EFF6FF;
  color: #1D4ED8;
  padding: 12px;
  border-radius: 12px;
}

.completed-block-title {
  font-weight: 900;
  color: #0F172A;
  margin-bottom: 10px;
}

.completed-item {
  padding: 14px;
  background: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 12px;
  margin-bottom: 12px;
}

.completed-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
}

.completed-nick {
  font-weight: 900;
  color: #0F172A;
}

.completed-time {
  font-size: 12px;
  color: #94A3B8;
}

.completed-dynamic {
  color: #334155;
  line-height: 1.8;
  white-space: pre-wrap;
}

.completed-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

</style>
