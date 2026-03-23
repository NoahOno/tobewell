<template>
  <div class="user-admin admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">全站用户管理</h2>
        <p class="ph-desc">监控平台注册动态，管理用户权限及账户状态</p>
      </div>
      <div class="ph-right">
        <el-tag effect="light" type="success" round>实时数据</el-tag>
      </div>
    </div>
    
    <div class="table-container premium-card" v-loading="loading">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column label="用户信息" min-width="250">
          <template #default="sc">
            <div class="user-cell">
              <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <div class="u-info">
                <span class="u-name">{{ sc.row.nickname || sc.row.username }}</span>
                <span class="u-username">@{{ sc.row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="权限角色" width="160">
          <template #default="scope">
            <el-select v-model="scope.row.role" size="small" class="role-select" @change="handleUpdateUser(scope.row)">
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账户状态" width="160">
          <template #default="scope">
            <div class="status-cell">
               <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleUpdateUser(scope.row)"
              />
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="status-tag">
                {{ scope.row.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册日期" width="200">
          <template #default="scope">
            <span class="date-text">{{ new Date(scope.row.createTime).toLocaleDateString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="管理操作" width="120" align="right">
          <template #default="scope">
            <el-popconfirm title="确定删除该用户并清理其所有数据吗？此操作不可撤销。" @confirm="handleDeleteUser(scope.row.id)">
              <template #reference>
                <el-button type="danger" size="small" link :disabled="scope.row.role === 'ADMIN'">注销账户</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const users = ref<any[]>([])
const loading = ref(false)

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/users')
    users.value = res.data
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleUpdateUser = async (user: any) => {
  try {
    await request.post('/admin/user/update', user)
     ElMessage.success('配置已更新')
  } catch (e) {
    ElMessage.error('更新受阻')
    fetchUsers()
  }
}

const handleDeleteUser = async (id: number) => {
  try {
    await request.delete(`/admin/user/${id}`)
    ElMessage.success('用户档案已移除')
    fetchUsers()
  } catch (e) {
    ElMessage.error('删除操作失败')
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.user-admin {
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
  margin: 0;
}

.table-container {
  padding: 0;
  overflow: hidden;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.u-info {
  display: flex;
  flex-direction: column;
}

.u-name {
  font-weight: 700;
  color: var(--text-main);
  font-size: 14px;
}

.u-username {
  font-size: 12px;
  color: var(--text-light);
}

.role-select {
  width: 120px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-tag {
  font-weight: 600;
}

.date-text {
  color: var(--text-muted);
  font-size: 13px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
