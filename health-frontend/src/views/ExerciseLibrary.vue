<template>
  <div class="exercise-library" :class="{ 'is-select-mode': selectMode }">
    <div class="header">
      <div class="header-left">
        <h2>{{ selectMode ? '选择替换动作' : '动作图鉴' }}</h2>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchQuery.keyword"
          placeholder="搜索动作名称..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-card premium-card">
      <div class="filter-row">
        <span class="filter-label">肌群</span>
        <div class="filter-options">
          <el-tag
            v-for="m in muscles"
            :key="m"
            :effect="searchQuery.muscle === m ? 'dark' : 'plain'"
            class="filter-tag"
            @click="toggleFilter('muscle', m)"
          >
            {{ m }}
          </el-tag>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">类型</span>
        <div class="filter-options">
          <el-tag
            v-for="t in types"
            :key="t"
            :effect="searchQuery.type === t ? 'dark' : 'plain'"
            class="filter-tag"
            @click="toggleFilter('type', t)"
          >
            {{ t }}
          </el-tag>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">器械</span>
        <div class="filter-options">
          <el-tag
            v-for="e in equipments"
            :key="e"
            :effect="searchQuery.equipment === e ? 'dark' : 'plain'"
            class="filter-tag"
            @click="toggleFilter('equipment', e)"
          >
            {{ e }}
          </el-tag>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">难度</span>
        <div class="filter-options">
          <el-tag
            v-for="d in difficulties"
            :key="d"
            :effect="searchQuery.difficulty === d ? 'dark' : 'plain'"
            class="filter-tag"
            @click="toggleFilter('difficulty', d)"
          >
            {{ d }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="filteredExercises.length === 0" class="empty-state">
      <el-empty description="没有找到匹配的动作" />
    </div>

    <!-- Grid -->
    <div v-else class="exercise-grid">
      <div
        v-for="ex in filteredExercises"
        :key="ex.id"
        class="ex-card premium-card"
        :class="{ 'is-offline': ex.isPublic === false }"
        @click="openDetail(ex)"
      >
        <!-- Card Cover / Visual -->
        <div class="ex-cover" :class="coverGradient(ex.muscle)">
          <el-icon class="ex-cover-icon"><VideoPlay /></el-icon>
          <div class="ex-difficulty-pill" :class="diffClass(ex.difficulty)">{{ ex.difficulty }}</div>
          <div class="ex-offline-badge" v-if="ex.isPublic === false">🚫 已下架</div>
        </div>
        <!-- Card Body -->
        <div class="ex-info">
          <h3 class="ex-name">{{ ex.name }}</h3>
          <div class="ex-meta">
            <span class="ex-meta-chip">{{ ex.muscle }}</span>
            <span class="ex-meta-chip">{{ ex.equipment }}</span>
            <span class="ex-meta-chip ex-type-chip">{{ ex.type }}</span>
          </div>
          <p class="ex-desc" v-if="ex.instruction">{{ ex.instruction?.slice(0, 55) }}...</p>
        </div>
        <div v-if="selectMode" class="ex-action">
          <el-button type="primary" round size="small" @click.stop="handleSelect(ex)">选择此动作</el-button>
        </div>
      </div>
    </div>

    <!-- Unified Premium Action Detail Dialog -->
    <el-dialog 
      v-model="detailVisible" 
      width="1100px" 
      style="border-radius: 24px; overflow: hidden;"
      class="premium-resource-dialog" 
      align-center 
      destroy-on-close
    >
      <div v-if="currentEx">
        <TrainingResourceViewer :item="currentEx" type="exercise">
           <template #left-actions v-if="selectMode">
              <div class="integrated-actions">
                 <el-button type="primary" size="large" class="btn-main" @click="handleSelect(currentEx)">
                    选择此动作作为替换
                 </el-button>
              </div>
           </template>
        </TrainingResourceViewer>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, VideoPlay } from '@element-plus/icons-vue'
import request from '../api/request'
import TrainingResourceViewer from '../components/TrainingResourceViewer.vue'

const props = defineProps({
  selectMode: { type: Boolean, default: false }
})

const emit = defineEmits(['select'])

const exercises = ref<any[]>([])
const loading = ref(false)

const muscles = ['全部', '胸部', '背部', '腿部', '核心', '全身', '上肢', '下肢']
const types = ['全部', '力量', '有氧', '拉伸']
const equipments = ['全部', '无器械', '哑铃', '杠铃', '单杠']
const difficulties = ['全部', '初级', '中级', '高级']

const searchQuery = reactive({
  keyword: '',
  muscle: '全部',
  type: '全部',
  equipment: '全部',
  difficulty: '全部'
})

const detailVisible = ref(false)
const currentEx = ref<any>(null)

const toggleFilter = (key: keyof typeof searchQuery, val: string) => {
  searchQuery[key] = val
  handleSearch()
}

