<template>
  <div class="training-dashboard">
    <!-- Main Content Area -->
    <div class="main-body" v-loading="loading">
      <!-- Training Dashboard (Global Overview) -->
      <template v-if="activeMenu === 'overview'">
        <div class="overview-wrap">
          <div class="overview-top-stats">
            <div v-reveal class="stat-card delay-0">
              <div class="stat-label">累计训练时长</div>
              <div class="stat-value">{{ formatMinutes(overviewSummary.totalDurationMinutes) }}</div>
            </div>
            <div v-reveal class="stat-card delay-1">
              <div class="stat-label">连续训练天数</div>
              <div class="stat-value">{{ overviewSummary.currentStreakDays }} 天</div>
            </div>
            <div v-reveal class="stat-card delay-2">
              <div class="stat-label">完成训练次数</div>
              <div class="stat-value">{{ overviewSummary.completedCount }} 次</div>
            </div>
          </div>

          <div v-reveal class="overview-chart-card delay-0">
            <div class="overview-section-title">最近七天运动趋势</div>
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>

          <div class="overview-bottom">
            <div v-reveal class="recent-card delay-1">
              <div class="overview-section-title">最近十条训练数据</div>
              <div class="commit-timeline">
                <div v-if="recentTrainings.length === 0" class="empty-subtle">暂无训练记录</div>
                <div v-for="(item, idx) in recentTrainings" :key="idx" class="commit-item">
                  <div class="commit-left">
                    <div class="commit-dot" />
                    <div v-if="idx !== recentTrainings.length - 1" class="commit-line" />
                  </div>
                  <div class="commit-body">
                    <div class="commit-meta">{{ formatDateShort(item.date) }} • {{ item.durationMinutes }} 分钟</div>
                    <div class="commit-title">{{ item.title }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-reveal class="today-card delay-2">
              <div class="overview-section-title">今日训练安排</div>
              <div v-if="todaySchedules.length === 0" class="empty-subtle">今天暂无安排</div>
              <div v-else class="today-list">
                <div
                  v-for="sched in todaySchedules"
                  :key="sched.id"
                  class="today-item"
                >
                  <div class="today-item-top">
                    <div class="today-title">{{ sched.title }}</div>
                    <div class="today-status">{{ formatScheduleStatus(sched.status) }}</div>
                  </div>
                  <div class="today-desc">{{ sched.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Training Diary (Calendar View) -->
      <template v-else-if="activeMenu === 'calendar'">
        <div class="calendar-layout">
          <div class="calendar-left">
            <div class="calendar-toolbar">
              <div class="calendar-title">{{ calendarTitle }}</div>
              <div class="calendar-actions">
                <el-button-group>
                  <el-button size="small" @click="shiftCalendar(-1)">上一{{ calendarView === 'month' ? '月' : '周' }}</el-button>
                  <el-button size="small" @click="shiftCalendar(1)">下一{{ calendarView === 'month' ? '月' : '周' }}</el-button>
                </el-button-group>
                <el-radio-group v-model="calendarView" size="small">
                  <el-radio-button label="month">月</el-radio-button>
                  <el-radio-button label="week">周</el-radio-button>
                </el-radio-group>
              </div>
            </div>

            <template v-if="calendarView === 'month'">
              <el-calendar v-model="selectedDate" class="premium-calendar">
                <template #date-cell="{ data }">
                  <div class="cell-content">
                    <span class="day-text">{{ data.day.split('-').pop() }}</span>
                    <div class="cell-events" v-if="getSchedules(data.day).length > 0">
                      <div
                        v-for="s in getSchedules(data.day)" 
                        :key="s.id" 
                        class="schedule-event" 
                        :class="getScheduleCellClass(s)"
                        :title="s.title"
                        @click.stop="openScheduleDetail(s)"
                      >
                        {{ s.title }}
                      </div>
                    </div>
                  </div>
                </template>
              </el-calendar>
            </template>
            <template v-else>
              <div class="week-board">
                <div
                  v-for="d in weekDates"
                  :key="d.dateStr"
                  class="week-day"
                  :class="{ 'is-active': d.dateStr === formattedSelectedDate }"
                  @click="selectDate(d.dateStr)"
                >
                  <div class="week-day-head">
                    <div class="week-day-label">{{ d.label }}</div>
                    <div class="week-day-date">{{ d.monthNum }}-{{ d.dayNum }}</div>
                  </div>
                  <div class="week-day-body">
                    <div
                      v-for="s in getSchedules(d.dateStr)"
                      :key="s.id"
                      class="week-task"
                      :class="getScheduleCellClass(s)"
                      @click.stop="openScheduleDetail(s)"
                    >
                      {{ s.title }}
                    </div>
                    <div v-if="getSchedules(d.dateStr).length === 0" class="week-empty">无任务</div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <div class="calendar-right">
            <div class="selected-date-display">{{ formattedSelectedDate }}</div>
            
            <div class="daily-tasks-wrapper">
              <div v-if="selectedDaySchedules.length === 0" class="empty-tasks">
                <el-empty description="这天没有训练安排" :image-size="80" />
                <el-button type="primary" round plain @click="router.push('/app/explore')">去探索新计划</el-button>
              </div>

              <!-- Loop Through Schedules For This Day -->
              <div 
                v-for="sched in selectedDaySchedules" 
                :key="sched.id" 
                class="task-card premium-card clickable-card"
                @click="openScheduleDetail(sched)"
              >
                <div class="tc-header-minimal">
                  <h4 class="tc-title-minimal">{{ sched.title }}</h4>
                  <span class="tc-status" :class="getTaskStatusClass(sched)">{{ mapStatus(sched.status, sched.date) }}</span>
                </div>
              </div>
            </div>

          </div>
        </div>
      </template>

      <!-- Active Training Plans Management -->
      <template v-if="activeMenu === 'plans'">
        <div class="train-header">
          <div class="train-header-left">
            <div class="train-title-row">
              <h2>训练管控</h2>
              <el-radio-group v-model="myTrainingTab" size="small">
                <el-radio-button label="plans">训练计划</el-radio-button>
                <el-radio-button label="courses">单次课程</el-radio-button>
              </el-radio-group>
            </div>
            <p>搜索与筛选你正在推进的训练内容，快速找到今天要练的那一个。</p>
          </div>
          <div class="train-header-right">
            <el-input
              v-model="trainQuery.keyword"
              placeholder="搜索标题或描述..."
              class="search-input"
              clearable
              @keyup.enter="noopSearch"
              @clear="noopSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="noopSearch">搜索</el-button>
          </div>
        </div>

        <div class="filters-card premium-card">
          <template v-if="myTrainingTab === 'plans'">
            <div class="filter-row">
              <span class="filter-label">目标</span>
              <div class="filter-options">
                <el-tag v-for="g in planGoals" :key="g" :effect="trainQuery.planGoal === g ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.planGoal = g">{{ g }}</el-tag>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">难度</span>
              <div class="filter-options">
                <el-tag v-for="d in planDifficulties" :key="d" :effect="trainQuery.planDifficulty === d ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.planDifficulty = d">{{ d }}</el-tag>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">频次</span>
              <div class="filter-options">
                <el-tag v-for="f in planFrequencies" :key="f" :effect="trainQuery.planFrequency === f ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.planFrequency = f">{{ f }}</el-tag>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="filter-row">
              <span class="filter-label">分类</span>
              <div class="filter-options">
                <el-tag v-for="c in courseCategories" :key="c" :effect="trainQuery.courseCategory === c ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.courseCategory = c">{{ c }}</el-tag>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">难度</span>
              <div class="filter-options">
                <el-tag v-for="d in courseDifficulties" :key="d" :effect="trainQuery.courseDifficulty === d ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.courseDifficulty = d">{{ d }}</el-tag>
              </div>
            </div>
            <div class="filter-row">
              <span class="filter-label">时长</span>
              <div class="filter-options">
                <el-tag v-for="t in courseDurations" :key="t" :effect="trainQuery.courseDuration === t ? 'dark' : 'plain'" class="filter-tag" @click="trainQuery.courseDuration = t">{{ t }}</el-tag>
              </div>
            </div>
          </template>
        </div>

        <template v-if="myTrainingTab === 'plans'">
          <div class="card-grid">
            <div v-for="(plan, idx) in filteredActivePlans" :key="plan.id" v-reveal class="module-card premium-card delay-0" :class="`delay-${idx % 5}`">
               <div class="card-header-tags"><el-tag size="small" type="success" effect="dark" style="border:none">推进中</el-tag></div>
               <h3 class="card-title">{{ plan.title }}</h3>
               <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
               
               <div class="plan-specs mt-2">
                 <div class="spec-item"><el-icon><RefreshRight /></el-icon> {{ plan.frequency || '未设置' }}</div>
               </div>
               
               <div class="card-actions mt-3">
                 <el-button size="small" type="primary" plain round @click="openAdjustFrequency(plan)">调整频次</el-button>
                 <el-button size="small" type="danger" plain round @click="handleCancelPlan(plan.id)">退出计划</el-button>
               </div>
            </div>
            <el-empty v-if="filteredActivePlans.length === 0" description="暂无匹配的训练计划" />
          </div>
        </template>

        <template v-else-if="myTrainingTab === 'courses'">
          <div class="card-grid">
            <div v-for="(course, idx) in filteredManagedCourses" :key="course.id" v-reveal class="module-card premium-card delay-0" :class="`delay-${idx % 5}`">
               <div class="card-header-tags"><el-tag size="small" type="primary" effect="dark" style="border:none">单次课</el-tag></div>
               <h3 class="card-title">{{ course.title }}</h3>
               <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
               <div class="plan-specs mt-2">
                  <div class="spec-item"><el-icon><Timer /></el-icon> {{ course.durationMinutes }} 分钟</div>
                  <div class="spec-item" v-if="courseSchedulesByCourseId[course.id]?.length">
                    <el-icon><Calendar /></el-icon> 已安排 {{ courseSchedulesByCourseId[course.id].length }} 次
                  </div>
               </div>
               <div class="card-actions mt-3">
                 <el-button size="small" type="primary" plain round @click="openCourseArrange(course)">训练安排</el-button>
                 <el-button size="small" type="danger" text @click="handleRemoveCourse(course.id)">移除</el-button>
               </div>
            </div>
            <el-empty v-if="filteredManagedCourses.length === 0" description="暂无匹配的单次课程" />
          </div>
        </template>
      </template>

      <!-- Favorites / Want to Train -->
      <template v-if="activeMenu === 'favorites'">
        <div class="section-header">
          <h3>想练及收藏</h3>
          <el-radio-group v-model="favoriteTab" size="small">
            <el-radio-button label="plans">计划</el-radio-button>
            <el-radio-button label="courses">课程</el-radio-button>
          </el-radio-group>
        </div>

        <template v-if="favoriteTab === 'plans'">
          <div class="card-grid">
            <div v-for="plan in favoritePlans" :key="plan.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="warning" effect="dark" style="border:none">想练</el-tag></div>
               <h3 class="card-title">{{ plan.title }}</h3>
               <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
               <div class="card-actions mt-3">
                 <el-button size="small" type="warning" plain round @click="unfavorite(plan, 'PLAN')">移出想练</el-button>
               </div>
            </div>
            <el-empty v-if="favoritePlans.length === 0" description="暂无添加到想练的计划" />
          </div>
        </template>

        <template v-else>
          <div class="card-grid">
            <div v-for="course in favoriteCourses" :key="course.id" class="module-card premium-card">
               <div class="card-header-tags"><el-tag size="small" type="primary" effect="dark" style="border:none">单次课收藏</el-tag></div>
               <h3 class="card-title">{{ course.title }}</h3>
               <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
               <div class="card-actions mt-3">
                 <el-button size="small" type="warning" plain round @click="unfavorite(course, 'COURSE')">移出收藏</el-button>
               </div>
            </div>
            <el-empty v-if="favoriteCourses.length === 0" description="暂无收藏的单次课程" />
          </div>
        </template>
      </template>

      <!-- My Created -->
      <template v-if="activeMenu === 'created'">
        <div class="section-header">
          <h3>我的创建</h3>
          <div style="display:flex; gap: 12px; align-items: center;">
            <el-radio-group v-model="createdTab" size="small">
              <el-radio-button label="plan">计划</el-radio-button>
              <el-radio-button label="course">课程</el-radio-button>
            </el-radio-group>
            <el-button v-if="createdTab === 'plan'" type="primary" round @click="openCreatePlan"><el-icon><Plus/></el-icon> 新建计划</el-button>
            <el-button v-else type="primary" round @click="openCreateCourse"><el-icon><Plus/></el-icon> 新建课程</el-button>
          </div>
        </div>

        <template v-if="createdTab === 'plan'">
          <div class="card-grid">
            <div v-for="plan in userCreatedPlans" :key="plan.id" class="module-card premium-card">
              <div class="card-header-tags">
                <el-tag size="small" effect="dark" style="border:none">私有</el-tag>
              </div>
              <h3 class="card-title">{{ plan.title }}</h3>
              <p class="card-desc">{{ (plan.description || '').slice(0, 80) }}...</p>
              <div class="card-actions mt-3">
                <el-button size="small" type="primary" plain round @click="editMyPlan(plan)">编辑</el-button>
                <el-button size="small" type="warning" plain round @click="submitShare('PLAN', plan.id)">申请入库</el-button>
              </div>
            </div>
            <el-empty v-if="userCreatedPlans.length === 0" description="暂无自定义训练计划" />
          </div>
        </template>
        <template v-else>
          <div class="card-grid">
            <div v-for="course in myCreatedCourses" :key="course.id" class="module-card premium-card">
              <div class="card-header-tags">
                <el-tag size="small" type="primary" effect="dark" style="border:none">私有</el-tag>
              </div>
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-desc">{{ (course.description || '').slice(0, 80) }}...</p>
              <div class="plan-specs mt-2">
                <div class="spec-item"><el-icon><Timer /></el-icon> {{ course.durationMinutes || 0 }} 分钟</div>
              </div>
              <div class="card-actions mt-3">
                <el-button size="small" type="primary" plain round @click="editMyCourse(course)">编辑</el-button>
                <el-button size="small" type="warning" plain round @click="submitShare('COURSE', course.id)">申请入库</el-button>
              </div>
            </div>
            <el-empty v-if="myCreatedCourses.length === 0" description="暂无自定义训练课程" />
          </div>
        </template>
      </template>

    </div>

    <!-- Schedule Detail Modal -->
    <el-dialog v-model="scheduleDetailVisible" :title="activeSchedule?.title || '训练详情'" width="500px" align-center destroy-on-close>
      <div v-if="activeSchedule" class="schedule-detail-content">
        <div class="sd-status-bar" :class="getTaskStatusClass(activeSchedule)">
          <span class="sd-status-text">{{ mapStatus(activeSchedule.status, activeSchedule.date) }}</span>
        </div>
        
        <p class="sd-desc">{{ activeSchedule.description || '暂无描述' }}</p>
        
        <div class="sd-meta">
          <div class="sd-meta-item" v-if="activeSchedule.durationMinutes">
            <el-icon><Timer /></el-icon> {{ activeSchedule.durationMinutes }} 分钟
          </div>
          <div class="sd-meta-item" v-if="activeSchedule.category">
            <el-icon><Aim /></el-icon> {{ activeSchedule.category }}
          </div>
        </div>

        <div class="sd-actions-title">动作列表</div>
        <div class="actions-list">
          <div v-for="(act, idx) in parseActions(activeSchedule.actions)" :key="idx" class="al-item">
            <span class="al-name">{{ idx + 1 }}. {{ act.name }}</span>
            <span class="al-sets">{{ act.sets }}</span>
          </div>
          <div v-if="parseActions(activeSchedule.actions).length === 0" class="empty-subtle">
            暂无具体动作
          </div>
        </div>
      </div>
      <template #footer>
        <div class="sd-footer-actions">
          <template v-if="activeSchedule?.status === 'COMPLETED'">
            <div class="success-msg"><el-icon><Check /></el-icon> 任务已完成打卡</div>
          </template>
          <template v-else-if="activeSchedule?.status === 'PAUSED'">
             <el-button v-if="activeSchedule.date >= todayStr" type="primary" plain @click="handleResumeSchedule(activeSchedule)">恢复训练</el-button>
             <span v-else class="skipped-msg">该任务已暂停</span>
          </template>
          <template v-else-if="activeSchedule?.status === 'SKIPPED'">
            <span class="skipped-msg">该任务已被跳过</span>
          </template>
          <template v-else-if="activeSchedule?.date < todayStr">
            <span class="locked-msg"><el-icon><Lock /></el-icon> 仅当日可完成训练</span>
          </template>
          <template v-else-if="activeSchedule?.date > todayStr">
            <span class="future-msg">📅 训练日程未到，请届时再来</span>
          </template>
          <template v-else>
            <!-- Today's active tasks -->
            <el-button plain @click="handleSkipScheduleWrapper(activeSchedule.id)">跳过</el-button>
            <el-button type="primary" plain @click="handlePostponeScheduleWrapper(activeSchedule)">改期</el-button>
            <el-button type="success" @click="startTrainingFromModal(activeSchedule)">开始训练</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- Course Arrange Modal -->
    <el-dialog v-model="courseArrangeVisible" :title="activeCourse?.title || '课程训练安排'" width="520px" align-center destroy-on-close>
      <div v-if="activeCourse" class="schedule-detail-content">
        <div class="sd-desc" style="margin: 0;">
          {{ activeCourse.description || '暂无描述' }}
        </div>

        <div class="sd-actions-title">已安排日期</div>
        <div v-if="activeCourseSchedules.length === 0" class="empty-subtle">暂无安排</div>
        <div v-else class="arrange-list">
          <div v-for="s in activeCourseSchedules" :key="s.id" class="arrange-item">
            <div class="arrange-date">{{ s.date }}</div>
            <el-button size="small" type="danger" text @click="removeCourseSchedule(s.id)">移除</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="sd-footer-actions">
          <el-button type="primary" plain @click="openAdjustCourseDate(activeCourse, activeCourseSchedules)">编辑安排</el-button>
          <el-button @click="courseArrangeVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Immersive Training Panel Dialog -->
    <el-dialog v-model="immersiveVisible" :title="activeSessionTitle" width="800px" top="5vh" class="immersive-dialog" destroy-on-close>
      <div class="immersive-layout">
        <!-- Left: Action Checklist -->
        <div class="immersive-left">
          <h4 style="margin: 0 0 16px;">今日动作列表</h4>
          <div class="action-check-list">
            <el-checkbox-group v-model="completedActions">
              <div v-for="(act, idx) in activeSessionActions" :key="idx" class="action-check-item">
                <el-checkbox :label="idx">
                  <span class="ac-name">{{ act.name }}</span>
                  <span class="ac-sets">{{ act.sets }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
          <div class="progress-wrap">
             <el-progress :percentage="trainingProgress" :status="trainingProgress === 100 ? 'success' : ''" :stroke-width="12" />
          </div>
        </div>

        <!-- Right: Timer -->
        <div class="immersive-right">
          <div class="timer-display">
            <div class="t-lbl">当前用时</div>
            <div class="t-val">{{ formattedTimer }}</div>
            <el-button type="primary" plain size="small" @click="toggleTimer">{{ timerRunning ? '暂停' : '继续' }}</el-button>
          </div>
          <div class="tip-area mt-4">
            <h4 style="margin: 0 0 12px; font-size: 15px;">💡 训练说明</h4>
            <p style="font-size: 13px; color: #64748B; line-height: 1.6;">请对照左侧动作列表逐一完成。完成后点击下方按钮进行打卡记录。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button @click="closeImmersive" type="info" plain>中止退出</el-button>
          <el-button type="success" size="large" @click="handleImmersiveComplete" :loading="checkingIn" :disabled="trainingProgress < 100">
            <el-icon><Check /></el-icon> 完成训练
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Feedback / Check-in Dialog -->
    <el-dialog v-model="checkInVisible" title="训练反馈记录" width="450px" align-center destroy-on-close>
      <div class="checkin-success-header">
        <el-icon :color="completedMarked ? '#10b981' : '#94a3b8'" size="48">
          <Star v-if="completedMarked" />
          <Edit v-else />
        </el-icon>
        <div class="success-text">{{ completedMarked ? '训练已完成！' : '记录本次训练' }}</div>
        <p>请填写本次训练的感受</p>
      </div>

      <el-form :model="feedbackForm" label-position="top">
        <el-form-item label="阶段用时：">
           <b style="font-size: 20px; color: #1e293b;">{{ formattedTimer }}</b>
        </el-form-item>
        <el-form-item label="身体状态评定：">
          <el-radio-group v-model="feedbackForm.difficulty">
            <el-radio-button label="TOO_EASY">轻松</el-radio-button>
            <el-radio-button label="GOOD">适中</el-radio-button>
            <el-radio-button label="TOO_HARD">吃力</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="感受/备忘录 (选填)：">
          <el-input v-model="feedbackForm.feeling" type="textarea" :rows="3" placeholder="写下今天的一点感悟..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" size="large" @click="submitImmersiveCheckIn" :loading="checkingIn" style="width: 100%; border-radius: 12px;">提交反馈</el-button>
      </template>
    </el-dialog>

    <!-- Postpone Dialog -->
    <el-dialog v-model="postponeDialogVisible" title="改期" width="450px" align-center destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="选择新的训练日期">
          <el-date-picker
            v-model="postponeTargetDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            :disabled-date="disabledPostponeDate"
          />
        </el-form-item>
        <div style="background: #EFF6FF; padding: 12px; border-radius: 8px; font-size: 13px; color: #1D4ED8;">
          将把当前任务（以及计划的后续待执行任务）按顺序移动到所选日期。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="postponeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="postponing" @click="confirmPostpone">确认改期</el-button>
      </template>
    </el-dialog>

    <!-- Adjust Frequency Dialog -->
    <el-dialog v-model="frequencyDialogVisible" title="调整计划频次" width="450px" align-center destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="每周训练天数 (1-7)">
          <el-input-number v-model="frequencyDays" :min="1" :max="7" />
        </el-form-item>
        <div style="background: #FFFBEB; padding: 12px; border-radius: 8px; font-size: 13px; color: #B45309;">
          调整后的频次将从下周一自动生效。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="frequencyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjustFrequency">确认调整</el-button>
      </template>
    </el-dialog>

    <!-- Adjust Course Date Dialog -->
    <el-dialog v-model="courseDateDialogVisible" title="安排课程日期" width="450px" align-center destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="选择安排日期（可多选）">
          <el-date-picker
            v-model="courseDates"
            type="dates"
            placeholder="选择一个或多个日期"
            style="width: 100%"
          />
        </el-form-item>
        <div style="background: #F0FDFA; padding: 12px; border-radius: 8px; font-size: 13px; color: #0D9488;">
          <el-icon><InfoFilled /></el-icon> 每次选择的日期都会添加为当日的一个单次任务。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="courseDateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjustCourseDate">确认安排</el-button>
      </template>
    </el-dialog>

    <!-- Create / Edit Plan Dialog -->
    <el-dialog v-model="planEditorVisible" :title="editingPlanId ? '编辑训练计划' : '新建训练计划'" width="720px" align-center destroy-on-close>
      <el-form :model="planForm" label-position="top">
        <el-form-item label="计划标题">
          <el-input v-model="planForm.title" />
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="planForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类/目标肌群">
              <el-input v-model="planForm.category" placeholder="例如：上肢,核心" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划周期">
              <el-input v-model="planForm.duration" placeholder="例如：4周" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="sd-actions-title">动作编排</div>
        <div class="card-actions mt-2">
          <el-button type="primary" plain round @click="openExerciseSelector('plan')">从动作库添加</el-button>
        </div>
        <div class="actions-list" style="margin-top: 12px;">
          <div v-for="(a, idx) in planForm.actions" :key="idx" class="al-item">
            <span class="al-name">{{ a.name }}</span>
            <el-input v-model="a.sets" size="small" style="width: 160px" placeholder="例如 3x12" />
            <el-button size="small" type="danger" text @click="planForm.actions.splice(idx, 1)">移除</el-button>
          </div>
          <div v-if="planForm.actions.length === 0" class="empty-subtle">未添加动作</div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="planEditorVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMyPlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- Create / Edit Course Dialog -->
    <el-dialog v-model="courseEditorVisible" :title="editingCourseId ? '编辑训练课程' : '新建训练课程'" width="720px" align-center destroy-on-close>
      <el-form :model="courseForm" label-position="top">
        <el-form-item label="课程标题">
          <el-input v-model="courseForm.title" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="分类/目标肌群">
              <el-input v-model="courseForm.category" placeholder="例如：下肢" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="courseForm.difficulty" placeholder="选择难度">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="courseForm.durationMinutes" :min="5" :max="240" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="sd-actions-title">动作编排</div>
        <div class="card-actions mt-2">
          <el-button type="primary" plain round @click="openExerciseSelector('course')">从动作库添加</el-button>
        </div>
        <div class="actions-list" style="margin-top: 12px;">
          <div v-for="(a, idx) in courseForm.actions" :key="idx" class="al-item">
            <span class="al-name">{{ a.name }}</span>
            <el-input v-model="a.sets" size="small" style="width: 160px" placeholder="例如 3x12" />
            <el-button size="small" type="danger" text @click="courseForm.actions.splice(idx, 1)">移除</el-button>
          </div>
          <div v-if="courseForm.actions.length === 0" class="empty-subtle">未添加动作</div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="courseEditorVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMyCourse">保存</el-button>
      </template>
    </el-dialog>

    <!-- Exercise Selector Dialog -->
    <el-dialog v-model="exerciseSelectorVisible" title="选择动作" width="760px" align-center destroy-on-close>
      <div style="display:flex; gap: 12px; align-items:center; margin-bottom: 12px;">
        <el-input v-model="exerciseKeyword" placeholder="搜索动作名称..." clearable @keyup.enter="fetchExercises" @clear="fetchExercises" />
        <el-button type="primary" plain @click="fetchExercises">搜索</el-button>
      </div>
      <div v-loading="exerciseLoading">
        <el-table :data="exerciseList" style="width:100%">
          <el-table-column prop="name" label="动作" min-width="180" />
          <el-table-column prop="muscle" label="肌群" width="120" />
          <el-table-column prop="equipment" label="器械" width="120" />
          <el-table-column prop="difficulty" label="难度" width="90" />
          <el-table-column label="操作" width="120" align="right">
            <template #default="sc">
              <el-button size="small" type="primary" link @click="addExerciseToForm(sc.row)">添加</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!exerciseLoading && exerciseList.length === 0" description="暂无动作" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="exerciseSelectorVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, Star, Edit, Plus, Check, Lock, Timer, Aim, RefreshRight, InfoFilled, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import request from '../api/request'

const router = useRouter()
const route = useRoute()
const activeMenu = computed(() => (route.query.tab as string) || 'overview')
const loading = ref(false)
const myTrainingTab = ref('plans') // internal switcher for My Training
const scheduleDetailVisible = ref(false)
const activeSchedule = ref<any>(null)

const trainQuery = reactive({
  keyword: '',
  planGoal: '全部',
  planDifficulty: '全部',
  planFrequency: '全部',
  courseCategory: '全部',
  courseDifficulty: '全部',
  courseDuration: '全部'
})

const planGoals = ['全部', '减脂', '增肌', '体能', '康复']
const planDifficulties = ['全部', '初级', '中级', '高级']
const planFrequencies = ['全部', '每周2-3天', '每周4-5天', '每周6+天']
const courseDifficulties = ['全部', '初级', '中级', '高级']
const courseDurations = ['全部', '≤15分钟', '15-30分钟', '30-45分钟', '≥45分钟']

const noopSearch = () => {}

const openScheduleDetail = (sched: any) => {
  activeSchedule.value = sched
  scheduleDetailVisible.value = true
}

const handleSkipScheduleWrapper = async (id: number) => {
  await handleSkipSchedule(id)
  scheduleDetailVisible.value = false
}

const handlePostponeScheduleWrapper = (sched: any) => {
  scheduleDetailVisible.value = false
  handlePostponeSchedule(sched)
}

const startTrainingFromModal = (sched: any) => {
  scheduleDetailVisible.value = false
  openImmersiveTraining(sched, 'SCHEDULE')
}

// Month Data State
const selectedDate = ref(new Date())
const calendarView = ref<'month' | 'week'>('month')
const monthlySchedules = ref<any[]>([])
const allMyPlans = ref<any[]>([])
const myCourses = ref<any[]>([])
const favoritePlans = ref<any[]>([])
const favoriteCourses = ref<any[]>([])
const favoriteTab = ref('plans') // internal switcher for favorites

// Today's date string for calendar restriction
const todayStr = new Date().toISOString().split('T')[0]

const createdTab = ref<'plan' | 'course'>('plan')
const planEditorVisible = ref(false)
const courseEditorVisible = ref(false)
const editingPlanId = ref<number | null>(null)
const editingCourseId = ref<number | null>(null)

const planForm = reactive<{ title: string; description: string; category: string; duration: string; actions: Array<{ name: string; sets: string }> }>({
  title: '',
  description: '',
  category: '',
  duration: '',
  actions: []
})

const courseForm = reactive<{ title: string; description: string; category: string; difficulty: string; durationMinutes: number; actions: Array<{ name: string; sets: string }> }>({
  title: '',
  description: '',
  category: '',
  difficulty: '初级',
  durationMinutes: 30,
  actions: []
})

const myCreatedCourses = computed(() => {
  return myCourses.value
})

const exerciseSelectorVisible = ref(false)
const exerciseTarget = ref<'plan' | 'course'>('plan')
const exerciseLoading = ref(false)
const exerciseList = ref<any[]>([])
const exerciseKeyword = ref('')

const openExerciseSelector = async (target: 'plan' | 'course') => {
  exerciseTarget.value = target
  exerciseSelectorVisible.value = true
  await fetchExercises()
}

const fetchExercises = async () => {
  exerciseLoading.value = true
  try {
    const res: any = await request.get('/exercise/list', { params: { keyword: exerciseKeyword.value || '' } })
    exerciseList.value = res.data || []
  } catch (e) {
    exerciseList.value = []
  } finally {
    exerciseLoading.value = false
  }
}

const addExerciseToForm = (ex: any) => {
  const item = {
    name: ex.name,
    sets: ex.recommendedSets || '3x12'
  }
  if (exerciseTarget.value === 'plan') {
    planForm.actions.push(item)
  } else {
    courseForm.actions.push(item)
  }
}

const openCreatePlan = () => {
  editingPlanId.value = null
  Object.assign(planForm, { title: '', description: '', category: '', duration: '', actions: [] })
  planEditorVisible.value = true
}

const openCreateCourse = () => {
  editingCourseId.value = null
  Object.assign(courseForm, { title: '', description: '', category: '', difficulty: '初级', durationMinutes: 30, actions: [] })
  courseEditorVisible.value = true
}

const editMyPlan = (plan: any) => {
  editingPlanId.value = plan.id
  Object.assign(planForm, {
    title: plan.title || '',
    description: plan.description || '',
    category: plan.category || '',
    duration: plan.duration || '',
    actions: (() => {
      try { return JSON.parse(plan.actions || '[]') } catch { return [] }
    })()
  })
  planEditorVisible.value = true
}

const editMyCourse = (course: any) => {
  editingCourseId.value = course.id
  Object.assign(courseForm, {
    title: course.title || '',
    description: course.description || '',
    category: course.category || '',
    difficulty: course.difficulty || '初级',
    durationMinutes: course.durationMinutes || 30,
    actions: (() => {
      try { return JSON.parse(course.actionsJson || '[]') } catch { return [] }
    })()
  })
  courseEditorVisible.value = true
}

const saveMyPlan = async () => {
  if (!planForm.title.trim()) return ElMessage.warning('请填写计划标题')
  if (planForm.actions.length === 0) return ElMessage.warning('请至少添加一个动作')
  try {
    await request.post('/training/save', {
      id: editingPlanId.value || undefined,
      title: planForm.title,
      description: planForm.description,
      category: planForm.category,
      duration: planForm.duration,
      actions: JSON.stringify(planForm.actions),
      isPublic: false
    })
    ElMessage.success('已保存')
    planEditorVisible.value = false
    await fetchAllMyPlans()
  } catch (e) {}
}

const saveMyCourse = async () => {
  if (!courseForm.title.trim()) return ElMessage.warning('请填写课程标题')
  if (courseForm.actions.length === 0) return ElMessage.warning('请至少添加一个动作')
  try {
    await request.post('/course/save', {
      id: editingCourseId.value || undefined,
      title: courseForm.title,
      description: courseForm.description,
      category: courseForm.category,
      difficulty: courseForm.difficulty,
      durationMinutes: courseForm.durationMinutes,
      actionsJson: JSON.stringify(courseForm.actions),
      isPublic: false
    })
    ElMessage.success('已保存')
    courseEditorVisible.value = false
    await fetchMyCourses()
  } catch (e) {}
}

const submitShare = async (type: 'PLAN' | 'COURSE', id: number) => {
  try {
    await request.post('/resource/submit', { resourceType: type, resourceId: id })
    ElMessage.success('已提交入库申请，等待管理员审核')
  } catch (e) {}
}

const courseSchedules = ref<any[]>([])
const courseArrangeVisible = ref(false)
const activeCourse = ref<any>(null)

const courseSchedulesByCourseId = computed<Record<number, any[]>>(() => {
  const map: Record<number, any[]> = {}
  for (const s of courseSchedules.value) {
    if (!s || s.sourceType !== 'COURSE' || !s.courseId) continue
    const id = Number(s.courseId)
    if (!map[id]) map[id] = []
    map[id].push(s)
  }
  for (const k of Object.keys(map)) {
    map[Number(k)] = map[Number(k)].sort((a, b) => String(a.date).localeCompare(String(b.date)))
  }
  return map
})

const managedCourses = computed(() => {
  return myCourses.value.filter(c => (courseSchedulesByCourseId.value[c.id]?.length || 0) > 0)
})

const courseCategories = computed(() => {
  const set = new Set<string>()
  for (const c of managedCourses.value) {
    if (c?.category) set.add(String(c.category))
  }
  return ['全部', ...Array.from(set)]
})

const activeCourseSchedules = computed(() => {
  if (!activeCourse.value?.id) return []
  return courseSchedulesByCourseId.value[activeCourse.value.id] || []
})

const openCourseArrange = (course: any) => {
  activeCourse.value = course
  courseArrangeVisible.value = true
}

const upcomingSchedules = ref<any[]>([])

const managedSchedules = computed(() => {
  return [...upcomingSchedules.value]
    .filter(s => s && s.date >= todayStr && (s.status === 'PENDING' || s.status === 'PAUSED'))
    .sort((a, b) => String(a.date).localeCompare(String(b.date)))
})

// --- Training Dashboard (Global Overview) ---
const overviewSummary = ref<{ totalDurationMinutes: number; currentStreakDays: number; completedCount: number }>({
  totalDurationMinutes: 0,
  currentStreakDays: 0,
  completedCount: 0
})
const recentTrainings = ref<Array<{ date: string; title: string; durationMinutes: number }>>([])
const todaySchedules = ref<any[]>([])

const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: any = null
const overviewTrend = ref<{ labels: string[]; durations: number[] }>({ labels: [], durations: [] })

const formatMinutes = (minutes: number) => {
  const v = minutes || 0
  const h = Math.floor(v / 60)
  const m = v % 60
  if (h <= 0) return `${m} 分钟`
  return `${h} 小时 ${m} 分钟`
}

const formatDateShort = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(`${dateStr}T00:00:00`)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}-${dd}`
}

const formatScheduleStatus = (status: string) => {
  if (status === 'COMPLETED') return '已完成'
  if (status === 'PAUSED') return '已暂停'
  if (status === 'SKIPPED') return '已跳过'
  if (status === 'PENDING') return '待执行'
  return status || ''
}

const currentMonth = computed(() => {
  const d = selectedDate.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const formattedSelectedDate = computed(() => {
  const d = selectedDate.value
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dy = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${dy}`
})

const weekStartDate = computed(() => {
  const d = new Date(selectedDate.value)
  const day = d.getDay()
  const diff = (day + 6) % 7
  d.setDate(d.getDate() - diff)
  d.setHours(0, 0, 0, 0)
  return d
})

const weekDates = computed(() => {
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return labels.map((label, idx) => {
    const d = new Date(weekStartDate.value)
    d.setDate(d.getDate() + idx)
    const dateStr = formatDateOnly(d)
    return {
      label,
      dateStr,
      dayNum: String(d.getDate()).padStart(2, '0'),
      monthNum: String(d.getMonth() + 1).padStart(2, '0')
    }
  })
})

const calendarTitle = computed(() => {
  if (calendarView.value === 'month') return currentMonth.value
  const start = weekDates.value[0]
  const end = weekDates.value[6]
  return `${start.monthNum}-${start.dayNum} ~ ${end.monthNum}-${end.dayNum}`
})

const shiftCalendar = async (delta: number) => {
  const d = new Date(selectedDate.value)
  if (calendarView.value === 'month') {
    d.setMonth(d.getMonth() + delta)
  } else {
    d.setDate(d.getDate() + delta * 7)
  }
  selectedDate.value = d
  await fetchMonthSchedules(d)
}

const selectDate = async (dateStr: string) => {
  selectedDate.value = new Date(`${dateStr}T00:00:00`)
  await fetchMonthSchedules(selectedDate.value)
}

const selectedDaySchedules = computed(() => {
  return getSchedules(formattedSelectedDate.value)
})

const userCreatedPlans = computed(() => {
  // Plans where isPublic is false and sourceId is null means user created from scratch
  return allMyPlans.value.filter(p => !p.isPublic && p.sourceId == null)
})

const activePlans = computed(() => {
  return allMyPlans.value.filter(p => p.isSubscribed !== false && (p.status === 'ACTIVE' || p.status === 'PLANNING'))
})

const filteredActivePlans = computed(() => {
  const keyword = trainQuery.keyword.trim().toLowerCase()
  return activePlans.value.filter((p: any) => {
    const title = String(p?.title || '').toLowerCase()
    const desc = String(p?.description || '').toLowerCase()
    if (keyword && !title.includes(keyword) && !desc.includes(keyword)) return false
    if (trainQuery.planGoal !== '全部' && String(p?.goal || '') !== trainQuery.planGoal) return false
    if (trainQuery.planDifficulty !== '全部' && String(p?.difficulty || '') !== trainQuery.planDifficulty) return false
    if (trainQuery.planFrequency !== '全部') {
      const freq = String(p?.frequency || '')
      if (!freq.includes(trainQuery.planFrequency)) return false
    }
    return true
  })
})

const filteredManagedCourses = computed(() => {
  const keyword = trainQuery.keyword.trim().toLowerCase()
  return managedCourses.value.filter((c: any) => {
    const title = String(c?.title || '').toLowerCase()
    const desc = String(c?.description || '').toLowerCase()
    if (keyword && !title.includes(keyword) && !desc.includes(keyword)) return false
    if (trainQuery.courseCategory !== '全部' && String(c?.category || '') !== trainQuery.courseCategory) return false
    if (trainQuery.courseDifficulty !== '全部' && String(c?.difficulty || '') !== trainQuery.courseDifficulty) return false
    if (trainQuery.courseDuration !== '全部') {
      const mins = Number(c?.durationMinutes || 0)
      if (trainQuery.courseDuration === '≤15分钟' && !(mins <= 15)) return false
      if (trainQuery.courseDuration === '15-30分钟' && !(mins > 15 && mins <= 30)) return false
      if (trainQuery.courseDuration === '30-45分钟' && !(mins > 30 && mins <= 45)) return false
      if (trainQuery.courseDuration === '≥45分钟' && !(mins >= 45)) return false
    }
    return true
  })
})

// Data Fetching
const fetchMonthSchedules = async (dateObj: Date) => {
  loading.value = true
  try {
    // Get beginning and end of month +- buffer
    const d = new Date(dateObj)
    const y = d.getFullYear()
    const m = d.getMonth()
    const start = new Date(y, m - 1, 20).toISOString().split('T')[0]
    const end = new Date(y, m + 1, 10).toISOString().split('T')[0]
    
    const res: any = await request.get(`/daily/range?start=${start}&end=${end}`)
    monthlySchedules.value = res.data || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const fetchAllMyPlans = async () => {
    try {
        const res: any = await request.get('/training/list')
        allMyPlans.value = res.data || []
    } catch(e) {}
}

const fetchMyCourses = async () => {
    try {
        const res: any = await request.get('/course/my')
        myCourses.value = res.data || []
    } catch(e) {}
}

const fetchTrainingDashboardOverview = async () => {
  try {
    loading.value = true
    const [summaryRes, trendRes, recentRes, todayRes] = await Promise.all([
      request.get('/training/dashboard/summary'),
      request.get('/training/dashboard/trend?days=7'),
      request.get('/training/dashboard/recent?limit=10'),
      request.get('/daily/today')
    ])

    overviewSummary.value = summaryRes.data || overviewSummary.value
    overviewTrend.value.labels = trendRes.data?.labels || []
    overviewTrend.value.durations = trendRes.data?.durations || []
    recentTrainings.value = recentRes.data?.items || []
    todaySchedules.value = todayRes.data || []

    await nextTick()
    initTrendChart()
  } catch(e) {
    // keep page usable even if some endpoints fail
  } finally {
    loading.value = false
  }
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const labels = overviewTrend.value.labels || []
  const durations = overviewTrend.value.durations || []

  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: { left: 10, right: 10, top: 30, bottom: 20, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [
      {
        name: '训练时长',
        type: 'line',
        smooth: true,
        data: durations,
        lineStyle: { width: 3, color: '#409EFF' },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.18)'
        }
      }
    ]
  })
}

