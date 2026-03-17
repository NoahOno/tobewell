<template>
  <div class="exercise-library" :class="{ 'is-select-mode': selectMode }">
    <div class="header">
      <div class="header-left">
        <h2>{{ selectMode ? '选择替换动作' : '动作图鉴' }}</h2>
        <p v-if="!selectMode">标准动作数据库，探索所有适合您的训练动作</p>
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
        @click="openDetail(ex)"
      >
        <div class="ex-image-placeholder">
          <!-- Placeholder for exercise video/gif -->
          <el-icon class="play-icon"><VideoPlay /></el-icon>
          <div class="difficulty-badge" :class="ex.difficulty">{{ ex.difficulty }}</div>
        </div>
        <div class="ex-info">
          <h3 class="ex-name">{{ ex.name }}</h3>
          <div class="ex-tags">
            <el-tag size="small" type="info">{{ ex.muscle }}</el-tag>
            <el-tag size="small" type="info">{{ ex.equipment }}</el-tag>
          </div>
        </div>
        <div v-if="selectMode" class="ex-action">
          <el-button type="primary" size="small" @click.stop="handleSelect(ex)">选择此动作</el-button>
        </div>
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" :title="currentEx?.name" width="500px" align-center destroy-on-close>
      <div v-if="currentEx" class="detail-content">
        <div class="video-box">
          <el-icon class="huge-icon"><VideoPlay /></el-icon>
          <p>演示视频</p>
        </div>
        <div class="detail-props">
          <div class="prop-item"><span class="label">目标肌群</span><span class="value">{{ currentEx.muscle }}</span></div>
          <div class="prop-item"><span class="label">训练类型</span><span class="value">{{ currentEx.type }}</span></div>
          <div class="prop-item"><span class="label">所需器械</span><span class="value">{{ currentEx.equipment }}</span></div>
          <div class="prop-item"><span class="label">适用难度</span><span class="value">{{ currentEx.difficulty }}</span></div>
        </div>
        
        <div class="detail-section">
          <h4>动作说明</h4>
          <p class="desc-text">{{ currentEx.instruction }}</p>
        </div>
        
        <div class="detail-section">
          <h4>常见错误</h4>
          <ul class="error-list">
            <li v-for="(err, i) in currentEx.commonErrors" :key="i">{{ err }}</li>
          </ul>
        </div>
        
        <div class="detail-section">
          <h4>推荐组数</h4>
          <p class="desc-text">{{ currentEx.recommendedSets }}</p>
        </div>
      </div>
      <template #footer v-if="selectMode">
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSelect(currentEx)">替换为此动作</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Search, VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  selectMode: { type: Boolean, default: false }
})

const emit = defineEmits(['select'])