const fetchExercises = async () => {
    loading.value = true
    try {
        const res: any = await request.get('/exercise/list', { params: searchQuery })
        exercises.value = res.data
    } finally {
        loading.value = false
    }
}

const filteredExercises = computed(() => exercises.value)

const handleSearch = () => {
  fetchExercises()
}

const openDetail = (ex: any) => {
  currentEx.value = ex
  // Process commonErrors if it's a string
  if (typeof currentEx.value.commonErrors === 'string') {
      try {
          currentEx.value.commonErrorsList = JSON.parse(currentEx.value.commonErrors)
      } catch (e) {
          currentEx.value.commonErrorsList = []
      }
  } else {
      currentEx.value.commonErrorsList = currentEx.value.commonErrors || []
  }
  detailVisible.value = true
}

const handleSelect = (ex: any) => {
  emit('select', ex)
  detailVisible.value = false
}

const coverGradient = (muscle: string) => {
  const m = muscle || ''
  if (m.includes('胸')) return 'grad-chest'
  if (m.includes('背')) return 'grad-back'
  if (m.includes('腿') || m.includes('下肢')) return 'grad-legs'
  if (m.includes('核心')) return 'grad-core'
  if (m.includes('肩') || m.includes('上肢')) return 'grad-shoulder'
  return 'grad-default'
}

const diffClass = (d: string) => {
  if (d === '高级') return 'diff-hard'
  if (d === '中级') return 'diff-medium'
  return 'diff-easy'
}

onMounted(fetchExercises)
</script>

<style scoped>
.exercise-library {
  padding: 24px;
}
.is-select-mode {
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h2 { margin: 0 0 8px; color: #1e293b; }
.header p { margin: 0; color: #64748b; font-size: 14px; }
.header-right { display: flex; gap: 12px; }

.filters-card {
  padding: 16px 24px;
  margin-bottom: 24px;
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
  font-weight: 600;
  color: #475569;
  width: 48px;
  flex-shrink: 0;
  line-height: 28px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tag {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 100px;
  padding: 0 16px;
}

/* ═══════════════════════
   Exercise Grid Cards
═══════════════════════ */
.exercise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.ex-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  border-radius: 18px;
  transition: transform 0.22s cubic-bezier(0.16,1,0.3,1), box-shadow 0.22s;
}
.ex-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 32px rgba(0,0,0,0.12) !important;
}
.ex-card.is-offline {
  opacity: 0.55;
  filter: grayscale(0.6);
}

/* Cover gradient bands */
.ex-cover {
  height: 130px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.grad-chest    { background: linear-gradient(135deg, #ef4444, #f97316); }
.grad-back     { background: linear-gradient(135deg, #3b82f6, #06b6d4); }
.grad-legs     { background: linear-gradient(135deg, #8b5cf6, #ec4899); }
.grad-core     { background: linear-gradient(135deg, #f59e0b, #10b981); }
.grad-shoulder { background: linear-gradient(135deg, #6366f1, #3b82f6); }
.grad-default  { background: linear-gradient(135deg, #1f8a70, #2d6cdf); }

.ex-cover-icon { font-size: 44px; color: rgba(255,255,255,0.7); }

.ex-difficulty-pill {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: 11px;
  font-weight: 700;
  color: white;
  background: rgba(0,0,0,0.28);
  backdrop-filter: blur(4px);
}
.diff-hard   { background: rgba(239,68,68,0.7) !important; }
.diff-medium { background: rgba(249,115,22,0.7) !important; }
.diff-easy   { background: rgba(16,185,129,0.7) !important; }

.ex-offline-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  border-radius: 100px;
  font-size: 10px;
  font-weight: 700;
  background: rgba(0,0,0,0.5);
  color: white;
}

.ex-info {
  padding: 14px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ex-name {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.3;
}
.ex-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.ex-meta-chip {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #64748b;
}
.ex-type-chip { background: #eff6ff; color: #3b82f6; }

/* Premium Training Detail Dialog Styles */
:deep(.premium-resource-dialog) {
  background: white;
  border-radius: 24px;
  overflow: hidden;
}

:deep(.premium-resource-dialog .el-dialog__header) {
  display: none;
}

:deep(.premium-resource-dialog .el-dialog__body) {
  padding: 0 !important;
}

/* Integrated Actions inside TRV Left Side */
.integrated-actions {
  display: flex;
  gap: 16px;
  margin-top: auto;
  padding-top: 40px;
}

.btn-main {
  flex: 1;
  height: 52px !important;
  font-weight: 800 !important;
  font-size: 15px !important;
  border-radius: 14px !important;
  background: #3b82f6 !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1) !important;
}

.btn-main:hover {
  background: #2563eb !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3) !important;
}

.ex-desc {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; /* Compatibility fix */
  -webkit-box-orient: vertical;
}

@media (max-width: 1024px) {
  :deep(.premium-resource-dialog) {
     width: 95% !important;
  }
}
</style>