watch(
  () => route.query.tab,
  async (tab) => {
    if (tab === 'overview') {
      await fetchTrainingDashboardOverview()
      return
    }
    if (tab === 'calendar') {
      await fetchMonthSchedules(selectedDate.value)
      return
    }
    if (tab === 'plans') {
      await fetchAllMyPlans()
      await fetchMyCourses()
      await fetchCourseSchedules()
      return
    }
    if (tab === 'favorites') {
      await fetchFavorites()
      return
    }
    await fetchMonthSchedules(selectedDate.value)
  },
  { immediate: true }
)

watch(myTrainingTab, async (val) => {
  if (activeMenu.value === 'plans' && val === 'courses') {
    await fetchCourseSchedules()
  }
})

onUnmounted(() => {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})

const fetchFavorites = async () => {
  loading.value = true
  try {
    // Plans
    const resP: any = await request.get('/interaction/collections?type=PLAN')
    const pIds = resP.data.map((c: any) => c.targetId)
    const libResP: any = await request.get('/training/library')
    favoritePlans.value = libResP.data.filter((p: any) => pIds.includes(p.id))

    // Courses
    const resC: any = await request.get('/interaction/collections?type=COURSE')
    const cIds = resC.data.map((c: any) => c.targetId)
    const libResC: any = await request.get('/course/library')
    favoriteCourses.value = libResC.data.filter((c: any) => cIds.includes(c.id))
  } catch(e) {} finally {
    loading.value = false
  }
}