// Mock Data for Exercises
const EXERCISES_DB = [
  { id: 1, name: '深蹲 (Squat)', muscle: '腿部', type: '力量', equipment: '无器械', difficulty: '初级', instruction: '双脚打开与肩同宽，腰背挺直，下蹲至大腿与地面平行。', commonErrors: ['膝盖内扣', '弯腰驼背', '重心不稳'], recommendedSets: '3-4组，每组12-15次' },
  { id: 2, name: '平板支撑 (Plank)', muscle: '核心', type: '力量', equipment: '无器械', difficulty: '初级', instruction: '手肘撑地，身体呈一条直线，收紧腹部和臀部。', commonErrors: ['塌腰', '撅屁股', '低头或仰头'], recommendedSets: '3-4组，每组30-60秒' },
  { id: 3, name: '开合跳 (Jumping Jacks)', muscle: '全身', type: '有氧', equipment: '无器械', difficulty: '初级', instruction: '跳跃时双脚分开，同时双手举过头顶击掌，落地时并拢。', commonErrors: ['落地过重', '手臂伸不直'], recommendedSets: '3组，每组30-45秒' },
  { id: 4, name: '波比跳 (Burpees)', muscle: '全身', type: '有氧', equipment: '无器械', difficulty: '高级', instruction: '下蹲、后踢腿成俯卧撑姿势，完成一个俯卧撑后收腿向上跳跃。', commonErrors: ['核心没有收紧', '跳跃高度不够'], recommendedSets: '3组，每组10-15次' },
  { id: 5, name: '杠铃卧推 (Bench Press)', muscle: '胸部', type: '力量', equipment: '杠铃', difficulty: '中级', instruction: '平躺在长椅上，双手握住杠铃，缓慢下放至胸口，然后推起。', commonErrors: ['手腕弯曲', '腰部过度反弓', '下放速度过快'], recommendedSets: '4组，每组8-12次' },
  { id: 6, name: '哑铃飞鸟 (Dumbbell Flyes)', muscle: '胸部', type: '力量', equipment: '哑铃', difficulty: '中级', instruction: '仰卧，双手持哑铃，手臂微屈，像拥抱一棵大树一样向外展开。', commonErrors: ['手臂伸得过直', '下放幅度过大导致肩膀受伤'], recommendedSets: '3组，每组10-15次' },
  { id: 7, name: '引体向上 (Pull-ups)', muscle: '背部', type: '力量', equipment: '单杠', difficulty: '高级', instruction: '双手握住单杠，收紧核心，背部发力将身体向上拉起，直到下巴过杠。', commonErrors: ['利用惯性甩动身体', '手臂发力过多'], recommendedSets: '4组，每组力竭' },
  { id: 8, name: '哑铃划船 (Dumbbell Row)', muscle: '背部', type: '力量', equipment: '哑铃', difficulty: '中级', instruction: '单膝跪在长椅上，另一只手持哑铃向后上方拉起，背部发力。', commonErrors: ['身体过度扭转', '依靠手臂力量拉起'], recommendedSets: '4组，每组10-12次' },
]

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
}

const filteredExercises = computed(() => {
  return EXERCISES_DB.filter(ex => {
    if (searchQuery.keyword && !ex.name.includes(searchQuery.keyword)) return false
    if (searchQuery.muscle !== '全部' && ex.muscle !== searchQuery.muscle) return false
    if (searchQuery.type !== '全部' && ex.type !== searchQuery.type) return false
    if (searchQuery.equipment !== '全部' && ex.equipment !== searchQuery.equipment) return false
    if (searchQuery.difficulty !== '全部' && ex.difficulty !== searchQuery.difficulty) return false
    return true
  })
})

const handleSearch = () => {
  // Triggers computed re-evaluation automatically
}

const openDetail = (ex: any) => {
  currentEx.value = ex
  detailVisible.value = true
}

const handleSelect = (ex: any) => {
  emit('select', ex)
  detailVisible.value = false
}
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

.exercise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.ex-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.ex-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.ex-image-placeholder {
  height: 140px;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.play-icon {
  font-size: 48px;
  color: white;
  opacity: 0.8;
}

.difficulty-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  background: #3b82f6;
}
.difficulty-badge.高级 { background: #ef4444; }
.difficulty-badge.初级 { background: #10b981; }

.ex-info {
  padding: 16px;
  flex: 1;
}

.ex-name {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.ex-tags {
  display: flex;
  gap: 8px;
}

.ex-action {
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
  text-align: right;
}

.empty-state {
  padding: 60px 0;
}

/* Detail dialog styles */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.video-box {
  background: #1e293b;
  border-radius: 8px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  gap: 12px;
}

.huge-icon { font-size: 64px; opacity: 0.8; }

.detail-props {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.prop-item {
  display: flex;
  justify-content: space-between;
}
.prop-item .label { color: #64748b; font-size: 13px; }
.prop-item .value { font-weight: 600; color: #1e293b; font-size: 13px; }

.detail-section h4 {
  margin: 0 0 8px;
  color: #1e293b;
  font-size: 15px;
}

.desc-text {
  margin: 0;
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
}

.error-list {
  margin: 0;
  padding-left: 20px;
  color: #ef4444;
  font-size: 14px;
}
</style>
