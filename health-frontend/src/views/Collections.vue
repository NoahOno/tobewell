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
        <el-tab-pane label="社区帖子" name="POST">
          <el-empty description="收藏帖子功能开发中..." />
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

const activeTab = ref('POST')
const loading = ref(false)

const fetchCollections = async () => {
  // Currently just a placeholder for future community content
  loading.value = true
  try {
    // const res: any = await request.get('/interaction/collections?type=' + activeTab.value)
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