// Schedule Operations
const mapStatus = (s: string, dateStr: string) => {
  if (s === 'COMPLETED') return '已完成'
  if (s === 'SKIPPED') return '已跳过'
  if ((s === 'PENDING' || s === 'PAUSED') && dateStr === todayStr) return '进行中'
  return '未开始'
}

const getTaskStatusClass = (sched: any) => {
  if (sched.status === 'COMPLETED') return 'status-COMPLETED'
  if (sched.status === 'SKIPPED') return 'status-SKIPPED'
  if ((sched.status === 'PENDING' || sched.status === 'PAUSED') && sched.date === todayStr) return 'status-IN_PROGRESS'
  return 'status-PENDING'
}

const getScheduleCellClass = (s: any) => {
  if (s.status === 'COMPLETED') return 'completed'
  if (s.status === 'SKIPPED') return 'skipped'
  if ((s.status === 'PENDING' || s.status === 'PAUSED') && s.date === todayStr) return 'in-progress'
  return 'pending'
}

const getSchedules = (dateStr: string) => {
  return monthlySchedules.value.filter(s => s.date === dateStr)
}

const parseActions = (json: string) => {
  if (!json) return []
  try { return JSON.parse(json) } catch { return [] }
}

const frequencyDialogVisible = ref(false)
const frequencyDays = ref(3)
const targetFrequencyPlan = ref<any>(null)

