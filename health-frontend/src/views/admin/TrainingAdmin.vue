<template>
  <div class="training-admin admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">训练资源中心</h2>
        <p class="ph-desc">维护系统标准化动作库、官方训练计划及单次精选课程</p>
      </div>
      <div class="ph-right" style="display:flex; gap:12px">
        <el-input v-model="searchKeyword" placeholder="搜索名称或描述..." style="width: 200px" clearable />
        <el-button type="primary" class="add-btn-premium" @click="handleAddButtonClick" round>
          <el-icon><Plus /></el-icon> {{ addBtnText }}
        </el-button>
      </div>
    </div>
    
    <div class="admin-tabs-container">
      <el-tabs v-model="activeModule" class="premium-tabs" @tab-change="handleTabChange">
        <!-- 1. Exercise (Action) Library CRUD -->
        <el-tab-pane label="动作图鉴 (Exercises)" name="exercises">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="filteredExercises" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="动作信息" min-width="200">
                <template #default="sc">
                   <div class="ex-cell">
                     <span class="ex-name">{{ sc.row.name }}</span>
                     <span class="ex-muscle">{{ sc.row.muscle }} · {{ sc.row.type }}</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column prop="equipment" label="所需器械" width="120" />
              <el-table-column label="难度" width="100">
                <template #default="sc">
                  <el-tag :type="difficultyTypeMap[sc.row.difficulty]" size="small" effect="light">{{ sc.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="演示状态" width="100">
                 <template #default="sc">
                    <el-icon v-if="sc.row.imageUrl" color="var(--primary-color)"><CircleCheckFilled /></el-icon>
                    <el-icon v-else color="var(--text-light)"><CircleCloseFilled /></el-icon>
                 </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="sc">
                  <el-tag :type="sc.row.isPublic === false ? 'danger' : 'success'" size="small" effect="light">
                    {{ sc.row.isPublic === false ? '下架' : '上架中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="220" align="right">
                <template #default="sc">
                  <el-button size="small" type="info" link @click="openPreview(sc.row, 'exercise')">预览</el-button>
                  <el-button size="small" type="primary" link @click="editExercise(sc.row)">编辑</el-button>
                  <el-button size="small" :type="sc.row.isPublic === false ? 'success' : 'warning'" link @click="toggleOffline(sc.row, 'exercise')">
                    {{ sc.row.isPublic === false ? '重新上架' : '下架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 2. Public Plans CRUD -->
        <el-tab-pane label="官方计划 (Plans)" name="plans">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="filteredPlans" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="计划标题" min-width="250">
                <template #default="sc">
                  <div class="plan-cell">
                    <span class="p-name">{{ sc.row.title }}</span>
                    <span class="p-meta">{{ sc.row.category }} · {{ sc.row.duration }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column label="状态" width="90">
                <template #default="sc">
                  <el-tag :type="sc.row.isPublic === false ? 'danger' : 'success'" size="small" effect="light">
                    {{ sc.row.isPublic === false ? '下架' : '上架中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="220" align="right">
                <template #default="sc">
                  <el-button size="small" type="info" link @click="openPreview(sc.row, 'plan')">预览</el-button>
                  <el-button size="small" type="primary" link @click="editPlan(sc.row)">编辑</el-button>
                  <el-button size="small" :type="sc.row.isPublic === false ? 'success' : 'warning'" link @click="toggleOffline(sc.row, 'plan')">
                    {{ sc.row.isPublic === false ? '重新上架' : '下架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 3. Public Courses CRUD -->
        <el-tab-pane label="精品课程 (Courses)" name="courses">
          <div class="table-card premium-card" v-loading="loading">
            <el-table :data="filteredCourses" style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column label="课程信息" min-width="250">
                <template #default="sc">
                   <div class="course-cell">
                     <span class="c-name">{{ sc.row.title }}</span>
                     <span class="c-meta">{{ sc.row.category }} · {{ sc.row.durationMinutes }}分钟</span>
                   </div>
                </template>
              </el-table-column>
              <el-table-column label="难度" width="120">
                <template #default="sc">
                  <el-tag :type="difficultyTypeMap[sc.row.difficulty]" size="small">{{ sc.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="sc">
                  <el-tag :type="sc.row.isPublic === false ? 'danger' : 'success'" size="small" effect="light">
                    {{ sc.row.isPublic === false ? '下架' : '上架中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="管理" width="220" align="right">
                <template #default="sc">
                  <el-button size="small" type="info" link @click="openPreview(sc.row, 'course')">预览</el-button>
                  <el-button size="small" type="primary" link @click="editCourse(sc.row)">编辑</el-button>
                  <el-button size="small" :type="sc.row.isPublic === false ? 'success' : 'warning'" link @click="toggleOffline(sc.row, 'course')">
                    {{ sc.row.isPublic === false ? '重新上架' : '下架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- Modals (Dialogs) -->
    
     <el-drawer v-model="exerciseDialogVisible" title="构建动作档案" size="450px" class="builder-drawer" :show-close="true" destroy-on-close>
       <div style="padding: 24px;">
         <div class="builder-section-title">基础录入</div>
         <el-form :model="formExercise" label-position="top">
           <el-form-item label="动作名称"><el-input v-model="formExercise.name" placeholder="如：标准深蹲" /></el-form-item>
           <el-row :gutter="16">
             <el-col :span="12"><el-form-item label="目标肌群"><el-input v-model="formExercise.muscle" placeholder="如：部位" /></el-form-item></el-col>
             <el-col :span="12"><el-form-item label="训练类型"><el-input v-model="formExercise.type" placeholder="如：力量" /></el-form-item></el-col>
           </el-row>
           <el-form-item label="动作说明"><el-input v-model="formExercise.instruction" type="textarea" :rows="3" /></el-form-item>
           <el-row :gutter="16">
             <el-col :span="12"><el-form-item label="所需器械"><el-input v-model="formExercise.equipment" placeholder="如：自重|哑铃" /></el-form-item></el-col>
             <el-col :span="12">
               <el-form-item label="难度级别">
                 <el-radio-group v-model="formExercise.difficulty">
                   <el-radio-button label="初级"/><el-radio-button label="中级"/><el-radio-button label="高级"/>
                 </el-radio-group>
               </el-form-item>
             </el-col>
           </el-row>
           <el-form-item label="推荐组数/时长"><el-input v-model="formExercise.recommendedSets" placeholder="例如：3x12次，或 每组40秒" /></el-form-item>
           <el-form-item label="常见错误建议指南 (逗号分隔)"><el-input v-model="formExercise.commonErrors" type="textarea" :rows="2" placeholder="如：膝盖内扣,背部未挺直" /></el-form-item>
           <el-form-item label="演示视频链接"><el-input v-model="formExercise.videoUrl" placeholder="MP4/GIF 链接" /></el-form-item>
         </el-form>
       </div>
       <template #footer>
         <div style="display: flex; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid #E2E8F0;">
           <el-button @click="exerciseDialogVisible = false" size="large" round>取消</el-button>
           <el-button type="primary" @click="saveExercise" size="large" round>保存至图鉴库</el-button>
         </div>
       </template>
     </el-drawer>

     <!-- Preview Dialog Component — using shared TrainingResourceViewer -->
     <el-dialog v-model="previewVisible" :title="previewItem?.title || previewItem?.name || '资源预览'" width="620px" destroy-on-close align-center class="resource-detail-dialog">
       <div v-if="previewItem">
         <!-- Admin-only status bar -->
         <div class="admin-preview-status">
           <el-tag :type="previewItem.isPublic === false ? 'danger' : 'success'" effect="dark" size="large" round>
             {{ previewItem.isPublic === false ? '当前状态：已下架' : '当前状态：上架中' }}
           </el-tag>
           <span class="admin-preview-hint">以下为用户端实际渲染效果</span>
         </div>
         <TrainingResourceViewer :item="previewItem" :type="previewType" />
       </div>
       <template #footer>
         <el-button @click="previewVisible = false" round>关闭预览</el-button>
         <el-button
           :type="previewItem?.isPublic === false ? 'success' : 'warning'"
           round
           @click="toggleOffline(previewItem, previewType); previewVisible = false"
         >
           {{ previewItem?.isPublic === false ? '恢复上架' : '下架该资源' }}
         </el-button>
       </template>
     </el-dialog>


     <!-- Refactored Mainstream Builder Drawer -->
     <el-drawer v-model="planCourseDialogVisible" :title="activeModule === 'plans' ? '构建训练计划' : '编排单次课程'" size="80%" class="builder-drawer" :show-close="true" destroy-on-close>
       <div class="builder-layout">
         <!-- Left Side: Basic Info -->
         <div class="builder-sidebar">
           <div class="builder-section-title">基础配置</div>
           <el-form v-if="activeModule === 'plans'" :model="formPlan" label-position="top">
             <el-form-item label="封面图 URL"><el-input v-model="formPlan.coverImage" placeholder="计划封面图..." /></el-form-item>
             <el-form-item label="计划标题"><el-input v-model="formPlan.title" placeholder="如：四周极速减脂" /></el-form-item>
             <el-form-item label="计划周期"><el-input v-model="formPlan.duration" placeholder="如：4周" /></el-form-item>
             <el-form-item label="计划目标 (逗号分隔)"><el-input v-model="formPlan.category" placeholder="如：减脂,全身" /></el-form-item>
             <el-form-item label="一句话简介"><el-input v-model="formPlan.description" type="textarea" :rows="3" placeholder="简述计划受众和特点..."/></el-form-item>
           </el-form>
           
           <el-form v-else :model="formCourse" label-position="top">
             <el-form-item label="封面图 URL"><el-input v-model="formCourse.coverImage" placeholder="课程封面图..." /></el-form-item>
             <el-form-item label="课程标题"><el-input v-model="formCourse.title" placeholder="如：15分钟燃脂" /></el-form-item>
             <el-form-item label="建议时长(分钟)"><el-input-number v-model="formCourse.durationMinutes" :min="1" :max="300" style="width: 100%;"/></el-form-item>
             <el-form-item label="目标分类"><el-input v-model="formCourse.category" placeholder="如：有氧,核心"/></el-form-item>
             <el-form-item label="总体难度">
               <el-radio-group v-model="formCourse.difficulty" style="display:flex; flex-wrap:wrap; gap:8px;">
                 <el-radio-button label="初级" />
                 <el-radio-button label="中级" />
                 <el-radio-button label="高级" />
               </el-radio-group>
             </el-form-item>
           </el-form>
         </div>

         <!-- Right Side: Structure Builder -->
         <div class="builder-content">
           <div class="builder-section-title">
             {{ activeModule === 'plans' ? '周期结构编排' : '单课动作流编排' }}
             <el-button v-if="activeModule === 'plans'" size="small" type="primary" plain @click="addPlanDay">
               <el-icon><Plus /></el-icon> 新增一天
             </el-button>
             <el-button v-else size="small" type="primary" plain @click="openExerciseSelector('course')">
               <el-icon><Plus /></el-icon> 插入动作
             </el-button>
           </div>
           
           <!-- PLAN BUILDER UI -->
           <div v-if="activeModule === 'plans'" class="plan-days-container">
             <div v-for="(dayObj, dayIdx) in formPlan.parsedActions" :key="dayIdx" class="plan-day-card">
               <div class="pd-header">
                 <div class="pd-h-left">
                   <div class="pd-day-num">Day {{ dayIdx + 1 }}</div>
                   <el-select v-model="dayObj.type" size="small" style="width: 100px;">
                     <el-option label="训练日" value="训练" />
                     <el-option label="休息日" value="休息" />
                   </el-select>
                   <el-input v-if="dayObj.type === '训练'" v-model="dayObj.title" size="small" placeholder="训练阶段名..." style="width: 140px; margin-left: 8px;" />
                 </div>
                 <el-button size="small" type="danger" link @click="removePlanDay(dayIdx)"><el-icon><Delete /></el-icon></el-button>
               </div>
               
               <div v-if="dayObj.type === '训练'" class="pd-body">
                 <!-- Actions for this day -->
                 <div class="pd-actions-list">
                    <div v-for="(act, actIdx) in dayObj.actions" :key="actIdx" class="action-strip">
                       <div class="as-name"><strong>{{ actIdx + 1 }}.</strong> {{ act.name }}</div>
                       <el-input v-model="act.sets" size="small" placeholder="组数/时长(次)" class="as-input" />
                       <el-button type="danger" link size="small" @click="dayObj.actions.splice(actIdx, 1)"><el-icon><Close /></el-icon></el-button>
                    </div>
                 </div>
                 <div class="pd-add-btn">
                   <el-button size="small" round @click="openExerciseSelectorForDay(dayIdx)">
                     <el-icon><Plus /></el-icon> 添加动作
                   </el-button>
                 </div>
               </div>
               
               <div v-else class="pd-rest-body">
                 <el-icon class="rest-icon"><Mug /></el-icon>
                 <span>充分休息，让身体更好恢复</span>
               </div>
             </div>
             <el-empty v-if="formPlan.parsedActions.length === 0" description="点击右上角新增计划天数" />
           </div>

           <!-- COURSE BUILDER UI -->
           <div v-else class="course-timeline-container">
             <el-timeline v-if="formCourse.parsedActions.length > 0">
               <el-timeline-item v-for="(act, actIdx) in formCourse.parsedActions" :key="actIdx" type="primary" size="large">
                 <div class="course-action-card">
                   <div class="ca-header">
                     <span class="ca-name">{{ act.name }}</span>
                     <el-button size="small" type="danger" link @click="formCourse.parsedActions.splice(actIdx, 1)">移除</el-button>
                   </div>
                   <div class="ca-body">
                     <div class="ca-input-group">
                       <span class="ca-label">要求 (组数/次数)：</span>
                       <el-input v-model="act.sets" size="small" placeholder="例: 15次或30秒" />
                     </div>
                     <div class="ca-input-group">
                       <span class="ca-label">休息间隔：</span>
                       <el-input v-model="act.rest" size="small" placeholder="例: 10秒休息" />
                     </div>
                   </div>
                 </div>
               </el-timeline-item>
             </el-timeline>
             <el-empty v-else description="右上方插入首个动作" />
           </div>

         </div>
       </div>

       <template #footer>
         <div style="display: flex; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid #E2E8F0;">
           <el-button @click="planCourseDialogVisible = false" size="large" round>关闭丢弃</el-button>
           <el-button type="primary" @click="savePlanCourse" size="large" round>校验并发布资源</el-button>
         </div>
       </template>
     </el-drawer>

    <!-- Activity Dialog -->
    <el-dialog
      v-model="activityDialogVisible"
      :title="formActivity.id ? '编辑活动' : '发布活动'"
      width="620px"
      class="premium-dialog"
    >
      <el-form :model="formActivity" label-position="top">
        <el-form-item label="活动标题">
          <el-input v-model="formActivity.title" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="formActivity.coverImage" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="活动描述 (HTML 字符串)">
          <el-input v-model="formActivity.descriptionHtml" type="textarea" :rows="4" />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="formActivity.startTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="formActivity.endTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="完成活动的要求(天)">
          <el-input-number v-model="formActivity.requiredDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>

        <el-form-item label="任务模板类型">
          <el-radio-group v-model="formActivity.templateType">
            <el-radio-button label="PLAN" />
            <el-radio-button label="COURSE" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择模板">
          <el-select
            v-if="formActivity.templateType === 'PLAN'"
            v-model="formActivity.templateId"
            placeholder="请选择训练计划"
            style="width: 100%"
          >
            <el-option v-for="p in plans" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
          <el-select
            v-else
            v-model="formActivity.templateId"
            placeholder="请选择精选课程"
            style="width: 100%"
          >
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="置顶">
          <el-radio-group v-model="formActivity.pinned">
            <el-radio :label="1">置顶</el-radio>
            <el-radio :label="0">不置顶</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="活动状态">
          <el-radio-group v-model="formActivity.status">
            <el-radio label="ONLINE">在线</el-radio>
            <el-radio label="OFFLINE">下线</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="saveActivity" round>确认发布</el-button>
      </template>
    </el-dialog>

    <!-- Activity Analytics Dialog -->
    <el-dialog v-model="analyticsDialogVisible" title="活动数据看板" width="420px" class="premium-dialog">
      <el-descriptions border :column="1" size="small">
        <el-descriptions-item label="总参与人数">{{ analyticsData.totalParticipants }}</el-descriptions-item>
        <el-descriptions-item label="日活跃人数 (完成任意任务去重)">{{ analyticsData.dailyActive }}</el-descriptions-item>
        <el-descriptions-item label="完成人数">{{ analyticsData.completedParticipants }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ formatPercent(analyticsData.completionRate) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="analyticsDialogVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>

    <!-- Exercise Selector Dialog -->
    <el-dialog v-model="exerciseSelectorVisible" title="选择动作" width="760px" align-center destroy-on-close class="premium-dialog">
      <div style="display:flex; gap: 12px; align-items:center; margin-bottom: 12px;">
        <el-input v-model="exerciseKeyword" placeholder="搜索动作..." clearable />
      </div>
      <div>
        <el-table :data="filteredExerciseSelection" style="width:100%" height="300px">
          <el-table-column prop="name" label="动作" min-width="150" />
          <el-table-column prop="muscle" label="肌群" width="100" />
          <el-table-column label="操作" width="80" align="right">
            <template #default="sc">
              <el-button size="small" type="primary" link @click="addExerciseToForm(sc.row)">添加</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="exerciseSelectorVisible = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, CircleCheckFilled, CircleCloseFilled, Delete, Close, Mug } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { Search } from '@element-plus/icons-vue'
import TrainingResourceViewer from '../../components/TrainingResourceViewer.vue'

const searchKeyword = ref('')
const activeModule = ref('exercises')
const exercises = ref<any[]>([])
const plans = ref<any[]>([])
const courses = ref<any[]>([])
const activities = ref<any[]>([])
const loading = ref(false)

const difficultyTypeMap: any = {
  '初级': 'success',
  '中级': 'warning',
  '高级': 'danger'
}

const addBtnText = computed(() => {
  if (activeModule.value === 'exercises') return '动作入库'
  if (activeModule.value === 'plans') return '发布官方计划'
  if (activeModule.value === 'courses') return '新增精选课'
  return '发布活动'
})

// Exercise Logic


// --- Added By Patch: Preview & Offline ---
const previewVisible = ref(false)
const previewItem = ref<any>(null)
const previewType = ref('')
const previewArr = ref<any[]>([])

const openPreview = (row: any, type: string) => {
  previewItem.value = row
  previewType.value = type
  previewArr.value = []
  
  if (type === 'plan' && row.actions) {
     try { previewArr.value = JSON.parse(row.actions) } catch (e) {}
  } else if (type === 'course') {
     try { previewArr.value = JSON.parse(row.actionsJson || row.actions) } catch (e) {}
  }
  
  previewVisible.value = true
}

const toggleOffline = async (row: any, type: string) => {
  const currentPublic = row.isPublic !== false
  const updatedItem = { ...row, isPublic: !currentPublic }
  
  let endpoint = ''
  if (type === 'exercise') endpoint = '/exercise/save'
  else if (type === 'plan') endpoint = '/admin/plan/save'
  else if (type === 'course') endpoint = '/admin/course/save'
  
  try {
     await request.post(endpoint, updatedItem)
     ElMessage.success(updatedItem.isPublic ? '已将该资源恢复上架！' : '该资源已被成功下架。')
     if (type === 'exercise') fetchExercises()
     if (type === 'plan') fetchPlans()
     if (type === 'course') fetchCourses()
  } catch(e) { ElMessage.error('状态更新失败') }
}


const exerciseDialogVisible = ref(false)
// formExercise is used by the drawer template above
const formExercise = reactive<any>({ id: null, name: '', muscle: '', type: '力量', equipment: '', difficulty: '初级', instruction: '', commonErrors: '', recommendedSets: '', videoUrl: '', imageUrl: '', isPublic: true })
// Keep formEx as alias so old code doesn't break
const formEx = formExercise

const fetchExercises = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/exercise/list')
        exercises.value = res.data
    } finally { loading.value = false }
}
const editExercise = (row: any) => {
    Object.assign(formExercise, { id: null, name: '', muscle: '', type: '力量', equipment: '', difficulty: '初级', instruction: '', commonErrors: '', recommendedSets: '', videoUrl: '', imageUrl: '', isPublic: true })
    Object.assign(formExercise, row)
    exerciseDialogVisible.value = true
}
const saveExercise = async () => {
    await request.post('/exercise/save', formExercise)
    ElMessage.success('系统库已同步')
    exerciseDialogVisible.value = false
    fetchExercises()
}

// Plan & Course Logic
const planCourseDialogVisible = ref(false)
const formPlan = reactive<any>({ id: null, title: '', description: '', duration: '', category: '', actions: '', parsedActions: [], isPublic: true })
const formCourse = reactive<any>({ id: null, title: '', difficulty: '初级', category: '', durationMinutes: 30, coverImage: '', actionsJson: '', parsedActions: [], isPublic: true })

// Activity Admin Logic
const activityDialogVisible = ref(false)
const analyticsDialogVisible = ref(false)
const analyticsData = reactive<any>({
  totalParticipants: 0,
  dailyActive: 0,
  completedParticipants: 0,
  completionRate: 0
})
const formActivity = reactive<any>({
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

const formatDateTime = (t: any) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatPercent = (v: any) => {
  const num = Number(v)
  if (!isFinite(num)) return '0%'
  return (num * 100).toFixed(2) + '%'
}

const fetchPlans = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/admin/plans')
        plans.value = res.data
    } finally { loading.value = false }
}
const fetchCourses = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/admin/courses')
        courses.value = res.data
    } finally { loading.value = false }
}

const handleTabChange = () => {
    if (activeModule.value === 'exercises') fetchExercises()
    if (activeModule.value === 'plans') fetchPlans()
    if (activeModule.value === 'courses') fetchCourses()
    if (activeModule.value === 'activities') {
      fetchActivities()
      // activity form depends on template library lists
      if (plans.value.length === 0) fetchPlans()
      if (courses.value.length === 0) fetchCourses()
    }
}

const handleAddButtonClick = () => {
  if (activeModule.value === 'exercises') {
    Object.assign(formExercise, { id: null, name: '', muscle: '', type: '力量', equipment: '', difficulty: '初级', instruction: '', commonErrors: '', recommendedSets: '', videoUrl: '', imageUrl: '', isPublic: true })
    exerciseDialogVisible.value = true
  } else if(activeModule.value === 'plans') {
    Object.assign(formPlan, { id: null, title: '', description: '', duration: '', category: '', actions: '', parsedActions: [], isPublic: true })
    planCourseDialogVisible.value = true
  } else if (activeModule.value === 'activities') {
    Object.assign(formActivity, {
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
    activityDialogVisible.value = true
  } else {
    Object.assign(formCourse, { id: null, title: '', difficulty: '初级', category: '', durationMinutes: 30, coverImage: '', actionsJson: '', parsedActions: [], isPublic: true })
    planCourseDialogVisible.value = true
  }
}

const editPlan = (row: any) => {
  Object.assign(formPlan, { id: null, title: '', description: '', duration: '', category: '', actions: '', parsedActions: [], isPublic: true })
  Object.assign(formPlan, row)
  try { formPlan.parsedActions = row.actions ? JSON.parse(row.actions) : [] } catch (e) { formPlan.parsedActions = [] }
  // Make sure we open in plans mode
  activeModule.value = 'plans'
  planCourseDialogVisible.value = true
}

const editCourse = (row: any) => {
  Object.assign(formCourse, { id: null, title: '', difficulty: '初级', category: '', durationMinutes: 30, coverImage: '', actionsJson: '', parsedActions: [], isPublic: true })
  Object.assign(formCourse, row)
  try { formCourse.parsedActions = row.actionsJson ? JSON.parse(row.actionsJson) : [] } catch (e) { formCourse.parsedActions = [] }
  // Make sure we open in courses mode
  activeModule.value = 'courses'
  planCourseDialogVisible.value = true
}

const savePlanCourse = async () => {
  if (activeModule.value === 'plans') {
    formPlan.actions = JSON.stringify(formPlan.parsedActions || [])
    await request.post('/admin/plan/save', formPlan)
    fetchPlans()
  } else {
    formCourse.actionsJson = JSON.stringify(formCourse.parsedActions || [])
    await request.post('/admin/course/save', formCourse)
    fetchCourses()
  }
  ElMessage.success('公共库已同步')
  planCourseDialogVisible.value = false
}

// Activities CRUD
const fetchActivities = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/activities')
    activities.value = res.data
  } finally {
    loading.value = false
  }
}

const editActivity = (row: any) => {
  Object.assign(formActivity, row)
  activityDialogVisible.value = true
}

const saveActivity = async () => {
  await request.post('/admin/activities', formActivity)
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
  analyticsData.totalParticipants = res?.data?.totalParticipants ?? 0
  analyticsData.dailyActive = res?.data?.dailyActive ?? 0
  analyticsData.completedParticipants = res?.data?.completedParticipants ?? 0
  analyticsData.completionRate = res?.data?.completionRate ?? 0
  analyticsDialogVisible.value = true
}

const deleteActivity = async (id: number) => {
  await request.delete(`/admin/activities/${id}`)
  ElMessage.success('活动已删除')
  fetchActivities()
}



// Computeds for search filtering
const filteredExercises = computed(() => {
  if(!searchKeyword.value) return exercises.value
  return exercises.value.filter(e => e.name?.includes(searchKeyword.value) || e.muscle?.includes(searchKeyword.value))
})
const filteredPlans = computed(() => {
  if(!searchKeyword.value) return plans.value
  return plans.value.filter(p => p.title?.includes(searchKeyword.value) || p.description?.includes(searchKeyword.value))
})
const filteredCourses = computed(() => {
  if(!searchKeyword.value) return courses.value
  return courses.value.filter(c => c.title?.includes(searchKeyword.value) || c.category?.includes(searchKeyword.value))
})

// Exercise Selector

const addPlanDay = () => {
  if(!formPlan.parsedActions) formPlan.parsedActions = []
  formPlan.parsedActions.push({ day: formPlan.parsedActions.length + 1, type: '训练', title: '新的训练段', actions: [] })
}

const removePlanDay = (idx: number) => {
  formPlan.parsedActions.splice(idx, 1)
}

const currentPlanDayTargetIdx = ref(0)
const openExerciseSelectorForDay = (dayIdx: number) => {
  currentPlanDayTargetIdx.value = dayIdx
  openExerciseSelector('plan')
}

const addExerciseToForm = (ex: any) => {
  if (exerciseTarget.value === 'plan') {
    if(!formPlan.parsedActions[currentPlanDayTargetIdx.value].actions) formPlan.parsedActions[currentPlanDayTargetIdx.value].actions = []
    formPlan.parsedActions[currentPlanDayTargetIdx.value].actions.push({ name: ex.name, sets: ex.recommendedSets || '3组x12次' })
  } else {
    if(!formCourse.parsedActions) formCourse.parsedActions = []
    formCourse.parsedActions.push({ name: ex.name, sets: ex.recommendedSets || '15次', rest: '15秒休息' })
  }
  ElMessage.success(`已添加: ${ex.name}`)
}

const exerciseSelectorVisible = ref(false)
const exerciseKeyword = ref('')
const exerciseTarget = ref<'plan' | 'course'>('plan')
const filteredExerciseSelection = computed(() => {
  if(!exerciseKeyword.value) return exercises.value
  return exercises.value.filter(e => e.name?.includes(exerciseKeyword.value))
})
const openExerciseSelector = (target: 'plan' | 'course') => {
  exerciseTarget.value = target
  exerciseSelectorVisible.value = true
}
// addExerciseToFormOld removed — using addExerciseToForm above


onMounted(fetchExercises)
</script>

<style scoped>
.training-admin {
  animation: fadeIn 0.4s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
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

.add-btn-premium {
  height: 48px;
  padding: 0 24px;
  font-weight: 700;
  box-shadow: 0 10px 15px -3px rgba(74, 222, 128, 0.2);
}

.admin-tabs-container :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.admin-tabs-container :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-muted);
}

.table-card {
  padding: 0;
  margin-top: 16px;
}

.ex-cell, .plan-cell, .course-cell {
  display: flex;
  flex-direction: column;
}

.ex-name, .p-name, .c-name {
  font-weight: 700;
  color: var(--text-main);
}

.ex-muscle, .p-meta, .c-meta {
  font-size: 12px;
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
  color: var(--text-light);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Admin Builder Styles */
.builder-drawer :deep(.el-drawer__body) { padding: 0; }
.builder-layout {
  display: flex;
  height: 100%;
  background: #f8fafc;
}
.builder-sidebar {
  width: 320px;
  background: white;
  border-right: 1px solid #e2e8f0;
  padding: 24px;
  overflow-y: auto;
}
.builder-content {
  flex: 1;
  padding: 32px 48px;
  overflow-y: auto;
}
.builder-section-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* Plan Days */
.plan-days-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.plan-day-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.pd-header {
  padding: 12px 16px;
  background: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pd-h-left { display: flex; align-items: center; gap: 12px; }
.pd-day-num { font-weight: 800; color: #3b82f6; font-size: 16px; }
.pd-body { padding: 16px; }
.pd-rest-body { padding: 32px; text-align: center; color: #10b981; font-weight: 600; display: flex; flex-direction: column; align-items: center; gap: 8px;}
.rest-icon { font-size: 32px; }
.pd-actions-list { margin-bottom: 12px; display: flex; flex-direction: column; gap: 8px; }
.action-strip {
  display: flex;
  align-items: center;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 8px;
  gap: 12px;
}
.as-name { flex: 1; font-weight: 600; color: #334155; }
.as-input { width: 140px; }
/* Course Timeline */
.course-timeline-container { padding: 10px 0; }
.course-action-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}
.ca-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.ca-name { font-weight: 700; color: #1e293b; font-size: 16px; }
.ca-body {
  display: flex;
  gap: 16px;
}
.ca-input-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.ca-label { font-size: 12px; color: #64748b; }


.preview-resource-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pr-title { font-size: 20px; font-weight: 800; color: #1e293b; }
.pr-meta { display: flex; gap: 8px; align-items: center; }
.pr-detail { color: #64748b; font-size: 13px; margin-left: 8px; }
.pr-content-box {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}
/* Resource Preview Stylings Matching Explore.vue */
.rp-struct { display: flex; flex-direction: column; gap: 12px; margin-top: 12px; }
.rp-day-card { background: white; border: 1px solid #e2e8f0; padding:16px; border-radius: 8px; margin-bottom: 12px;}
.rp-day-lbl { font-size: 13px; font-weight: 700; color: #3b82f6;}
.rp-day-title { font-size: 15px; color: #1e293b; margin: 4px 0 12px;}
.rp-acts { display:flex; flex-wrap:wrap; gap:8px;}
.rp-act-badge { font-size: 12px; background: #f1f5f9; padding: 4px 10px; border-radius:4px; color: #475569;}
.rp-timeline-card { background: #f8fafc; padding: 12px; border-radius:8px; display:flex; flex-direction:column; gap:6px;}
.rtc-name { font-weight:600; color: #1e293b;}
.rtc-sets { font-size:13px; color: #64748b;}
</style>
