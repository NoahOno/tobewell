<template>
  <div class="collections">
    <div class="page-header">
      <div class="header-content">
        <h1>我的收藏</h1>
        <p>你收藏的所有优质健康内容都在这里</p>
      </div>
    </div>

    <div v-loading="loading" class="content-container">
      <el-tabs v-model="activeTab" class="collection-tabs">
        <el-tab-pane label="训练计划" name="PLAN">
          <div v-if="collectedPlans.length > 0" class="collection-grid">
            <div v-for="item in collectedPlans" :key="item.id" class="collection-card premium-card">
              <div class="card-type-tag">训练计划</div>
              <h3 class="item-title">{{ item.targetTitle || '未命名计划' }}</h3>
              <div class="item-footer">
                <span class="collect-time">{{ new Date(item.createTime).toLocaleDateString() }} 收藏</span>
                <el-button link type="danger" @click="handleUncollect(item)">取消收藏</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的训练计划" />
        </el-tab-pane>
        
        <el-tab-pane label="健康知识" name="NEWS">
          <el-empty description="收藏知识功能开发中..." />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('PLAN')
const collectedPlans = ref<any[]>([])
const loading = ref(false)

const fetchCollections = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/interaction/collections?type=PLAN')
    collectedPlans.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleUncollect = async (item: any) => {
  try {
    await request.delete('/interaction/collect', {
      params: {
        targetId: item.targetId,
        targetType: activeTab.value
      }
    })
    ElMessage.success('已取消收藏')
    fetchCollections()
  } catch (e) {}
}

onMounted(fetchCollections)
</script>

<style scoped>
.collections { padding: 100px 24px 120px; }
.page-header { margin-bottom: 40px; }
.page-header h1 { font-size: 32px; font-weight: 800; color: #1E293B; margin-bottom: 8px; }
.page-header p { color: #64748B; font-size: 16px; }

.collection-tabs :deep(.el-tabs__header) { margin-bottom: 32px; }
.collection-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 600; padding: 0 32px; height: 50px; line-height: 50px; }

.collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}
.collection-card {
  padding: 24px;
  position: relative;
  transition: transform 0.3s;
}
.collection-card:hover { transform: translateY(-5px); }
.card-type-tag { font-size: 12px; color: var(--el-color-primary); background: rgba(64, 158, 255, 0.1); padding: 4px 10px; border-radius: 4px; display: inline-block; margin-bottom: 16px; font-weight: 600; }
.item-title { font-size: 18px; font-weight: 700; color: #1E293B; margin-bottom: 20px; }
.item-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #F1F5F9; padding-top: 16px; }
.collect-time { font-size: 12px; color: #94A3B8; }
</style>