const openAdjustFrequency = (plan: any) => {
  targetFrequencyPlan.value = plan
  // extract current frequency number if possible, else default 3
  const match = plan.frequency ? String(plan.frequency).match(/\d+/) : null
  frequencyDays.value = match ? parseInt(match[0]) : 3
  frequencyDialogVisible.value = true
}

const confirmAdjustFrequency = async () => {
  try {
    await request.post(`/training/subscribe/${targetFrequencyPlan.value.id}/frequency`, { daysPerWeek: frequencyDays.value })
    ElMessage.success('频次调整成功，下周生效')
    frequencyDialogVisible.value = false
    fetchAllMyPlans()
  } catch(e) {}
}

const courseDateDialogVisible = ref(false)
const courseDates = ref<Date[]>([])
const targetCourseDate = ref<any>(null)

const parseDateOnly = (dateStr: string) => new Date(`${dateStr}T00:00:00`)
const formatDateOnly2 = (d: Date) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const openAdjustCourseDate = (course: any, scheduledItems?: any[]) => {
  targetCourseDate.value = course
  courseDates.value = (scheduledItems || [])
    .map(s => parseDateOnly(String(s.date)))
  courseDateDialogVisible.value = true
}

const confirmAdjustCourseDate = async () => {
  try {
    const formattedDates = courseDates.value.map(d => formatDateOnly2(d))
    await request.post('/daily/course', {
      courseId: targetCourseDate.value.id,
      dates: formattedDates
    })
    ElMessage.success(courseDates.value.length === 0 ? '课程训练安排已清空' : '课程日期已成功安排')
    courseDateDialogVisible.value = false
    await fetchMonthSchedules(selectedDate.value)
    await fetchCourseSchedules()
    if (activeCourse.value?.id === targetCourseDate.value?.id) {
      if ((courseSchedulesByCourseId.value[targetCourseDate.value.id]?.length || 0) === 0) {
        courseArrangeVisible.value = false
      }
    }
  } catch(e) {}
}
const removeCourseSchedule = async (scheduleId: number) => {
  try {
    await request.delete(`/daily/${scheduleId}`)
    courseSchedules.value = courseSchedules.value.filter(s => s && s.id !== scheduleId)
    monthlySchedules.value = monthlySchedules.value.filter(s => s && s.id !== scheduleId)
    ElMessage.success('已移除该次安排')
    if (activeCourse.value?.id && (courseSchedulesByCourseId.value[activeCourse.value.id]?.length || 0) === 0) {
      courseArrangeVisible.value = false
    }
  } catch(e) {}
}

