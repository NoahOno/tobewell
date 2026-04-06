<template>
  <div class="library-courses admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">精品课程管理</h2>
        <p class="ph-desc">维护系统官方精选训练课程，包含动作序列编排</p>
      </div>
      <div class="ph-right">
        <el-input v-model="searchKeyword" placeholder="搜索课程..." style="width: 200px" clearable @input="fetchCourses" />
        <el-button type="primary" round style="margin-left: 12px;" @click="handleAddCourse">新增精选课</el-button>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table 
        :data="courses" 
        style="width: 100%; cursor: pointer;"
        @row-click="editCourse"
      >
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
        <el-table-column label="显示状态" width="100">
          <template #default="sc">
            <el-tag :type="sc.row.isPublic === false ? 'danger' : 'success'" size="small" effect="light">
              {{ sc.row.isPublic === false ? '已下架' : '展示中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="right">
          <template #default="sc">
            <el-button 
              size="small" 
              :type="sc.row.isPublic ? 'warning' : 'success'" 
              link 
              @click.stop="toggleStatus(sc.row)"
            >
              {{ sc.row.isPublic ? '下架' : '上架' }}
            </el-button>
            <el-divider direction="vertical" />
            <el-button size="small" type="info" link @click.stop="openPreview(sc.row)">预览</el-button>
            <el-button size="small" type="primary" link @click.stop="editCourse(sc.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Drawer -->
    <el-drawer v-model="drawerVisible" :title="formCourse.id ? '编辑课程' : '新增精选课'" size="850px" destroy-on-close>
       <div style="padding: 24px;">
         <el-form :model="formCourse" label-position="top">
           <el-row :gutter="32">
             <!-- 左侧基础信息 -->
             <el-col :span="10">
               <el-form-item label="课程封面 (图片上传)">
                  <el-upload
                    class="premium-uploader banner-uploader"
                    style="height: 200px;"
                    action="/api/file/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleUploadSuccess"
                  >
                    <img v-if="formCourse.coverImage" :src="formCourse.coverImage" class="uploader-preview" />
                    <div v-else class="uploader-placeholder">
                      <el-icon class="uploader-icon"><Plus /></el-icon>
                      <span>上传封面图</span>
                    </div>
                  </el-upload>
               </el-form-item>
               <el-form-item label="课程标题"><el-input v-model="formCourse.title" placeholder="填写课程名称" /></el-form-item>
               <el-form-item label="分类"><el-input v-model="formCourse.category" placeholder="如: 增肌、减脂" /></el-form-item>
               <el-form-item label="适合人群"><el-input v-model="formCourse.audience" placeholder="如: 新手、进阶" /></el-form-item>
             </el-col>

             <!-- 右侧核心训练信息 -->
             <el-col :span="14">
               <el-row :gutter="16">
                 <el-col :span="12">
                   <el-form-item label="时长 (分钟)">
                     <el-input-number v-model="formCourse.durationMinutes" style="width: 100%" />
                   </el-form-item>
                 </el-col>
                 <el-col :span="12">
                   <el-form-item label="难度">
                      <el-radio-group v-model="formCourse.difficulty">
                        <el-radio-button label="初级"/><el-radio-button label="中级"/><el-radio-button label="高级"/>
                      </el-radio-group>
                   </el-form-item>
                 </el-col>
               </el-row>
               
               <el-divider>动作轨迹编排</el-divider>
               <el-button type="primary" size="small" plain @click="openExerciseSelector" style="margin-bottom: 12px;">插入动作</el-button>
               <div class="course-actions-list" style="max-height: 400px; overflow-y: auto; padding-right: 8px;">
                  <div v-for="(act, idx) in formCourse.parsedActions" :key="idx" class="action-strip">
                     <span class="idx">{{ Number(idx) + 1 }}</span>
                     <span class="name">{{ act.name }}</span>
                     <el-input v-model="act.sets" size="small" placeholder="组/次" style="width: 80px" />
                     <el-button type="danger" size="small" link @click="formCourse.parsedActions.splice(Number(idx), 1)">移除</el-button>
                  </div>
                  <div v-if="!formCourse.parsedActions || formCourse.parsedActions.length === 0" style="text-align: center; color: #94a3b8; font-size: 13px; padding: 24px 0;">
                    暂无编排动作，请点击上方按钮插入
                  </div>
               </div>
             </el-col>
           </el-row>
         </el-form>
       </div>
       <template #footer>
          <div style="display:flex; justify-content: flex-end; padding:16px;">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" @click="saveCourse">保存编排</el-button>
          </div>
       </template>
    </el-drawer>

    <!-- Preview Drawer -->
    <el-drawer v-model="previewVisible" title="精品课程预览" size="950px" destroy-on-close>
       <div v-if="previewItem">
          <TrainingResourceViewer :item="previewItem" type="course" />
       </div>
    </el-drawer>

    <!-- Exercise Selector -->
    <el-dialog v-model="selectorVisible" title="选择动作" width="500px">
       <el-input v-model="exKeyword" placeholder="搜索动作..." clearable style="margin-bottom: 12px" />
       <el-table :data="filteredExercises" height="300px" @row-click="addActionToCourse">
          <el-table-column prop="name" label="动作名" />
          <el-table-column prop="muscle" label="肌群" />
       </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../api/request'
import TrainingResourceViewer from '../../components/TrainingResourceViewer.vue'

const courses = ref<any[]>([])
const exercises = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const exKeyword = ref('')
const drawerVisible = ref(false)
const previewVisible = ref(false)
const selectorVisible = ref(false)
const previewItem = ref<any>(null)

const difficultyTypeMap: any = { '初级': 'success', '中级': 'warning', '高级': 'danger' }

const uploadHeaders = computed(() => ({
  'satoken': localStorage.getItem('token') || ''
}))

const formCourse = reactive<any>({
  id: null, title: '', coverImage: '', category: '', audience: '', durationMinutes: 15, difficulty: '初级', isPublic: true, actionsJson: '', parsedActions: []
})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/courses')
    courses.value = res.data.filter((c:any) => !searchKeyword.value || c.title.includes(searchKeyword.value))
  } finally { loading.value = false }
}

