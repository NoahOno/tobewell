<template>
  <div class="records">
    <div class="record-grid">
      <!-- Input Sidebar -->
      <div class="record-left">
        <div class="premium-card premium-form-card">
          <div class="card-header-premium">
            <el-icon :size="24" color="#4ADE80"><EditPen /></el-icon>
            <h3>指标录入</h3>
          </div>
          <el-form :model="form" label-position="top" class="form-premium">
            <el-form-item label="指标名称">
              <el-autocomplete
                v-model="form.name"
                :fetch-suggestions="querySearch"
                placeholder="选择或输入指标"
                style="width: 100%"
                @select="handleSelect"
              />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="14">
                <el-form-item label="数值">
                  <el-input-number v-model="form.value" :precision="2" :step="0.1" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="单位">
                  <el-input v-model="form.unit" placeholder="kg, cm..." />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="记录时间">
              <el-date-picker
                v-model="form.recordTime"
                type="datetime"
                placeholder="记录时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="submitRecord" class="submit-btn" round>
              提交健康记录
            </el-button>
          </el-form>
        </div>
      </div>

      <!-- History Table -->
      <div class="record-right">
        <div class="premium-card premium-table-card">
          <div class="card-header-premium">
            <el-icon :size="24" color="#3B82F6"><Timer /></el-icon>
            <h3>历史指标记录</h3>
          </div>
          <el-table :data="recentRecords" style="width: 100%" v-loading="tableLoading" class="table-premium">
            <el-table-column prop="name" label="健康指标">
              <template #default="{ row }">
                <el-tag size="small" class="metric-tag" effect="light" :type="row.name === '体重' ? 'success' : 'primary'">
                  {{ row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数据数值">
              <template #default="{ row }">
                <span class="value-highlight">{{ row.value }}</span>
                <span class="unit-label">{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="更新时间" width="200">
              <template #default="{ row }">
                {{ formatTime(row.recordTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'
import { EditPen, Timer } from '@element-plus/icons-vue'

const loading = ref(false)
const tableLoading = ref(false)
const recentRecords = ref([])

const form = reactive({
  name: '',
  value: 0,
  unit: '',
  recordTime: new Date()
})

const defaultMetrics = [
  { value: '体重', unit: 'kg' },
  { value: '身高', unit: 'cm' },
  { value: '步数', unit: '步' },
  { value: '心率', unit: 'bpm' },
  { value: '睡眠时长', unit: 'h' }
]

const querySearch = (queryString: string, cb: any) => {
  const results = queryString
    ? defaultMetrics.filter(m => m.value.includes(queryString))
    : defaultMetrics
  cb(results)
}

const handleSelect = (item: any) => {
  form.unit = item.unit
}

const fetchRecentRecords = async () => {
  tableLoading.value = true
  try {
    const res: any = await request.get('/metric/latest')
    recentRecords.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    tableLoading.value = false
  }
}

const submitRecord = async () => {
  if (!form.name) return ElMessage.warning('请输入名称')
  loading.value = true
  try {
    await request.post('/metric/record', form)
    ElMessage.success('记录成功')
    fetchRecentRecords()
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  fetchRecentRecords()
})
</script>

<style scoped>
.records {
  padding: 40px 0 80px;
  max-width: 1200px;
  margin: 0 auto;
}

.record-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
}

.premium-form-card, .premium-table-card {
  padding: 32px;
}

.card-header-premium {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
}

.card-header-premium h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
}

.form-premium :deep(.el-form-item) {
  margin-bottom: 24px;
}

.form-premium :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-muted);
  padding-bottom: 8px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 10px 15px -3px rgba(74, 222, 128, 0.3);
  margin-top: 12px;
}

/* Table Customization */
.table-premium :deep(.el-table__header-wrapper th) {
  background-color: #F8FAFC !important;
  color: var(--text-muted);
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 16px 0;
}

.table-premium :deep(.el-table__row td) {
  padding: 16px 0;
  color: var(--text-main);
  font-weight: 500;
}

.metric-tag {
  font-weight: 700;
  border-radius: 8px;
}

.value-highlight {
  font-family: 'SF Pro Display', 'Inter', sans-serif;
  font-weight: 800;
  color: var(--primary-color);
  font-size: 16px;
}

.unit-label {
  font-size: 12px;
  color: var(--text-light);
  margin-left: 4px;
}
</style>
