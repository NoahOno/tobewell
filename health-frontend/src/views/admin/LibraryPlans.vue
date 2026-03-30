<template>
  <div class="library-plans admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">系统训练计划</h2>
        <p class="ph-desc">维护系统官方标准化周期性训练计划</p>
      </div>
      <div class="ph-right">
        <el-input v-model="searchKeyword" placeholder="搜索计划..." style="width: 200px" clearable @input="fetchPlans" />
        <el-button type="primary" round style="margin-left: 12px;" @click="handleAddPlan">发布官方计划</el-button>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table 
        :data="plans" 
        style="width: 100%; cursor: pointer;"
        @row-click="editPlan"
      >
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
            <el-button size="small" type="primary" link @click.stop="editPlan(sc.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Drawer -->
    <el-drawer v-model="drawerVisible" :title="formPlan.id ? '编辑训练计划' : '构建训练计划'" size="550px" destroy-on-close>
       <div style="padding: 24px;">
         <el-form :model="formPlan" label-position="top">
           
           <el-form-item label="计划标题"><el-input v-model="formPlan.title" /></el-form-item>
           
           <el-form-item label="计划封面 (图片上传)">
              <el-upload
                class="premium-uploader banner-uploader"
                action="/api/file/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
              >
                <img v-if="formPlan.coverImage" :src="formPlan.coverImage" class="uploader-preview" />
                <div v-else class="uploader-placeholder">
                  <el-icon class="uploader-icon"><Plus /></el-icon>
                  <span>上传计划封面</span>
                </div>
              </el-upload>
           </el-form-item>

           <el-row :gutter="16">
             <el-col :span="8"><el-form-item label="计划周期 (如: 4周)"><el-input v-model="formPlan.duration" /></el-form-item></el-col>
             <el-col :span="8"><el-form-item label="目标分类 (如: 减脂)"><el-input v-model="formPlan.category" /></el-form-item></el-col>
             <el-col :span="8"><el-form-item label="适合人群"><el-input v-model="formPlan.audience" /></el-form-item></el-col>
           </el-row>
           <el-form-item label="计划详情简介"><el-input v-model="formPlan.description" type="textarea" :rows="3" /></el-form-item>
           
           <el-divider>周期结构编排</el-divider>
           <el-button type="primary" size="small" plain @click="addPlanDay">新增一天</el-button>
           <div class="plan-days-list">
              <div v-for="(day, dIdx) in formPlan.parsedActions" :key="dIdx" class="day-card">
                 <div class="day-header">
                    <span class="day-num">Day {{ Number(dIdx) + 1 }}</span>
                    <el-select v-model="day.type" size="small" style="width: 100px;">
                       <el-option label="训练日" value="训练" />
                       <el-option label="休息日" value="休息" />
                    </el-select>
                    <el-button type="danger" size="small" link @click="formPlan.parsedActions.splice(dIdx, 1)">移除</el-button>
                 </div>
                 <div v-if="day.type === '训练'" class="day-body">
                    <el-select 
                      v-model="day.courseId" 
                      placeholder="选择关联训练课程" 
                      style="width: 100%"
                      filterable
                      @change="(val: number) => handleCourseChange(val, day)"
                    >
                      <el-option 
                        v-for="c in courses" 
                        :key="c.id" 
                        :label="c.title" 
                        :value="c.id" 
                      />
                    </el-select>
                 </div>
                 <div v-else class="day-body rest-hint">
                    🛌 身体建议在这一天进行主动恢复或完全休息
                 </div>
              </div>
           </div>
         </el-form>
       </div>
       <template #footer>
          <div style="display:flex; justify-content: flex-end; padding:16px;">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" @click="savePlan">保存</el-button>
          </div>
       </template>
    </el-drawer>

    <!-- Preview Drawer -->
    <el-drawer v-model="previewVisible" title="训练计划预览" size="950px" destroy-on-close>
       <div v-if="previewItem">
          <TrainingResourceViewer :item="previewItem" type="plan" />
       </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../api/request'
import TrainingResourceViewer from '../../components/TrainingResourceViewer.vue'

const plans = ref<any[]>([])
const courses = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const drawerVisible = ref(false)
const previewVisible = ref(false)
const previewItem = ref<any>(null)

const uploadHeaders = computed(() => ({
  'satoken': localStorage.getItem('token') || ''
}))

const formPlan = reactive<any>({
  id: null, title: '', coverImage: '', duration: '', category: '', audience: '', description: '', actions: '', parsedActions: [], isPublic: true
})

const fetchPlans = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/plans')
    plans.value = res.data.filter((p:any) => !searchKeyword.value || p.title.includes(searchKeyword.value))
  } finally { loading.value = false }
}

const toggleStatus = async (row: any) => {
  row.isPublic = !row.isPublic
  await request.post('/admin/plan/save', row)
  ElMessage.success(row.isPublic ? '计划已上架' : '计划已下架')
  fetchPlans()
}

const fetchCourses = async () => {
  const res: any = await request.get('/admin/courses')
  courses.value = res.data
}

const handleAddPlan = () => {
  Object.assign(formPlan, { id: null, title: '', coverImage: '', duration: '', category: '', audience: '', description: '', actions: '', parsedActions: [], isPublic: true })
  drawerVisible.value = true
}

const editPlan = (row: any) => {
  Object.assign(formPlan, row)
  try { formPlan.parsedActions = row.actions ? JSON.parse(row.actions) : [] } catch(e) { formPlan.parsedActions = [] }
  drawerVisible.value = true
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    formPlan.coverImage = res.url
    ElMessage.success('计划封面上传完成')
  }
}

const openPreview = (row: any) => {
  const item = { ...row }
  try { item.parsedActions = row.actions ? JSON.parse(row.actions) : [] } catch(e) { item.parsedActions = [] }
  previewItem.value = item
  previewVisible.value = true
}

const savePlan = async () => {
  formPlan.actions = JSON.stringify(formPlan.parsedActions)
  await request.post('/admin/plan/save', formPlan)
  ElMessage.success('训练计划同步完成')
  drawerVisible.value = false
  fetchPlans()
}

const addPlanDay = () => formPlan.parsedActions.push({ type: '训练', title: '训练日', courseId: null, courseTitle: '' })

const handleCourseChange = (courseId: number, day: any) => {
  const c = courses.value.find(x => x.id === courseId)
  if (c) {
    day.courseTitle = c.title
    day.title = c.title
  }
}

onMounted(() => { 
  fetchPlans()
  fetchCourses()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.plan-cell { display:flex; flex-direction: column; }
.p-name { font-weight: 700; color: var(--text-main); }
.p-meta { font-size: 12px; color: var(--text-light); }
.day-card { background: #f8fafc; padding: 16px; border-radius: 12px; margin-top: 12px; }
.day-header { display:flex; justify-content: space-between; align-items:center; margin-bottom: 12px; }
.day-num { font-weight: 800; color: #3b82f6; }

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