const toggleStatus = async (row: any) => {
  row.isPublic = !row.isPublic
  await request.post('/admin/course/save', row)
  ElMessage.success(row.isPublic ? '课程已上架' : '课程已下架')
  fetchCourses()
}

const fetchExercises = async () => {
  const res: any = await request.get('/exercise/list')
  exercises.value = res.data
}

const filteredExercises = computed(() => exercises.value.filter(e => !exKeyword.value || e.name.includes(exKeyword.value)))

const handleAddCourse = () => {
  Object.assign(formCourse, { id: null, title: '', coverImage: '', category: '', audience: '', durationMinutes: 15, difficulty: '初级', isPublic: true, actionsJson: '', parsedActions: [] })
  drawerVisible.value = true
}

const editCourse = (row: any) => {
  Object.assign(formCourse, row)
  try { formCourse.parsedActions = row.actionsJson ? JSON.parse(row.actionsJson) : [] } catch(e) { formCourse.parsedActions = [] }
  drawerVisible.value = true
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    formCourse.coverImage = res.url
    ElMessage.success('课程封面上传完成')
  }
}

const openPreview = (row: any) => {
  const item = { ...row }
  try { item.parsedActions = row.actionsJson ? JSON.parse(row.actionsJson) : [] } catch(e) { item.parsedActions = [] }
  previewItem.value = item
  previewVisible.value = true
}

const saveCourse = async () => {
  formCourse.actionsJson = JSON.stringify(formCourse.parsedActions)
  await request.post('/admin/course/save', formCourse)
  ElMessage.success('精品课程已同步')
  drawerVisible.value = false
  fetchCourses()
}

const openExerciseSelector = () => selectorVisible.value = true
const addActionToCourse = (row: any) => {
  formCourse.parsedActions.push({ name: row.name, sets: row.recommendedSets || '15次', rest: '15秒' })
  ElMessage.success(`添加成功: ${row.name}`)
}

onMounted(() => {
  fetchCourses()
  fetchExercises()
})
</script>

<style scoped>
.page-header { display:flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.course-cell { display:flex; flex-direction: column; }
.c-name { font-weight: 700; color: var(--text-main); }
.c-meta { font-size: 12px; color: var(--text-light); }
.course-actions-list { margin-top: 12px; }
.action-strip { display: flex; align-items: center; gap: 12px; background: #f8fafc; padding: 8px 12px; border-radius: 8px; margin-bottom: 8px; }
.idx { font-weight: 800; color: #3b82f6; }
.name { flex: 1; font-weight: 600; font-size: 14px; }

/* Premium Uploader */
.premium-uploader {
  width: 100%;
  height: 160px;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  transition: all 0.3s;
}
.premium-uploader:hover { border-color: #3b82f6; background: #eff6ff; }
.uploader-preview { width: 100%; height: 100%; object-fit: cover; }
.uploader-icon { font-size: 32px; color: #94a3b8; }
.uploader-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94a3b8; font-size: 14px; font-weight: 600; }
</style>
