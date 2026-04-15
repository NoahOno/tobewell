<template>
  <div class="community-page" :class="{ 'is-activity': activeTab === 'activityCenter' }">
    <!-- ===== CENTER MAIN ===== -->
    <main class="community-main">

      <!-- == ACTIVITY CENTER == -->
      <template v-if="activeTab === 'activityCenter'">
        <div class="activity-page-header">
          <div class="activity-page-title">活动中心</div>
          <div class="activity-page-sub">发现并参与官方精选活动，完成目标解锁专属积分奖励</div>
        </div>

        <div v-if="activitiesLoading" class="act-loading"><el-skeleton :rows="4" animated /></div>
        <div v-else-if="activities.length === 0" style="padding: 40px 0">
          <el-empty description="当前没有进行中的活动" />
        </div>
        <div v-else class="activity-card-grid">
          <div
            v-for="(act, idx) in activities"
            :key="act.id"
            class="activity-card premium-card"
            :class="{ 'activity-pinned': act.pinned === 1 }"
            @click="openActivityDetail(act.id)"
          >
            <div class="activity-type-badge" :class="`type-${act.activityType}`">
              {{ activityTypeLabel(act.activityType) }}
            </div>
            <div v-if="act.pinned === 1" class="activity-pin-mark">📌</div>
            <div class="activity-card-title">{{ act.title }}</div>
            <div class="activity-card-desc" v-if="act.description">
              {{ (act.description || '').replace(/<[^>]*>/g, '').slice(0, 80) }}
            </div>

            <!-- Stats row -->
            <div class="activity-card-stats">
              <div class="act-stat" style="flex:1">
                <span class="act-stat-lbl" style="font-weight: 600; color: #1E293B; font-size: 13px">{{ act.targetDescription }}</span>
              </div>
              <div class="act-stat" v-if="act.joined && act.completedTasks !== undefined">
                <span class="act-stat-num" style="color: #3B82F6">{{ act.completedTasks }}</span>
                <span class="act-stat-lbl">已完成</span>
              </div>
              <div class="act-stat" v-if="act.rewardPoints > 0"><span class="act-stat-num" style="color: #F59E0B">+{{ act.rewardPoints }}</span><span class="act-stat-lbl">积分</span></div>
            </div>
            
            <!-- Progress bar for joined activities -->
            <div v-if="showJoinedActivityProgress(act)" class="activity-card-progress">
              <div class="act-progress-label">
                <span>完成进度</span>
                <span style="font-weight: 700; color: #3B82F6">{{ activityProgressDisplay(act).done }}/{{ activityProgressDisplay(act).total }}</span>
              </div>
              <el-progress 
                :percentage="activityProgressDisplay(act).pct" 
                :color="activityProgressDisplay(act).done >= activityProgressDisplay(act).total ? '#10B981' : '#3B82F6'" 
                :stroke-width="6"
                style="margin-top: 4px"
              />
            </div>

            <div class="activity-card-footer">
              <div class="activity-card-meta">
                <span>{{ formatDateRange(act.startTime, act.endTime) }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px">
                <el-tag :type="getActivityDisplayStatusType(act)" size="small">{{ getActivityDisplayStatus(act) }}</el-tag>
                <el-button
                  size="small"
                  :type="act.joined ? 'info' : 'primary'"
                  :plain="act.joined"
                  :disabled="act.joined"
                  :loading="applyActivityLoadingId === act.id"
                  @click.stop="applyActivityFromCard(act)"
                >{{ act.joined ? '已参与' : '参与' }}</el-button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- == POST FEED == -->
      <template v-else>
        <!-- ===== INLINE POST DETAIL VIEW (Reddit style) ===== -->
        <template v-if="viewingPost">
          <div class="post-detail-inline">
            <!-- Back bar -->
            <div class="detail-back-bar">
              <el-button class="detail-back-btn" :icon="ArrowLeft" circle @click="closePost" />
            </div>

            <!-- Post body -->
            <div class="detail-post-body">
              <div class="post-header">
                <el-avatar :size="40" class="post-avatar" :src="viewingPost.avatar || defaultAvatar" @click="showUserCard(viewingPost.userId)">{{ (viewingPost.nickname || '#')[0] }}</el-avatar>
                <div class="post-author-info">
                  <span class="author-name" @click="showUserCard(viewingPost.userId)">{{ viewingPost.nickname || ('用户 #' + viewingPost.userId) }}</span>
                  <span class="post-time">{{ formatTime(viewingPost.createTime) }}</span>
                </div>
                <div class="post-header-actions">
                  <el-button v-if="viewingPost.userId === currentUserId" size="small" link type="danger" @click="deletePost(viewingPost.id)">删除</el-button>
                </div>
              </div>
              <div class="detail-tags" v-if="viewingPost.tags">
                <el-tag v-for="tag in (viewingPost.tags||'').split(',')" :key="tag" size="small" type="info" effect="plain"># {{ tag.trim() }}</el-tag>
              </div>
              <p class="detail-content">{{ viewingPost.content }}</p>
              <!-- Images -->
              <div v-if="viewingPost.images" class="detail-images">
                <el-image
                  v-for="(img, i) in viewingPost.images.split(',')"
                  :key="i"
                  :src="img.trim()"
                  fit="cover"
                  class="detail-img"
                  :preview-src-list="viewingPost.images.split(',')"
                  :initial-index="i"
                />
              </div>
              <div class="detail-actions">
                <el-button :type="viewingPost.isLiked ? 'danger' : 'default'" round @click="toggleLike(viewingPost)">
                  <svg viewBox="0 0 1024 1024" width="16" height="16" :fill="viewingPost.isLiked ? '#fff' : '#606266'" style="margin-right:6px;vertical-align:middle"><path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3 0.1-35.3-7-69.6-20.9-101.9z"/></svg>
                  {{ viewingPost.isLiked ? '已赞' : '点赞' }}<span v-if="viewingPost.likeCount" style="margin-left:4px">({{ viewingPost.likeCount }})</span>
                </el-button>
                <el-button :type="viewingPost.isCollected ? 'warning' : 'default'" round @click="toggleCollect(viewingPost)">
                  <el-icon><StarFilled v-if="viewingPost.isCollected" /><Star v-else /></el-icon>
                  {{ viewingPost.isCollected ? '已收藏' : '收藏' }}<span v-if="viewingPost.collectionCount" style="margin-left:4px">({{ viewingPost.collectionCount }})</span>
                </el-button>
              </div>
            </div>

            <!-- Comments section -->
            <div class="detail-comments premium-card">
              <div class="comments-title">评论 ({{ postComments.length }})</div>
              <div class="comment-input-row">
                <el-input v-model="commentText" placeholder="写下你的看法..." type="textarea" :rows="2" />
                <el-button type="primary" round :disabled="!commentText" @click="submitComment">发布</el-button>
              </div>
              <div class="comment-tip">输入 `@tbw` 后发布，`tbw 智能助手` 会以回复评论的方式参与问答。</div>
              <div class="comments-list">
                <div v-for="c in postComments" :key="c.id" class="comment-item">
                  <div class="c-header">
                    <div class="comment-user">
                      <el-avatar :size="28" :src="c.avatar || defaultAvatar" class="comment-avatar">{{ (c.nickname || ('用户 #' + c.userId))[0] }}</el-avatar>
                      <span class="c-user" style="cursor:pointer" @click="showUserCard(c.userId)">{{ c.nickname || ('用户 #' + c.userId) }}</span>
                    </div>
                    <span class="c-time">{{ formatTime(c.createTime) }}</span>
                  </div>
                  <div class="c-content">{{ c.content }}</div>
                  <div v-if="c.replies?.length" class="comment-replies">
                    <div v-for="reply in c.replies" :key="reply.id" class="reply-item" :class="{ 'reply-ai': isAiReply(reply) }">
                      <div class="c-header">
                        <div class="reply-user-wrap">
                          <el-avatar :size="24" :src="reply.avatar || defaultAvatar" class="comment-avatar">{{ (reply.nickname || ('用户 #' + reply.userId))[0] }}</el-avatar>
                          <span class="c-user" style="cursor:pointer" @click="showUserCard(reply.userId)">{{ reply.nickname || ('用户 #' + reply.userId) }}</span>
                          <el-tag v-if="isAiReply(reply)" size="small" type="success" effect="plain">AI 回答</el-tag>
                        </div>
                        <span class="c-time">{{ formatTime(reply.createTime) }}</span>
                      </div>
                      <div class="c-content">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>
                <div v-if="postComments.length === 0" class="no-comments">还没有评论，快来发表看法！</div>
              </div>
            </div>
          </div>
        </template>

        <!-- ===== NORMAL FEED ===== -->
        <template v-else>
        <!-- Sticky toolbar -->
        <div class="feed-toolbar">
          <el-input
            v-model="searchQ"
            placeholder="搜索帖子或 #标签..."
            class="feed-search"
            clearable
            @clear="clearSearch"
            @keyup.enter="doSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" round @click="doSearch" style="box-shadow: 0 4px 12px rgba(59,130,246,0.3)">搜索</el-button>
          <el-button type="primary" :icon="EditPen" round @click="openPostDialog" style="box-shadow: 0 4px 12px rgba(59,130,246,0.3)">发帖</el-button>
        </div>

        <!-- Search result indicator -->
        <div v-if="isSearchMode" class="search-result-bar">
          <span>搜索「{{ lastSearchQ }}」· {{ posts.length }} 条</span>
          <el-button link @click="clearSearch">清除</el-button>
        </div>

        <div v-if="loading && posts.length === 0" class="feed-skeleton">
          <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom:16px" />
        </div>
        <div v-else-if="posts.length === 0 && !loading" class="empty-feed">
          <el-empty :description="isSearchMode ? '没有找到相关帖子' : '暂无帖子，快来发第一帖！'" />
        </div>

        <div
          v-for="(post, idx) in posts"
          :key="post.id"
          v-reveal
          class="post-card premium-card"
          :class="`delay-${idx % 5}`"
          @click="openPost(post)"
        >
          <div class="post-header">
            <el-avatar :size="38" class="post-avatar" :src="post.avatar || defaultAvatar" @click.stop="showUserCard(post.userId)">{{ (post.nickname || '#')[0] }}</el-avatar>
            <div class="post-author-info">
              <span class="author-name" @click.stop="showUserCard(post.userId)">{{ post.nickname || ('用户 #' + post.userId) }}</span>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
            <div class="post-header-actions">
              <el-button v-if="post.userId === currentUserId" size="small" link type="danger" @click.stop="deletePost(post.id)">删除</el-button>
            </div>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <!-- Thumbnail images preview -->
          <div v-if="post.images" class="post-thumbnail-row">
            <img v-for="(img, i) in post.images.split(',').slice(0,3)" :key="i" :src="img.trim()" class="post-thumb" />
          </div>
          <p class="post-excerpt">{{ (post.content || '').slice(0, 120) }}{{ (post.content?.length || 0) > 120 ? '...' : '' }}</p>
          <div class="post-tags-row" v-if="post.tags">
            <el-tag v-for="tag in (post.tags || '').split(',')" :key="tag" size="small" type="info" effect="plain" class="post-tag"># {{ tag.trim() }}</el-tag>
          </div>
          <div class="post-footer">
            <el-button link class="post-stat-btn like-btn" :class="{ 'is-active': post.isLiked }" @click.stop="toggleLike(post)">
              <svg viewBox="0 0 1024 1024" width="18" height="18" :fill="post.isLiked ? '#F56C6C' : '#94A3B8'"><path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3 0.1-35.3-7-69.6-20.9-101.9z"/></svg>
              <span class="stat-count">{{ post.likeCount || 0 }}</span>
            </el-button>
            <el-button link class="post-stat-btn collect-btn" :class="{ 'is-active': post.isCollected }" @click.stop="toggleCollect(post)">
              <el-icon><StarFilled v-if="post.isCollected" /><Star v-else /></el-icon>
              <span class="stat-count">{{ post.collectionCount || 0 }}</span>
            </el-button>
            <span class="post-stat"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
          </div>
        </div>
        </template>
      </template>
    </main>

    <!-- ===== RIGHT SIDEBAR (sticky, own scroll, hidden in activity center) ===== -->
    <aside v-if="activeTab !== 'activityCenter'" class="community-right">
      <div class="right-card premium-card">
        <div class="right-title">热门话题</div>
        <div v-for="(hot, i) in hotPosts" :key="hot.id" class="hot-item" @click="openPost(hot)">
          <span class="hot-rank" :class="`rank-${i+1}`">{{ i + 1 }}</span>
          <span class="hot-text">{{ hot.title }}</span>
        </div>
        <el-empty v-if="hotPosts.length === 0" description="暂无热门" :image-size="60" />
      </div>
      <div class="right-card premium-card">
        <div class="right-title">热门活动</div>
        <div v-for="(act, i) in trendingActivities" :key="act.id" class="hot-item" @click="openActivityDetail(act.id)">
          <span class="hot-rank" :class="`rank-${i+1}`">{{ i + 1 }}</span>
          <span class="hot-text">{{ act.title }}</span>
        </div>
        <el-empty v-if="trendingActivities.length === 0" description="暂无热门活动" :image-size="60" />
      </div>
      <div class="right-card premium-card">
        <div class="right-title">社区公告</div>
        <p style="font-size:13px; color:#64748B; line-height:1.7; margin:0">欢迎来到健康管理社区！发帖时使用 #话题 可以自动添加标签，请遵守社区规定，保持友善交流。</p>
      </div>
    </aside>

    <!-- ===== ACTIVITY DETAIL DIALOG ===== -->
    <el-dialog v-model="activityDialogVisible" :title="activityDetail?.title || '活动详情'" width="680px" align-center append-to-body destroy-on-close>
      <div v-loading="activityDetailLoading" style="min-height: 180px">
        <div v-if="activityDetail">
          <div class="act-detail-type-row">
            <el-tag :type="activityTypeTagType(activityDetail.activityType)" effect="plain">{{ activityTypeLabel(activityDetail.activityType) }}</el-tag>
            <el-tag :type="getActivityDisplayStatusType(activityDetail)" effect="plain">{{ getActivityDisplayStatus(activityDetail) }}</el-tag>
            <el-tag v-if="activityDetail.pinned === 1" type="warning" effect="plain">📌 置顶</el-tag>
          </div>
          <div class="act-detail-stats">
            <div class="act-stat-item"><span class="act-stat-label">活动时间</span><span>{{ formatDateRange(activityDetail.startTime, activityDetail.endTime) }}</span></div>
            <div class="act-stat-item">
              <span class="act-stat-label">目标</span>
              <span style="font-weight: 600; color: #1E293B;">{{ activityDetail.targetDescription }}</span>
            </div>
            <div class="act-stat-item" v-if="activityDetail.rewardPoints > 0"><span class="act-stat-label">完成奖励</span><span style="color:#F59E0B;font-weight:700">+{{ activityDetail.rewardPoints }} 积分</span></div>
            <div class="act-stat-item" v-if="activityDetail.topicName"><span class="act-stat-label">关联话题</span><span style="color:#8B5CF6">{{ activityDetail.topicName }}</span></div>
            <div class="act-stat-item" v-if="activityDetail.activityType === 1"><span class="act-stat-label">统计方式</span><span style="color:#3B82F6">{{ activityDetail.countMode === 'COUNT' ? '按次数统计' : '按天数统计' }}</span></div>
            <div class="act-stat-item" v-if="activityDetail.activityType === 2"><span class="act-stat-label">统计方式</span><span style="color:#3B82F6">{{ activityDetail.countMode === 'COUNT' ? '按次数统计' : '按天数统计' }}</span></div>
            <div class="act-stat-item" v-if="activityDetail.activityType === 3"><span class="act-stat-label">统计方式</span><span style="color:#3B82F6">{{ getTopicStatModeLabel(activityDetail.topicStatMode) }}</span></div>
          </div>
          
          <!-- 关联训练内容展示 -->
          <div v-if="activityDetail.templateInfo" class="template-info-section">
            <el-divider>📋 关联训练内容</el-divider>
            <div class="template-info-card">
              <div class="template-info-header">
                <div class="template-type-badge">{{ activityDetail.templateType === 'PLAN' ? '训练计划' : '单次课程' }}</div>
                <div class="template-title">{{ activityDetail.templateInfo.title }}</div>
              </div>
              <p v-if="activityDetail.templateInfo.description" class="template-desc">{{ activityDetail.templateInfo.description }}</p>
              <div class="template-meta">
                <span v-if="activityDetail.templateInfo.category">🏷️ {{ activityDetail.templateInfo.category }}</span>
                <span v-if="activityDetail.templateInfo.duration">⏱️ {{ activityDetail.templateInfo.duration }}</span>
                <span v-if="activityDetail.templateInfo.durationMinutes">⏱️ {{ activityDetail.templateInfo.durationMinutes }}分钟</span>
                <span v-if="activityDetail.templateInfo.difficulty">📊 {{ activityDetail.templateInfo.difficulty }}</span>
              </div>
            </div>
          </div>
          
          <div class="act-detail-desc" v-if="activityDetail.description">
            {{ activityDetail.description.replace(/<[^>]*>/g, '') }}
          </div>
          
          <!-- 进度展示 -->
          <div v-if="showJoinedActivityProgress(activityDetail)" class="act-detail-progress">
            <el-divider>📊 我的进度</el-divider>
            <div class="progress-section">
              <div class="progress-label">
                <span>已完成</span>
                <span style="font-weight: 700; color: #3B82F6">{{ activityProgressDisplay(activityDetail).done }}/{{ activityProgressDisplay(activityDetail).total }}</span>
              </div>
              <el-progress 
                :percentage="activityProgressDisplay(activityDetail).pct" 
                :color="activityProgressDisplay(activityDetail).done >= activityProgressDisplay(activityDetail).total ? '#10B981' : '#3B82F6'" 
                :stroke-width="8"
              />
            </div>
          </div>
          
          <div class="act-detail-actions">
            <template v-if="!activityDetail.joined">
              <!-- 挑战类活动选择 -->
              <template v-if="activityDetail.activityType === 2">
                <el-button type="success" size="large" round :loading="applyActivityLoadingId === activityDetail.id" @click="showApplyOptions = true">🚀 立即参与</el-button>
              </template>
              <!-- 其他类型活动直接参与 -->
              <template v-else>
                <el-button type="success" size="large" round :loading="applyActivityLoadingId === activityDetail.id" @click="applyActivity(activityDetail.id)">🚀 立即参与</el-button>
              </template>
            </template>
            <template v-else>
              <el-tag v-if="activityDetail.participationStatus === 'COMPLETED'" type="success" size="large">🎉 已完成</el-tag>
              <template v-else>
                <el-tag type="primary" size="large">✅ 进行中</el-tag>
                <!-- 打卡按钮 -->
                <el-button v-if="activityDetail.activityType === 1" type="warning" size="large" round :loading="checkinLoading" @click="handleCheckIn">📝 今日打卡</el-button>
                <!-- 发帖按钮 -->
                <el-button v-if="activityDetail.activityType === 3" type="primary" size="large" round @click="openPostDialogWithTopic">一键发帖</el-button>
              </template>
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="activityDialogVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 参与选项对话框 -->
    <el-dialog v-model="showApplyOptions" title="选择参与方式" width="500px" align-center append-to-body destroy-on-close>
      <div class="apply-options">
        <div class="apply-option-card" :class="{ active: selectedScheduleType === 'AUTO' }" @click="selectedScheduleType = 'AUTO'">
          <div class="option-icon">📅</div>
          <div class="option-title">自动安排训练</div>
          <div class="option-desc">系统将从今天开始自动为您生成{{ activityDetail?.requiredDays || 7 }}天的训练计划</div>
        </div>
        <div class="apply-option-card" :class="{ active: selectedScheduleType === 'FREE' }" @click="selectedScheduleType = 'FREE'">
          <div class="option-icon">🆓</div>
          <div class="option-title">自由安排训练</div>
          <div class="option-desc">您可以自由选择何时开始和完成训练任务</div>
        </div>
      </div>
      <div v-if="selectedScheduleType === 'AUTO'" style="margin-top: 16px; padding: 0 4px">
        <div style="font-size: 14px; font-weight: 600; margin-bottom: 8px">微调每周训练日</div>
        <el-checkbox-group v-model="subscribeForm.weeklyDays">
          <el-checkbox label="MONDAY">周一</el-checkbox>
          <el-checkbox label="TUESDAY">周二</el-checkbox>
          <el-checkbox label="WEDNESDAY">周三</el-checkbox>
          <el-checkbox label="THURSDAY">周四</el-checkbox>
          <el-checkbox label="FRIDAY">周五</el-checkbox>
          <el-checkbox label="SATURDAY">周六</el-checkbox>
          <el-checkbox label="SUNDAY">周日</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="showApplyOptions = false" round>取消</el-button>
        <el-button type="primary" round :loading="applyActivityLoadingId === activityDetail?.id" @click="applyActivityWithSchedule">确认参与</el-button>
      </template>
    </el-dialog>

    <!-- (post detail now inline, no dialog needed) -->


    <!-- ===== USER CARD DIALOG ===== -->
    <el-dialog v-model="userCardVisible" width="320px" :show-header="false" align-center append-to-body class="user-profile-dialog">
      <div v-if="cardUser" class="user-card-content">
        <el-avatar :size="64" class="card-avatar" :src="cardUser.avatar || defaultAvatar">{{ (cardUser.nickname || '#')[0] }}</el-avatar>
        <h3 class="card-nickname">{{ cardUser.nickname || ('用户 #' + cardUser.id) }}</h3>
        <div class="card-stats">
          <div class="stat-item"><b>{{ cardUser.followingCount || 0 }}</b><span>关注</span></div>
          <div class="stat-item"><b>{{ cardUser.followerCount || 0 }}</b><span>粉丝</span></div>
        </div>
        <div class="card-actions" v-if="cardUser.id !== currentUserId">
          <el-button :type="cardUser.isFollowing ? 'default' : 'primary'" round block @click="toggleFollow(cardUser)" :loading="followLoading">
            {{ cardUser.isFollowing ? '已关注' : '关注' }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- ===== CREATE POST DIALOG ===== -->
    <el-dialog v-model="createDialogVisible" title="发表新帖子" width="600px" align-center append-to-body destroy-on-close>
      <el-form :model="postForm" label-position="top">
        <el-form-item label="标题"><el-input v-model="postForm.title" placeholder="给帖子一个吸引人的标题" /></el-form-item>
        <el-form-item label="内容">
          <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="分享你的健康心得... 使用 #关键词 自动生成标签" />
        </el-form-item>
        <el-form-item label="图片（可选，最多9张）">
          <div class="upload-image-grid">
            <div
              v-for="(img, i) in postForm.images"
              :key="i"
              class="upload-preview-item"
            >
              <img :src="img" class="upload-preview-img" />
              <div class="upload-preview-remove" @click="postForm.images.splice(i, 1)">✕</div>
            </div>
            <label v-if="postForm.images.length < 9" class="upload-trigger" for="post-img-upload">
              <el-icon style="font-size:24px;color:#94A3B8"><Plus /></el-icon>
              <span style="font-size:12px;color:#94A3B8;margin-top:4px">添加图片</span>
              <input
                id="post-img-upload"
                type="file"
                accept="image/*"
                multiple
                style="display:none"
                @change="handleImageUpload"
              />
            </label>
          </div>
          <div v-if="uploadingImages" style="color:#3B82F6;font-size:13px;margin-top:8px">图片上传中...</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="postSubmitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { Search, Star, User, Trophy, EditPen, ChatDotRound, StarFilled, Management, Plus, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import request from '../api/request'

const route = useRoute()
const loading = ref(false)
const activitiesLoading = ref(false)
const posts = ref<any[]>([])
const hotPosts = ref<any[]>([])
const postComments = ref<any[]>([])
const searchQ = ref('')
const lastSearchQ = ref('')
const isSearchMode = ref(false)

const activeTab = computed(() => (route.query.tab as string) || 'recommend')

const postDialogVisible = ref(false)
const createDialogVisible = ref(false)
const userCardVisible = ref(false)
const followLoading = ref(false)
const selectedPost = ref<any>(null)
const viewingPost = ref<any>(null)
const cardUser = ref<any>(null)
const commentText = ref('')
const commentSubmitting = ref(false)
const postSubmitting = ref(false)
const uploadingImages = ref(false)

// Activities
const activities = ref<any[]>([])
const trendingActivities = ref<any[]>([])
const activityCompletedItems = ref<any[]>([])
const activityCompletedMeta = ref<any>({ totalParticipants: 0 })
const applyActivityLoadingId = ref<number | null>(null)
const activityDialogVisible = ref(false)
const activityDetailLoading = ref(false)
const activityDetail = ref<any>(null)
const showApplyOptions = ref(false)
const selectedScheduleType = ref('AUTO')
const checkinLoading = ref(false)

const currentUserId = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const subscribeForm = reactive({
  weeklyDays: ['MONDAY', 'WEDNESDAY', 'FRIDAY']
})

const postForm = reactive({ title: '', content: '', category: '综合', images: [] as string[] })

// ---- Activity helpers ----
const activityTypeLabel = (type: number) => {
  if (type === 1) return '打卡类'
  if (type === 2) return '挑战类'
  if (type === 3) return '话题类'
  return '活动'
}
const activityTypeTagType = (type: number) => {
  if (type === 1) return 'success'
  if (type === 2) return 'danger'
  if (type === 3) return 'warning'
  return 'info'
}
const getStatLabel = (act: any) => {
  if (act.activityType === 1) return act.countMode === 'COUNT' ? '次目标' : '天目标'
  if (act.activityType === 2) {
    if (act.countMode === 'COUNT') return '次目标'
    return '天目标'
  }
  if (act.activityType === 3) {
    if (act.topicStatMode === 'SHARED') return '分享目标'
    if (act.topicStatMode === 'DAYS') return '天目标'
    return '次目标'
  }
  return '天目标'
}
const getStatUnit = (act: any) => {
   if (act.activityType === 1) return act.countMode === 'COUNT' ? '次' : '天'
   if (act.activityType === 2) {
     if (act.countMode === 'COUNT') return '次'
     return '天'
   }
   if (act.activityType === 3) {
     if (act.topicStatMode === 'SHARED') return ''
     if (act.topicStatMode === 'DAYS') return '天'
     return '次'
   }
   return '天'
 }
 const getTopicStatModeLabel = (mode: string) => {
   if (mode === 'SHARED') return '是否分享'
   if (mode === 'DAYS') return '按分享天数'
   return '按分享次数'
 }
const getActivityDisplayStatus = (row: any) => {
  if (row.status === 'DRAFT') return '草稿'
  if (row.status === 'OFFLINE') return '已下线'
  const now = Date.now()
  const st = row.startTime ? new Date(row.startTime).getTime() : 0
  const et = row.endTime ? new Date(row.endTime).getTime() : Infinity
  if (st && now < st) return '未开始'
  if (et !== Infinity && now > et) return '已结束'
  return '进行中'
}
const getActivityDisplayStatusType = (row: any) => {
  const s = getActivityDisplayStatus(row)
  return s === '草稿' ? 'info' : s === '未开始' ? 'warning' : s === '进行中' ? 'success' : s === '已结束' ? 'info' : 'danger'
}

/** 活动中心「一键发帖」发布后用于强制刷新对应活动详情 */
const activityIdAfterPost = ref<number | null>(null)

const activityProgressDisplay = (d: any) => {
  if (!d?.joined) return { done: 0, total: 1, pct: 0 }
  const done = Number(d.completedTasks) || 0
  let total = Number(d.totalTasks)
  if (!Number.isFinite(total) || total <= 0) {
    if (d.activityType === 3 && d.topicStatMode === 'SHARED') total = 1
    else total = Math.max(1, Number(d.requiredDays) || 1)
  }
  const pct = total > 0 ? Math.min(100, Math.round((done / total) * 100)) : 0
  return { done, total, pct }
}

const showJoinedActivityProgress = (d: any) => {
  if (!d?.joined) return false
  const t = Number(d.totalTasks)
  if (Number.isFinite(t) && t > 0) return true
  if (d.activityType === 3) return true
  return Number(d.requiredDays) > 0
}

// ---- Data fetching ----
const fetchUserInfo = async () => {
  try { const res: any = await request.get('/auth/info'); currentUserId.value = res.data.id } catch (e) {}
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/community/posts', { params: { tab: activeTab.value } })
    posts.value = res.data
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const fetchHotPosts = async () => {
  try { const res: any = await request.get('/community/hot'); hotPosts.value = res.data } catch (e) {}
}

const fetchActivities = async () => {
  activitiesLoading.value = true
  try { const res: any = await request.get('/activity/list'); activities.value = res.data || [] } catch (e) {} finally { activitiesLoading.value = false }
}

const fetchTrendingActivities = async () => {
  try { const res: any = await request.get('/activity/trending'); trendingActivities.value = res.data || [] } catch (e) {}
}

watch(activeTab, (val) => {
  isSearchMode.value = false
  if (val === 'activityCenter') {
    fetchActivities()
    fetchTrendingActivities()
  } else {
    fetchPosts()
  }
}, { immediate: true })

const fetchActivityCompleted = async (activityId: number) => {
  try {
    const res: any = await request.get(`/activity/${activityId}/completed`)
    activityCompletedItems.value = res?.data?.items || []
    activityCompletedMeta.value = { totalParticipants: res?.data?.totalParticipants || 0 }
  } catch (e) {
    activityCompletedItems.value = []
    activityCompletedMeta.value = { totalParticipants: 0 }
  }
}

const openActivityDetail = async (activityId: number) => {
  activityDialogVisible.value = true
  activityDetailLoading.value = true
  try {
    const res: any = await request.get(`/activity/${activityId}`)
    activityDetail.value = res.data
    await fetchActivityCompleted(activityId)
  } catch (e) {
    activityDetail.value = null
    activityCompletedItems.value = []
  } finally { activityDetailLoading.value = false }
}

const applyActivity = async (activityId: number) => {
  applyActivityLoadingId.value = activityId
  try {
    await request.post(`/activity/${activityId}/apply`)
    ElMessage.success('参与成功！')
    await fetchActivities()
    if (activityDetail.value?.id === activityId) {
      const res: any = await request.get(`/activity/${activityId}`)
      activityDetail.value = res.data
    }
  } catch (e) {} finally { applyActivityLoadingId.value = null }
}

/** 列表卡片上参与：挑战类显式按「自动安排」报名，避免空 JSON 体被解析为 scheduleType=null */
const applyActivityFromCard = async (act: any) => {
  applyActivityLoadingId.value = act.id
  try {
    const body = act.activityType === 2 ? { scheduleType: 'AUTO' } : undefined
    await request.post(`/activity/${act.id}/apply`, body)
    ElMessage.success('参与成功！')
    await fetchActivities()
    if (activityDetail.value?.id === act.id) {
      const res: any = await request.get(`/activity/${act.id}`)
      activityDetail.value = res.data
    }
  } catch (e) {} finally { applyActivityLoadingId.value = null }
}

const applyActivityWithSchedule = async () => {
  if (!activityDetail.value?.id) return
  applyActivityLoadingId.value = activityDetail.value.id
  try {
    await request.post(`/activity/${activityDetail.value.id}/apply`, {
      scheduleType: selectedScheduleType.value,
      weeklyDays: subscribeForm.weeklyDays
    })
    ElMessage.success('参与成功！')
    showApplyOptions.value = false
    await fetchActivities()
    const res: any = await request.get(`/activity/${activityDetail.value.id}`)
    activityDetail.value = res.data
  } catch (e) {} finally { applyActivityLoadingId.value = null }
}

const handleCheckIn = async () => {
  if (!activityDetail.value?.id) return
  checkinLoading.value = true
  try {
    await request.post(`/activity/${activityDetail.value.id}/checkin`)
    ElMessage.success('打卡成功！')
    const res: any = await request.get(`/activity/${activityDetail.value.id}`)
    activityDetail.value = res.data
    await fetchActivities()
  } catch (e) {} finally { checkinLoading.value = false }
}

const openPostDialogWithTopic = () => {
  activityIdAfterPost.value = activityDetail.value?.id ?? null
  if (activityDetail.value?.topicName) {
    const topic = activityDetail.value.topicName.replace(/^#+/, '').trim()
    postForm.title = `参与${activityDetail.value.title}活动`
    postForm.content = `#${topic}\n\n分享我的活动参与心得...`
  }
  createDialogVisible.value = true
}

const formatDateRange = (start: any, end: any) => {
  if (!start) return ''
  const fmt = (d: Date) => `${d.getMonth() + 1}/${d.getDate()}`
  const s = new Date(start)
  if (!end) return fmt(s)
  const e = new Date(end)
  return `${fmt(s)} ~ ${fmt(e)}`
}

const doSearch = async () => {
  if (!searchQ.value.trim()) return
  loading.value = true; isSearchMode.value = true; lastSearchQ.value = searchQ.value
  try { const res: any = await request.get('/community/search', { params: { q: searchQ.value } }); posts.value = res.data } catch (e) {} finally { loading.value = false }
}

const clearSearch = () => { isSearchMode.value = false; searchQ.value = ''; fetchPosts() }

const commentCountDisplay = computed(() =>
  postComments.value.reduce((count, item) => count + 1 + (item.replies?.length || 0), 0)
)

const isAiReply = (comment: any) => comment?.nickname === 'tbw 智能助手'

const openPost = async (post: any) => {
  viewingPost.value = post
  commentText.value = ''
  try { const res: any = await request.get(`/community/post/${post.id}/comments`); postComments.value = res.data } catch (e) {}
}

const closePost = () => {
  viewingPost.value = null
  postComments.value = []
}

const toggleLike = async (post: any) => {
  const was = post.isLiked, cnt = post.likeCount || 0
  post.isLiked = !was; post.likeCount = was ? Math.max(0, cnt - 1) : cnt + 1
  try {
    if (was) await request.delete(`/community/post/${post.id}/like`)
    else { await request.post(`/community/post/${post.id}/like`); ElMessage.success('已点赞') }
  } catch { post.isLiked = was; post.likeCount = cnt; ElMessage.error('操作失败') }
}

const toggleCollect = async (post: any) => {
  const was = post.isCollected, cnt = post.collectionCount || 0
  post.isCollected = !was; post.collectionCount = was ? Math.max(0, cnt - 1) : cnt + 1
  try {
    if (was) { await request.delete('/interaction/collect', { params: { targetId: post.id, targetType: 'POST' } }); ElMessage.success('已取消收藏') }
    else { await request.post('/interaction/collect', { targetId: post.id, targetType: 'POST', targetTitle: post.title }); ElMessage.success('已加入收藏') }
  } catch { post.isCollected = was; post.collectionCount = cnt; ElMessage.error('操作失败') }
}

const showUserCard = async (userId: number) => {
  try { const res: any = await request.get(`/community/user/${userId}`); cardUser.value = res.data; userCardVisible.value = true } catch { ElMessage.error('获取用户信息失败') }
}

const toggleFollow = async (user: any) => {
  followLoading.value = true
  const was = user.isFollowing
  try {
    if (was) { await request.delete(`/community/follow/${user.id}`); user.isFollowing = false; user.followerCount--; ElMessage.success('已取消关注') }
    else { await request.post(`/community/follow/${user.id}`); user.isFollowing = true; user.followerCount++; ElMessage.success('关注成功') }
    if (activeTab.value === 'following') fetchPosts()
  } catch {} finally { followLoading.value = false }
}

const deletePost = async (postId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', { type: 'warning' })
    await request.delete(`/community/post/${postId}`)
    ElMessage.success('删除成功')
    if (viewingPost.value?.id === postId) closePost()
    fetchPosts()
  } catch {}
}

const submitComment = async () => {
  if (!commentText.value || !viewingPost.value) return
  const post = viewingPost.value
  const content = commentText.value
  const origCount = post.commentCount || 0
  const origComments = [...postComments.value]
  postComments.value = [{ id: Date.now(), userId: currentUserId.value, nickname: '我', content, createTime: new Date().toISOString() }, ...origComments]
  post.commentCount = origCount + 1
  commentText.value = ''
  commentSubmitting.value = true
  try {
    await request.post(`/community/post/${post.id}/comment`, { content })
    const res: any = await request.get(`/community/post/${post.id}/comments`)
    postComments.value = res.data
    post.commentCount = commentCountDisplay.value
    ElMessage.success('评论发布成功')
  } catch { postComments.value = origComments; post.commentCount = origCount; commentText.value = content; ElMessage.error('回复失败') }
  commentSubmitting.value = false
}

const openPostDialog = (prefill?: { title?: string; content?: string }) => {
  activityIdAfterPost.value = null
  Object.assign(postForm, { title: prefill?.title || '', content: prefill?.content || '', images: [] })
  createDialogVisible.value = true
}

const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  const files = Array.from(input.files)
  const remaining = 9 - postForm.images.length
  const toUpload = files.slice(0, remaining)
  uploadingImages.value = true
  try {
    for (const file of toUpload) {
      const fd = new FormData()
      fd.append('file', file)
      const res: any = await request.post('/file/upload', fd)
      if (res.url) postForm.images.push(res.url)
    }
  } catch (e) { ElMessage.error('图片上传失败') } finally {
    uploadingImages.value = false
    input.value = ''
  }
}

const submitPost = async () => {
  if (!postForm.title || !postForm.content) return ElMessage.warning('请填写标题和内容')
  const refreshAid = activityIdAfterPost.value
  postSubmitting.value = true
  try {
    await request.post('/community/post', {
      title: postForm.title,
      content: postForm.content,
      category: postForm.category,
      images: postForm.images.join(',')
    })
    createDialogVisible.value = false
    ElMessage.success('发布成功')
    Object.assign(postForm, { title: '', content: '', category: '综合', images: [] })
    await fetchPosts()
    await fetchActivities()
    if (refreshAid != null) {
      const res: any = await request.get(`/activity/${refreshAid}`)
      if (activityDetail.value?.id === refreshAid) {
        activityDetail.value = { ...res.data }
      }
      await nextTick()
    }
  } catch {}
  finally {
    activityIdAfterPost.value = null
    postSubmitting.value = false
  }
}

const formatTime = (t: string) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const onOpenActivityDetailEvent = (e: Event) => {
  const ce = e as CustomEvent<{ activityId?: number }>
  if (ce.detail?.activityId != null) openActivityDetail(ce.detail.activityId)
}

onMounted(() => {
  fetchUserInfo()
  fetchPosts()
  fetchHotPosts()
  fetchActivities()
  fetchTrendingActivities()
  window.addEventListener('openActivityDetail', onOpenActivityDetailEvent)
})
onUnmounted(() => {
  window.removeEventListener('openActivityDetail', onOpenActivityDetailEvent)
})
</script>

<style scoped>
/* ===========================
   PAGE LAYOUT - 2-column grid
   =========================== */
.community-page {
  display: grid;
  grid-template-columns: 1fr 280px; /* main feed + right sidebar */
  gap: 24px;
  align-items: start;
  height: 100%;
}
.community-page.is-activity {
  grid-template-columns: 1fr; /* Activity Center hides right sidebar */
}

/* Center main */
.community-main {
  padding: 0 0 40px 0;
  height: 100%;
  overflow-y: auto;
  scrollbar-width: thin;
}

/* Sticky feed toolbar */
.feed-toolbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px 4px;
  margin-bottom: 12px;
}
.feed-search { flex: 1; }
.feed-search :deep(.el-input__wrapper) { border-radius: 18px; box-shadow: none; border: 1px solid #E2E8F0; background: #fff; }

/* Right sidebar - sticky with own scroll */
.community-right {
  height: 100%;
  overflow-y: auto;
  padding-right: 4px; /* for scrollbar spacing */
  padding-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scrollbar-width: none;
}
.community-right::-webkit-scrollbar { display: none; }

/* ===========================
   ACTIVITY CENTER
   =========================== */
.activity-page-header {
  padding: 24px 20px 14px;
}
.activity-page-title { font-size: 22px; font-weight: 800; color: #0F172A; }
.activity-page-sub { font-size: 13px; color: #94A3B8; margin-top: 4px; }
.act-loading { padding: 24px 20px; }

.activity-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  padding: 0 20px 20px;
}

.activity-card {
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
  position: relative;
  overflow: hidden;
}
.activity-card:hover { transform: translateY(-4px); box-shadow: 0 16px 40px rgba(59,130,246,0.12); }
.activity-pinned { border: 2px solid #F59E0B; }
.activity-pin-mark { position: absolute; top: 14px; right: 14px; font-size: 16px; }

.activity-type-badge {
  display: inline-block; font-size: 11px; font-weight: 700;
  padding: 3px 10px; border-radius: 100px; margin-bottom: 10px; letter-spacing: 0.04em;
}
.type-1 { background: #ECFDF5; color: #059669; }
.type-2 { background: #FEF2F2; color: #DC2626; }
.type-3 { background: #FFFBEB; color: #D97706; }

.activity-card-title { font-size: 16px; font-weight: 800; color: #0F172A; margin-bottom: 6px; line-height: 1.4; }
.activity-card-desc { font-size: 13px; color: #94A3B8; line-height: 1.6; margin-bottom: 12px; }

.activity-card-stats {
  display: flex; gap: 20px; padding: 10px 0; border-top: 1px solid #F1F5F9; border-bottom: 1px solid #F1F5F9; margin-bottom: 12px;
}
.act-stat { display: flex; flex-direction: column; align-items: center; gap: 2px; flex: 1; }
.act-stat-num { font-size: 18px; font-weight: 800; color: #1E293B; }
.act-stat-lbl { font-size: 11px; color: #94A3B8; }

.activity-card-progress {
  padding-top: 12px;
  border-top: 1px solid #F1F5F9;
}
.act-progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748B;
}

.activity-card-footer { display: flex; align-items: center; justify-content: space-between; }
.activity-card-meta { font-size: 12px; color: #64748B; }

/* ===========================
   POST FEED
   =========================== */
.search-result-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #EFF6FF; padding: 8px 20px; font-size: 14px; color: #3B82F6;
}
.empty-feed { padding: 40px 20px; }
.feed-skeleton { padding: 20px; }

.post-card {
  margin: 0 4px 16px;
  padding: 24px;
  cursor: pointer;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85); /* Semi-transparent suspension */
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: box-shadow 0.25s, transform 0.25s;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
}
.post-card:hover { box-shadow: 0 12px 36px rgba(59,130,246,0.12); transform: translateY(-3px); }

.post-header { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 10px; }
.post-avatar { flex-shrink: 0; }
.post-author-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.author-name { font-size: 14px; font-weight: 700; color: #1E293B; cursor: pointer; }
.author-name:hover { color: var(--el-color-primary); }
.post-time { font-size: 12px; color: #94A3B8; }
.post-header-actions { margin-left: auto; }

.post-title { font-size: 16px; font-weight: 800; color: #0F172A; margin: 0 0 8px; }
.post-excerpt { font-size: 14px; color: #475569; line-height: 1.7; margin: 0 0 10px; }
.post-tags-row { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.post-tag { font-size: 12px !important; }
.post-footer { display: flex; align-items: center; gap: 16px; padding-top: 12px; border-top: 1px solid #F8FAFC; }

.post-stat-btn { color: #94A3B8 !important; padding: 0 !important; }
.post-stat-btn.is-active { color: #F56C6C !important; }
.stat-count { margin-left: 4px; font-size: 13px; }
.post-stat { display: flex; align-items: center; gap: 4px; font-size: 14px; color: #94A3B8; }

/* ===========================
   RIGHT SIDEBAR
   =========================== */
.right-card { 
  padding: 20px; 
  border-radius: 20px; 
  background: white;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.right-title { font-size: 13px; font-weight: 800; color: #374151; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.hot-item { display: flex; align-items: center; gap: 10px; padding: 7px 0; cursor: pointer; border-bottom: 1px solid #F8FAFC; }
.hot-item:hover .hot-text { color: var(--el-color-primary); }
.hot-rank { font-size: 12px; font-weight: 800; width: 20px; height: 20px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: #F1F5F9; color: #94A3B8; flex-shrink: 0; }
.rank-1 { background: #FEF3C7; color: #D97706; }
.rank-2 { background: #F1F5F9; color: #64748B; }
.rank-3 { background: #FEE2E2; color: #EF4444; }
.hot-text { font-size: 13px; color: #374151; line-height: 1.4; flex: 1; }

/* ===========================
   ACTIVITY DIALOG
   =========================== */
.act-detail-type-row { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.act-detail-stats {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
  background: #F8FAFC; border-radius: 12px; padding: 16px; margin-bottom: 16px;
}
.act-stat-item { display: flex; flex-direction: column; gap: 2px; }
.act-stat-label { font-size: 11px; color: #94A3B8; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.act-detail-desc { background: #EFF6FF; color: #1E40AF; padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.7; margin-bottom: 16px; }
.act-detail-actions { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.act-participant-list { display: flex; flex-direction: column; gap: 10px; }
.act-participant-item { display: flex; align-items: center; gap: 12px; padding: 8px 12px; background: #F8FAFC; border-radius: 10px; }
.act-participant-info { display: flex; align-items: center; gap: 8px; flex: 1; }
.act-participant-name { font-size: 14px; font-weight: 600; color: #1E293B; }

/* Template info section */
.template-info-section {
  margin-bottom: 16px;
}
.template-info-card {
  background: linear-gradient(135deg, #EFF6FF 0%, #F0F9FF 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #DBEAFE;
}
.template-info-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.template-type-badge {
  background: #3B82F6;
  color: white;
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 700;
}
.template-title {
  font-size: 16px;
  font-weight: 800;
  color: #1E40AF;
}
.template-desc {
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 12px;
}
.template-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.template-meta span {
  font-size: 12px;
  color: #64748B;
  background: white;
  padding: 4px 10px;
  border-radius: 6px;
}

/* Progress section */
.act-detail-progress {
  margin-bottom: 16px;
}
.progress-section {
  background: #F8FAFC;
  border-radius: 12px;
  padding: 16px;
}
.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #64748B;
  margin-bottom: 8px;
}

/* Apply options */
.apply-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.apply-option-card {
  border: 2px solid #E2E8F0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.apply-option-card:hover {
  border-color: #3B82F6;
  background: #EFF6FF;
}
.apply-option-card.active {
  border-color: #3B82F6;
  background: #EFF6FF;
}
.option-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
.option-title {
  font-size: 16px;
  font-weight: 800;
  color: #1E293B;
  margin-bottom: 4px;
}
.option-desc {
  font-size: 13px;
  color: #64748B;
  line-height: 1.5;
}

/* ===========================
   INLINE POST DETAIL (Reddit style)
   =========================== */
.post-detail-inline {
  display: flex;
  flex-direction: column;
  gap: 0;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.detail-back-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 4px 0 14px;
  position: sticky;
  top: 0;
  z-index: 10;
  margin-bottom: 4px;
}
.detail-back-btn {
  border-color: #D1D5DB;
}

.detail-post-body {
  padding: 8px 0 18px;
  margin-bottom: 12px;
}
.detail-post-title {
  font-size: 22px;
  font-weight: 800;
  color: #0F172A;
  margin: 12px 0 10px;
  line-height: 1.4;
}
.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0;
}
.detail-img {
  width: 200px;
  height: 150px;
  border-radius: 12px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #E2E8F0;
}
.detail-comments {
  padding: 24px;
  border-radius: 20px;
  margin-bottom: 16px;
}
.detail-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.detail-content { font-size: 15px; line-height: 1.9; color: #334155; white-space: pre-wrap; margin: 0 0 16px; }
.detail-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.comments-title { font-size: 16px; font-weight: 800; color: #0F172A; margin-bottom: 16px; }
.comment-input-row { display: flex; gap: 10px; margin-bottom: 16px; }
.comment-tip { margin-bottom: 12px; color: #64748B; font-size: 13px; line-height: 1.6; }
.comments-list { display: flex; flex-direction: column; gap: 10px; }
.comment-item { padding: 12px 16px; background: #F8FAFC; border-radius: 12px; }
.comment-replies { margin-top: 10px; display: flex; flex-direction: column; gap: 8px; }
.reply-item { margin-left: 14px; padding: 10px 12px; border-radius: 10px; background: #ffffff; border: 1px solid #E2E8F0; }
.reply-item.reply-ai { background: #F0FDF4; border-color: #BBF7D0; }
.reply-user-wrap { display: flex; align-items: center; gap: 8px; }
.comment-user { display: flex; align-items: center; gap: 10px; }
.comment-avatar { flex-shrink: 0; }
.c-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.c-user { font-weight: 700; font-size: 13px; color: #1E293B; }
.c-time { font-size: 12px; color: #94A3B8; }
.c-content { font-size: 14px; color: #475569; line-height: 1.6; }
.no-comments { text-align: center; color: #94A3B8; padding: 24px 0; }

/* Post card thumbnail preview */
.post-thumbnail-row { display: flex; gap: 6px; margin: 8px 0; }
.post-thumb { width: 80px; height: 60px; border-radius: 8px; object-fit: cover; border: 1px solid #E2E8F0; }

/* Image upload grid (Weibo style) */
.upload-image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
}
.upload-preview-item {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #E2E8F0;
}
.upload-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-preview-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(0,0,0,0.5);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
}
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  border: 2px dashed #CBD5E1;
  border-radius: 10px;
  cursor: pointer;
  background: #F8FAFC;
  transition: border-color 0.2s;
}
.upload-trigger:hover { border-color: #3B82F6; background: #EFF6FF; }

/* ===========================
   USER CARD DIALOG
   =========================== */
.user-card-content { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 8px 0; }
.card-avatar { border: 3px solid white; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.card-nickname { font-size: 18px; font-weight: 800; margin: 0; }
.card-stats { display: flex; gap: 32px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.stat-item b { font-size: 20px; font-weight: 800; color: var(--el-color-primary); }
.stat-item span { font-size: 12px; color: #94A3B8; }
.card-actions { width: 100%; }
</style>