const postponeDialogVisible = ref(false)
const postponing = ref(false)
const postponeTargetSchedule = ref<any>(null)
const postponeTargetDate = ref<Date>(new Date())
const postponeMinTime = ref<number>(0)

const parseLocalDate = (dateStr: string) => {
  // Backend uses LocalDate (YYYY-MM-DD), force parse as local midnight.
  return new Date(`${dateStr}T00:00:00`)
}

const formatDateOnly = (d: Date) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const fetchUpcomingSchedules = async () => {
  loading.value = true
  try {
    const start = todayStr
    const end = formatDateOnly(new Date(Date.now() + 60 * 86400000))
    const res: any = await request.get(`/daily/range?start=${start}&end=${end}`)
    upcomingSchedules.value = res.data || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const fetchCourseSchedules = async () => {
  loading.value = true
  try {
    const start = todayStr
    const end = formatDateOnly(new Date(Date.now() + 365 * 86400000))
    const res: any = await request.get(`/daily/range?start=${start}&end=${end}`)
    courseSchedules.value = (res.data || []).filter((s: any) => s && s.sourceType === 'COURSE' && (s.status === 'PENDING' || s.status === 'PAUSED'))
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const disabledPostponeDate = (time: Date) => {
  return time.getTime() < postponeMinTime.value
}

const handleSkipSchedule = async (id: number, silent=false) => {
  try {
    if (!silent) {
        await ElMessageBox.confirm('确定要手动跳过今天的这项训练任务吗？将会标记为“已跳过”', '提示', { type: 'info' })
    }
    await request.post(`/daily/${id}/skip`)
    if (!silent) ElMessage.success('已跳过')
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handlePostponeSchedule = async (sched: any) => {
  postponeTargetSchedule.value = sched
  const original = parseLocalDate(String(sched.date))
  const minDate = new Date(original)
  // Only allow "later" postpone to avoid reversing order.
  minDate.setDate(minDate.getDate() + 1)
  postponeMinTime.value = minDate.getTime()
  postponeTargetDate.value = minDate
  postponeDialogVisible.value = true
}

const handlePauseSchedule = async (sched: any) => {
  try {
    await ElMessageBox.confirm(
      '将从当前任务开始，把后续训练日程全部设为暂停状态，是否继续？',
      '暂停训练确认',
      { type: 'warning' }
    )
    await request.post(`/daily/${sched.id}/pause`)
    ElMessage.success('暂停训练成功')
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleResumeSchedule = async (sched: any) => {
  try {
    await ElMessageBox.confirm(
      '将恢复当前及后续已暂停的训练日程，是否继续？',
      '恢复训练确认',
      { type: 'info' }
    )
    await request.post(`/daily/${sched.id}/resume`)
    ElMessage.success('恢复训练成功')
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const confirmPostpone = async () => {
  if (!postponeTargetSchedule.value) return
  try {
    postponing.value = true
    const sched = postponeTargetSchedule.value
    const targetDateStr = formatDateOnly(postponeTargetDate.value)
    await request.post(`/daily/${sched.id}/postpone`, { targetDate: targetDateStr })
    ElMessage.success('改期成功')
    postponeDialogVisible.value = false
    const nextSelected = new Date(`${targetDateStr}T00:00:00`)
    selectedDate.value = nextSelected
    await fetchMonthSchedules(nextSelected)
  } catch(e) {
  } finally {
    postponing.value = false
  }
}

const handleResetPlanProgress = async (sched: any) => {
  if (sched.sourceType !== 'PLAN' || !sched.planId) return
  try {
    await ElMessageBox.confirm(
      '该操作会将此训练计划进度清零，并从第一天重新生成日程，是否继续？',
      '重置确认',
      { type: 'warning' }
    )
    await request.post(`/daily/${sched.id}/reset`)
    ElMessage.success('训练计划已重置')
    await fetchAllMyPlans()
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleCancelTraining = async (sched: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该训练安排吗？',
      '取消训练确认',
      { type: 'warning' }
    )
    if (sched.sourceType === 'PLAN' && sched.planId) {
      await request.post(`/training/unsubscribe/${sched.planId}`)
      ElMessage.success('已取消训练计划')
      fetchAllMyPlans()
    } else {
      await request.delete(`/daily/${sched.id}`)
      ElMessage.success('已取消训练安排')
    }
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleCompleteScheduleDirectly = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要将此项任务标记为“已完成”吗？', '确认', { type: 'success' })
    await request.post(`/daily/${id}/complete`, { status: 'COMPLETED' })
    ElMessage.success('任务已标记完成')
    upcomingSchedules.value = upcomingSchedules.value.filter(s => s && s.id !== id)
    await fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const handleCancelPlan = async (planId: number) => {
  try {
    await ElMessageBox.confirm('这会导致正在跟踪的整套计划与余下日程被取消退订，确定吗？', '取消退订确认', { type: 'danger' })
    await request.post(`/training/unsubscribe/${planId}`) // Or delete API if fully implemented
    ElMessage.success('训练计划已取消并退订')
    fetchMonthSchedules(selectedDate.value)
    fetchAllMyPlans()
  } catch(e) {}
}

const handleRemoveCourse = async (courseId: number) => {
  try {
    await ElMessageBox.confirm('确定要从我的课程中移除该单次课吗？', '移除确认', { type: 'warning' })
    await request.delete(`/course/${courseId}`)
    ElMessage.success('单次课程已移除')
    fetchMyCourses()
    fetchMonthSchedules(selectedDate.value)
  } catch(e) {}
}

const unfavorite = async (item: any, type: string) => {
  try {
    await request.delete(`/interaction/collect?targetId=${item.id}&targetType=${type}`)
    ElMessage.success('已移出列表')
    fetchFavorites()
  } catch(e) {}
}

// Immersive Check-in Dialog
const immersiveVisible = ref(false)
const checkingIn = ref(false)
const activeSession = ref<any>(null)
const activeSessionType = ref<'SCHEDULE' | 'COURSE'>('SCHEDULE')
const activeSessionTitle = computed(() => {
  if (!activeSession.value) return ''
  return activeSessionType.value === 'COURSE' ? `[单次课] ${activeSession.value.title}` : `[日常排期] ${activeSession.value.title}`
})
const activeSessionActions = computed(() => {
  if (!activeSession.value) return []
  return parseActions(activeSessionType.value === 'COURSE' ? activeSession.value.actionsJson : activeSession.value.actions)
})

const completedActions = ref<number[]>([])
const trainingProgress = computed(() => {
  if (activeSessionActions.value.length === 0) return 100 // fallback if no actions
  return Math.round((completedActions.value.length / activeSessionActions.value.length) * 100)
})

// Timer logic
const timerSeconds = ref(0)
const timerRunning = ref(false)
let timerInterval: any = null

const formattedTimer = computed(() => {
  const m = Math.floor(timerSeconds.value / 60).toString().padStart(2, '0')
  const s = (timerSeconds.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const toggleTimer = () => {
  if (timerRunning.value) {
    clearInterval(timerInterval)
    timerRunning.value = false
  } else {
    timerRunning.value = true
    timerInterval = setInterval(() => { timerSeconds.value++ }, 1000)
  }
}

const feedbackForm = reactive({
  difficulty: 'GOOD',
  feeling: ''
})

const checkInVisible = ref(false)

const openImmersiveTraining = (session: any, type: 'SCHEDULE' | 'COURSE') => {
  activeSession.value = session
  activeSessionType.value = type
  completedActions.value = []
  timerSeconds.value = 0
  
  // reset feedback
  feedbackForm.difficulty = 'GOOD'
  feedbackForm.feeling = ''

  immersiveVisible.value = true
  
  // start timer instantly
  toggleTimer()
}

const closeImmersiveQuietly = () => {
  if (timerRunning.value) {
    clearInterval(timerInterval)
    timerRunning.value = false
  }
  immersiveVisible.value = false
}

const completedMarked = ref(false)
// Track whether the immersive session was completed (vs. aborted) for feedback routing
const sessionWasCompleted = ref(false)

const openCheckIn = (wasCompleted: boolean) => {
  completedMarked.value = wasCompleted
  sessionWasCompleted.value = wasCompleted
  if (timerRunning.value) toggleTimer()
  checkInVisible.value = true
}

const handleImmersiveComplete = async () => {
  // 1. Mark the schedule as COMPLETED first
  checkingIn.value = true
  try {
    if (activeSessionType.value === 'SCHEDULE' && activeSession.value?.id) {
      await request.post(`/daily/${activeSession.value.id}/complete`, { status: 'COMPLETED' })
      ElMessage.success('训练已标记完成')
      upcomingSchedules.value = upcomingSchedules.value.filter(s => s && s.id !== activeSession.value.id)
      await fetchMonthSchedules(selectedDate.value)
    }
  } catch(e) {} finally {
    checkingIn.value = false
  }
  // 2. Close panel and open feedback (no further status changes — feedback uses /feedback endpoint)
  closeImmersiveQuietly()
  openCheckIn(true)
}

const closeImmersive = async () => {
  try {
      await ElMessageBox.confirm('确定要中止本次训练吗？', '确认退出', {
          confirmButtonText: '确定中止',
          cancelButtonText: '继续训练',
          type: 'warning'
      })
      // Close the immersive panel first
      closeImmersiveQuietly()
      // Open feedback but mark as NOT completed (abort flow)
      openCheckIn(false)
  } catch (e) {
      // User clicked '继续训练'
  }
}

const submitImmersiveCheckIn = async () => {
  checkingIn.value = true
  const duration = Math.max(1, Math.floor(timerSeconds.value / 60))
  const payload = {
    completeDuration: duration,
    difficulty: feedbackForm.difficulty,
    feeling: feedbackForm.feeling
  }
  
  try {
    if (activeSessionType.value === 'COURSE') {
      // Course: use the existing complete endpoint (courses don't revert status here)
      await request.post(`/course/${activeSession.value.id}/complete`, payload)
    } else {
      // SCHEDULE: use dedicated feedback endpoints that do NOT touch schedule status
      if (activeSession.value?.id) {
        await request.post(`/daily/${activeSession.value.id}/feedback`, payload)
      } else {
        // No linked schedule (edge case)
        await request.post('/daily/feedback', payload)
      }
    }
    ElMessage.success('反馈已记录至健康档案 🎉')
    checkInVisible.value = false
  } catch(e) {
    ElMessage.error('提交失败，请重试')
  } finally {
    checkingIn.value = false
  }
}

// Global hook checking for "start" params directly from explore redirection
watch(() => route.query, (q) => {
  if (q.start && myCourses.value.length > 0) {
    const courseToStart = myCourses.value.find(c => String(c.id) === String(q.start))
    if (courseToStart) {
      // Auto open immersive panel
      openImmersiveTraining(courseToStart, 'COURSE')
      // remove query to prevent loop
      router.replace({ query: { tab: 'calendar' } })
    }
  }
}, { immediate: true })

// Watch date change to fetch new month data if needed
watch(selectedDate, async (newVal, oldVal) => {
  if (newVal.getMonth() !== oldVal.getMonth()) {
    await fetchMonthSchedules(newVal)
  }
})

onMounted(async () => {
  await fetchAllMyPlans()
  await fetchMyCourses()
  
  // process start query param if exists post fetch
  if (route.query.start) {
     const courseToStart = myCourses.value.find(c => String(c.id) === String(route.query.start))
     if (courseToStart) {
       openImmersiveTraining(courseToStart, 'COURSE')
       router.replace({ query: { tab: 'calendar' } })
     }
  }
})

</script>

<style scoped>
.training-dashboard {
  display: flex;
  height: 100%;
  background:
    radial-gradient(at 0% 0%, rgba(56, 189, 248, 0.12) 0px, transparent 55%),
    radial-gradient(at 100% 0%, rgba(251, 146, 60, 0.1) 0px, transparent 60%),
    radial-gradient(at 100% 100%, rgba(74, 222, 128, 0.12) 0px, transparent 55%),
    #F8FAFF;
}

.main-body {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.train-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.train-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.train-header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 1000;
  color: #0F172A;
}
.train-header-left p {
  margin: 8px 0 0;
  color: #64748B;
  font-size: 14px;
}
.train-header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-input {
  width: 360px;
  max-width: 44vw;
}
.filters-card {
  padding: 16px 24px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.filter-label {
  font-weight: 800;
  color: #334155;
  width: 56px;
  flex-shrink: 0;
  margin-top: 2px;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-tag {
  cursor: pointer;
  border-radius: 999px;
  padding: 8px 12px;
  font-weight: 800;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.section-header h3 {
  font-size: 22px;
  margin: 0;
  color: #1E293B;
}

/* Calendar Layout */
.calendar-layout {
  display: flex;
  gap: 32px;
}
.calendar-left {
  flex: 2;
  min-width: 600px;
}
.calendar-right {
  flex: 1;
  min-width: 320px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.calendar-title {
  font-size: 18px;
  font-weight: 800;
  color: #0F172A;
}
.calendar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Calendar Styling adjustments */
.premium-calendar {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}
.premium-calendar :deep(.el-calendar__header) {
  display: none;
}
.cell-content {
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}
.day-text {
  font-weight: 600;
  color: #334155;
  margin-bottom: 4px;
  display: block;
  text-align: left;
  padding: 4px;
}
.cell-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 0 4px;
  flex: 1;
  overflow-y: auto;
}
.cell-events::-webkit-scrollbar { display: none; }
.schedule-event {
  font-size: 11px;
  padding: 3px 6px;
  border-radius: 4px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
  line-height: 1.2;
}
.schedule-event.pending { background: rgba(148, 163, 184, 0.9); }
.schedule-event.in-progress { background: rgba(59, 130, 246, 0.9); }
.schedule-event.completed { background: rgba(16, 185, 129, 0.85); }
.schedule-event.skipped { background: rgba(245, 158, 11, 0.9); }
.schedule-event.missed { background: rgba(148, 163, 184, 0.85); }

.week-board {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}
.week-day {
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  padding: 12px;
  min-height: 220px;
  display: flex;
  flex-direction: column;
}
.week-day.is-active {
  border-color: rgba(59, 130, 246, 0.55);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}
.week-day-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding-bottom: 10px;
  border-bottom: 1px solid #F1F5F9;
}
.week-day-label {
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
}
.week-day-date {
  font-size: 12px;
  color: #64748B;
}
.week-day-body {
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  overflow-y: auto;
}
.week-task {
  font-size: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.week-task.pending { background: rgba(148, 163, 184, 0.9); }
.week-task.in-progress { background: rgba(59, 130, 246, 0.9); }
.week-task.completed { background: rgba(16, 185, 129, 0.85); }
.week-task.skipped { background: rgba(245, 158, 11, 0.9); }
.week-task.missed { background: rgba(148, 163, 184, 0.85); }
.week-empty {
  font-size: 12px;
  color: #94A3B8;
}

.selected-date-display {
  font-size: 14px;
  color: #94A3B8;
  margin-bottom: 16px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 8px;
}

/* Task Card */
.task-card {
  padding: 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;
}
/* Schedule Detail Modal */
.schedule-detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sd-status-bar {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
}
.sd-status-bar.status-PENDING { background: #F1F5F9; color: #64748B; }
.sd-status-bar.status-IN_PROGRESS { background: #DBEAFE; color: #2563EB; }
.sd-status-bar.status-COMPLETED { background: #D1FAE5; color: #059669; }
.sd-status-bar.status-SKIPPED { background: #FEF3C7; color: #D97706; }
.sd-status-bar.status-MISSED { background: #FEE2E2; color: #DC2626; }

.sd-desc {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0;
}
.sd-meta {
  display: flex;
  gap: 16px;
}
.sd-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #64748B;
}
.sd-actions-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  margin-top: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E2E8F0;
}
.sd-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}
.arrange-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.arrange-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #F8FAFC;
}
.arrange-date {
  font-size: 13px;
  color: #0F172A;
  font-weight: 700;
}
.clickable-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.clickable-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.08);
}
.tc-header-minimal {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tc-title-minimal {
  font-size: 16px;
  margin: 0;
  color: #1E293B;
}

.tc-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.tc-title h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #1E293B;
}
.tc-status {
  font-size: 12px;
  font-weight: 700;
}
.status-PENDING { color: #94A3B8; }
.status-IN_PROGRESS { color: #3B82F6; }
.status-COMPLETED { color: #10B981; }
.status-SKIPPED { color: #F59E0B; }
.status-MISSED { color: #94A3B8; font-weight: 800; }

.tc-desc {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 12px;
}

.actions-list {
  background: white;
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 16px;
}
.al-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  padding: 4px 0;
}

.tc-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
  width: 100%;
}

.success-msg { color: #10B981; font-weight: 700; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.skipped-msg { color: #94A3B8; font-style: italic; font-size: 14px; }
.locked-msg {
  color: #94A3B8;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #F1F5F9;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px dashed #CBD5E1;
}
.future-msg {
  color: #3B82F6;
  font-size: 13px;
  background: #EFF6FF;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px dashed #BFDBFE;
}

/* General Grids */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.module-card {
  padding: 20px;
  transition: all 0.25s;
}
.card-title { font-size: 16px; font-weight: 800; color: #1E293B; margin: 0 0 8px; line-height: 1.4; }
.card-desc { font-size: 13px; color: #64748B; line-height: 1.5; margin: 0; }

.mt-4 { margin-top: 24px; }
.mt-3 { margin-top: 16px; }
.mt-2 { margin-top: 8px; }

/* Immersive Dialog Styling */
.immersive-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #E2E8F0;
  margin-right: 0;
}
.immersive-layout {
  display: flex;
  gap: 32px;
  min-height: 400px;
}
.immersive-left {
  flex: 2;
  border-right: 1px solid #E2E8F0;
  padding-right: 32px;
}
.immersive-right {
  flex: 1;
}

.action-check-list {
  margin-bottom: 24px;
}
.action-check-item {
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}
.action-check-item:hover { background: #F1F5F9; }
.ac-name { font-weight: 700; color: #1E293B; margin-left: 8px; font-size: 15px; }
.ac-sets { color: #64748B; font-size: 13px; margin-left: 12px; }

.progress-wrap {
  margin-top: auto;
}

.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F8FAFC;
  padding: 24px;
  border-radius: 12px;
}
.t-lbl { color: #64748B; font-size: 14px; margin-bottom: 8px; }
.t-val { font-size: 48px; font-weight: 900; color: #1E293B; font-variant-numeric: tabular-nums; line-height: 1; margin-bottom: 16px; font-family: 'Courier New', monospace; letter-spacing: -2px; }

.checkin-success-header {
  text-align: center;
  padding: 10px 0 20px;
}
.checkin-success-header p {
  margin: 4px 0 0;
  color: #64748B;
  font-size: 14px;
}
.success-text {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-top: 12px;
}
.tip-area {
  background: #F8FAFC;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #3B82F6;
}

/* Training Dashboard Styles */
.overview-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-top-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 220px;
  padding: 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}

.stat-label {
  color: #64748B;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 26px;
  font-weight: 900;
  color: #1E293B;
}

.overview-chart-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px;
}

.overview-section-title {
  font-size: 18px;
  font-weight: 900;
  color: #1E293B;
  margin-bottom: 12px;
}

.trend-chart {
  height: 260px;
  width: 100%;
}

.overview-bottom {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.recent-card,
.today-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px;
}

.recent-card {
  flex: 1.1;
  min-width: 420px;
}

.today-card {
  flex: 0.9;
  min-width: 320px;
}

.empty-subtle {
  color: #94A3B8;
  font-size: 14px;
  padding: 8px 0;
}

.commit-timeline {
  padding-top: 6px;
}

.commit-item {
  display: flex;
  gap: 12px;
  position: relative;
  padding: 6px 0;
}

.commit-left {
  width: 26px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.commit-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409EFF;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.18);
}

.commit-line {
  width: 2px;
  flex: 1;
  background: #E5E7EB;
  margin-top: 6px;
}

.commit-body {
  flex: 1;
}

.commit-meta {
  color: #94A3B8;
  font-size: 12px;
  margin-bottom: 4px;
}

.commit-title {
  color: #1E293B;
  font-weight: 800;
  font-size: 14px;
  line-height: 1.3;
}

.today-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.today-item {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #F1F5F9;
  background: #F8FAFC;
}

.today-item-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
}

.today-title {
  font-size: 14px;
  font-weight: 900;
  color: #1E293B;
}

.today-status {
  font-size: 12px;
  font-weight: 800;
  color: #64748B;
  white-space: nowrap;
}

.today-desc {
  font-size: 13px;
  color: #64748B;
  line-height: 1.4;
}

@media (max-width: 1100px) {
  .overview-bottom {
    flex-direction: column;
  }

  .recent-card,
  .today-card {
    min-width: unset;
    width: 100%;
  }
}

</style>
