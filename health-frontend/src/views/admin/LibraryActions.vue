<template>
  <div class="library-actions admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">动作图鉴管理</h2>
        <p class="ph-desc">维护系统标准化动作库，包含演示、说明及建议组数</p>
      </div>
      <div class="ph-right">
        <el-input v-model="searchKeyword" placeholder="搜索名称..." style="width: 200px" clearable @input="fetchExercises" />
        <el-button type="primary" round style="margin-left: 12px;" @click="handleAddAction">动作入库</el-button>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table 
        :data="exercises" 
        style="width: 100%; cursor: pointer;"
        @row-click="editExercise"
      >
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
            <el-button size="small" type="primary" link @click.stop="editExercise(sc.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Drawer -->
    <el-drawer v-model="drawerVisible" :title="formExercise.id ? '编辑动作档案' : '构建动作档案'" size="850px" destroy-on-close>
       <div style="padding: 24px;">
         <el-form :model="formExercise" label-position="top">
           <el-row :gutter="32">
             <!-- 左侧基础信息 (40%) -->
             <el-col :span="10">
               <el-form-item label="封面图 (图片上传)">
                  <el-upload
                    class="premium-uploader"
                    style="width: 100%; height: 200px;"
                    action="/api/file/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleCoverSuccess"
                  >
                    <img v-if="formExercise.coverImage" :src="formExercise.coverImage" class="uploader-preview" />
                    <div v-else class="uploader-placeholder">
                      <el-icon class="uploader-icon"><Plus /></el-icon>
                      <span>上传封面图</span>
                    </div>
                  </el-upload>
               </el-form-item>
               <el-form-item label="动作名称">
                 <el-input v-model="formExercise.name" placeholder="填写动作名称" />
               </el-form-item>
               <el-form-item label="目标肌群">
                 <el-input v-model="formExercise.muscle" placeholder="填写主要发力肌肉" />
               </el-form-item>
               <el-row :gutter="12">
                 <el-col :span="12">
                   <el-form-item label="训练类型">
                     <el-input v-model="formExercise.type" placeholder="如: 力量" />
                   </el-form-item>
                 </el-col>
                 <el-col :span="12">
                   <el-form-item label="建议时长">
                     <el-input v-model="formExercise.duration" placeholder="秒/分" />
                   </el-form-item>
                 </el-col>
               </el-row>
             </el-col>

             <!-- 右侧核心训练信息 (60%) -->
             <el-col :span="14">
               <el-form-item label="GIF动图 / 演示视频">
                  <el-upload
                    class="premium-uploader video-uploader"
                    action="/api/file/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleVideoSuccess"
                  >
                    <img v-if="formExercise.videoUrl" :src="formExercise.videoUrl" class="uploader-preview" />
                    <div v-else class="uploader-placeholder">
                      <el-icon class="uploader-icon"><Plus /></el-icon>
                      <span>上传演示动图</span>
                    </div>
                  </el-upload>
               </el-form-item>

               <el-form-item label="难度级别" style="margin-bottom: 16px;">
                 <el-radio-group v-model="formExercise.difficulty">
                   <el-radio-button label="初级" />
                   <el-radio-button label="中级" />
                   <el-radio-button label="高级" />
                 </el-radio-group>
               </el-form-item>

               <el-row :gutter="16">
                 <el-col :span="12">
                   <el-form-item label="所需器械">
                     <el-input v-model="formExercise.equipment" placeholder="无器械则填无" />
                   </el-form-item>
                 </el-col>
                 <el-col :span="12">
                   <el-form-item label="建议设置 (组数/次数)">
                     <el-input v-model="formExercise.recommendedSets" placeholder="例如: 3x12次/40秒" />
                   </el-form-item>
                 </el-col>
               </el-row>

               <el-form-item label="动作说明" style="margin-bottom: 16px;">
                 <el-input v-model="formExercise.instruction" type="textarea" :rows="3" placeholder="填写动作的操作要领" />
               </el-form-item>

               <el-form-item label="常见错误与建议">
                 <el-input v-model="formExercise.commonErrors" type="textarea" :rows="2" placeholder="填写容易产生的受力错误" />
               </el-form-item>
             </el-col>
           </el-row>
         </el-form>
       </div>
       <template #footer>
          <div style="display:flex; justify-content: flex-end; padding:16px;">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" @click="saveExercise">保存编排</el-button>
          </div>
       </template>
    </el-drawer>

    <!-- Preview Drawer -->
    <el-drawer v-model="previewVisible" title="动作演示预览" size="950px" destroy-on-close>
       <div v-if="previewItem">
          <TrainingResourceViewer :item="previewItem" type="exercise" />
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

const exercises = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const drawerVisible = ref(false)
const previewVisible = ref(false)
const previewItem = ref<any>(null)

const difficultyTypeMap: any = { '初级': 'success', '中级': 'warning', '高级': 'danger' }

const uploadHeaders = computed(() => ({
  'satoken': localStorage.getItem('token') || ''
}))

const formExercise = reactive<any>({
  id: null, name: '', muscle: '', type: '力量', equipment: '', difficulty: '初级', instruction: '', commonErrors: '', recommendedSets: '', videoUrl: '', imageUrl: '', coverImage: '', isPublic: true, duration: ''
})

const fetchExercises = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/exercise/list')
    exercises.value = res.data.filter((e:any) => !searchKeyword.value || e.name.includes(searchKeyword.value))
  } finally { loading.value = false }
}

const toggleStatus = async (row: any) => {
  row.isPublic = !row.isPublic
  await request.post('/exercise/save', row)
  ElMessage.success(row.isPublic ? '动作已上架' : '动作已下架')
  fetchExercises()
}

const handleAddAction = () => {
  Object.assign(formExercise, { id: null, name: '', muscle: '', type: '力量', equipment: '', difficulty: '初级', instruction: '', commonErrors: '', recommendedSets: '', videoUrl: '', imageUrl: '', coverImage: '', isPublic: true, duration: '' })
  drawerVisible.value = true
}

const editExercise = (row: any) => {
  Object.assign(formExercise, row)
  drawerVisible.value = true
}

const handleCoverSuccess = (res: any) => {
  if (res.code === 200) {
    formExercise.coverImage = res.url
    ElMessage.success('封面图上传完成')
  }
}

const handleVideoSuccess = (res: any) => {
  if (res.code === 200) {
    formExercise.videoUrl = res.url
    ElMessage.success('动作演示上传完成')
  }
}

const saveExercise = async () => {
  await request.post('/exercise/save', formExercise)
  ElMessage.success('动作库已同步')
  drawerVisible.value = false
  fetchExercises()
}

const openPreview = (row: any) => {
  previewItem.value = row
  previewVisible.value = true
}

onMounted(fetchExercises)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px; }
.ph-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0; }
.ph-desc { font-size: 14px; color: var(--text-muted); }
.ex-cell { display: flex; flex-direction: column; }
.ex-name { font-weight: 700; color: var(--text-main); }
.ex-muscle { font-size: 12px; color: var(--text-light); }

/* Premium Uploader */
.premium-uploader {
  width: 120px;
  height: 120px;
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
.uploader-icon { font-size: 28px; color: #94a3b8; }

.video-uploader { width: 100%; height: 180px; }
.uploader-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94a3b8; font-size: 13px; font-weight: 600; }
</style>
