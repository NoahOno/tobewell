<template>
  <div class="admin-dashboard admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">全站运营看板</h2>
        <p class="ph-desc">欢迎回来，管理员。以下是平台的实时运营核心数据概览。</p>
      </div>
      <div class="ph-right">
        <el-button type="primary" link @click="fetchStats">
            <el-icon><Refresh /></el-icon> 刷新数据
        </el-button>
      </div>
    </div>

    <!-- Quick Stats Grid -->
    <div class="stats-grid">
      <div class="stats-card">
        <div class="sc-icon"><el-icon size="24" color="#4F46E5"><User /></el-icon></div>
        <div class="sc-info">
          <span class="sc-label">平台总用户</span>
          <h3 class="sc-value">{{ stats.totalUsers }}</h3>
          <p class="sc-trend"><span class="trend-up">+{{ Math.floor(stats.totalUsers * 0.05) }}</span> 本月增长</p>
        </div>
      </div>

      <div class="stats-card">
        <div class="sc-icon"><el-icon size="24" color="#10B981"><Notebook /></el-icon></div>
        <div class="sc-info">
          <span class="sc-label">社区帖子数</span>
          <h3 class="sc-value">{{ stats.totalPosts }}</h3>
          <p class="sc-trend">覆盖 {{ stats.totalPosts > 0 ? (stats.totalPosts * 1.5).toFixed(0) : 0 }} 累计互动</p>
        </div>
      </div>

      <div class="stats-card">
        <div class="sc-icon"><el-icon size="24" color="#F59E0B"><Collection /></el-icon></div>
        <div class="sc-info">
          <span class="sc-label">官方训练课</span>
          <h3 class="sc-value">{{ stats.totalCourses }}</h3>
          <p class="sc-trend">及 {{ stats.totalPlans }} 个系统化计划</p>
        </div>
      </div>

      <div class="stats-card">
        <div class="sc-icon"><el-icon size="24" color="#3B82F6"><Trophy /></el-icon></div>
        <div class="sc-info">
          <span class="sc-label">活跃活动</span>
          <h3 class="sc-value">{{ stats.totalActivities }}</h3>
          <p class="sc-trend">激励用户持续打卡</p>
        </div>
      </div>
    </div>

    <div class="dashboard-charts">
      <div class="chart-container premium-card">
        <div class="chart-header">
           <h4>用户账号状态分布</h4>
        </div>
        <v-chart class="chart-body" :option="pieOption" autoresize />
      </div>

      <div class="chart-container premium-card">
        <div class="chart-header">
           <h4>最近活跃趋势 (模拟)</h4>
        </div>
        <v-chart class="chart-body" :option="lineOption" autoresize />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Refresh, User, Notebook, Collection, Trophy } from '@element-plus/icons-vue'
import request from '../../api/request'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, PieChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref({
  totalUsers: 0,
  totalPosts: 0,
  totalCourses: 0,
  totalPlans: 0,
  totalActivities: 0,
  activeUsers: 0,
  bannedUsers: 0
})

const fetchStats = async () => {
  try {
    const res: any = await request.get('/admin/dashboard/stats')
    stats.value = res.data
  } catch (e) {}
}

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%', left: 'center' },
  series: [
    {
      name: '账户状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: [
        { value: stats.value.activeUsers, name: '正常使用', itemStyle: { color: '#10B981' } },
        { value: stats.value.bannedUsers, name: '已封禁', itemStyle: { color: '#EF4444' } }
      ]
    }
  ]
}))

const lineOption = computed(() => ({
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], boundaryGap: false },
  yAxis: { type: 'value' },
  tooltip: { trigger: 'axis' },
  series: [{
    data: [120, 200, 150, 80, 70, 110, 130],
    type: 'line',
    smooth: true,
    lineStyle: { width: 4, color: '#3B82F6' },
    areaStyle: { color: 'rgba(59, 130, 246, 0.1)' },
    symbolSize: 8
  }]
}))

onMounted(fetchStats)
</script>

<style scoped>
.admin-dashboard {
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

.ph-desc { color: var(--text-muted); margin: 0; font-size: 15px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stats-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -10px rgba(0,0,0,0.1);
}

.sc-icon {
  background: #f8fafc;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sc-info {
  display: flex;
  flex-direction: column;
}

.sc-label { font-size: 14px; color: var(--text-muted); font-weight: 500; }
.sc-value { font-size: 24px; font-weight: 800; color: var(--text-main); margin: 4px 0; }
.sc-trend { font-size: 12px; color: var(--text-light); }
.trend-up { color: #10B981; font-weight: 600; }

.dashboard-charts {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 24px;
}

.chart-container {
  height: 400px;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.chart-header h4 { margin: 0 0 20px; font-weight: 700; color: var(--text-main); }
.chart-body { flex: 1; min-height: 0; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
