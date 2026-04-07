<template>
  <div class="health-services-page admin-page-fade">
    <div class="page-header">
      <div class="ph-left">
        <h2 class="ph-title">健康服务管理</h2>
        <p class="ph-desc">统一维护聊天服务卡片、提示词、接口配置和默认模型切换。</p>
      </div>
      <div class="ph-right">
        <el-radio-group v-model="activeTab" size="large">
          <el-radio-button
            v-for="tab in tabOptions"
            :key="tab.value"
            :label="tab.value"
          >
            {{ tab.label }}
          </el-radio-button>
        </el-radio-group>
        <el-button type="primary" round @click="openCreateDrawer">
          {{ activeTab === 'services' ? '新增服务' : '新增接口' }}
        </el-button>
      </div>
    </div>

    <div class="table-card premium-card" v-loading="loading">
      <el-table
        v-if="activeTab === 'services'"
        :data="filteredServices"
        style="width: 100%; cursor: pointer;"
        @row-click="editService"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="服务信息" min-width="280">
          <template #default="{ row }">
            <div class="service-cell">
              <span class="service-name">{{ row.name }}</span>
              <span class="service-meta">{{ row.serviceKey }} · {{ row.tagLabel || '未设置标签' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="styleLabel" label="交流风格" min-width="160" />
        <el-table-column prop="providerName" label="绑定接口" min-width="180" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="light">
              {{ row.enabled ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="editService(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" link @click.stop="toggleService(row)">
              {{ row.enabled ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table
        v-else
        :data="filteredProviders"
        style="width: 100%; cursor: pointer;"
        @row-click="editProvider"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="接口配置" min-width="260">
          <template #default="{ row }">
            <div class="service-cell">
              <span class="service-name">{{ row.name }}</span>
              <span class="service-meta">{{ row.providerType }} · {{ row.model }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="Base URL" min-width="240" show-overflow-tooltip />
        <el-table-column label="默认" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : 'info'" effect="plain">
              {{ row.isDefault ? '默认' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="light">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="editProvider(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" link @click.stop="toggleProvider(row)">
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
            <el-divider direction="vertical" />
            <el-button size="small" type="success" link :disabled="row.isDefault" @click.stop="setDefaultProvider(row)">
              设为默认
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer
      v-model="serviceDrawerVisible"
      :title="serviceForm.id ? '编辑健康服务' : '新增健康服务'"
      size="720px"
      destroy-on-close
    >
      <div class="drawer-body">
        <el-form :model="serviceForm" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="服务名称">
                <el-input v-model="serviceForm.name" placeholder="例如：健身教练" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务标识">
                <el-input v-model="serviceForm.serviceKey" placeholder="例如：fitness_coach" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="标签">
                <el-input v-model="serviceForm.tagLabel" placeholder="例如：训练指导" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交流风格">
                <el-input v-model="serviceForm.styleLabel" placeholder="例如：结构化教练" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="排序">
                <el-input-number v-model="serviceForm.sortOrder" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="默认意图">
                <el-select v-model="serviceForm.defaultIntent" style="width: 100%">
                  <el-option label="普通问答" value="chat" />
                  <el-option label="训练计划" value="training_plan" />
                  <el-option label="训练课程" value="course" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="绑定接口">
                <el-select v-model="serviceForm.apiConfigId" style="width: 100%">
                  <el-option
                    v-for="provider in providers"
                    :key="provider.id"
                    :label="provider.name"
                    :value="provider.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="简介">
            <el-input v-model="serviceForm.description" type="textarea" :rows="3" placeholder="展示在前台服务卡和聊天顶部的说明。" />
          </el-form-item>

          <el-form-item label="系统提示词">
            <el-input
              v-model="serviceForm.systemPrompt"
              type="textarea"
              :rows="7"
              placeholder="定义该服务的回答边界、风格、风险提示和生成偏向。"
            />
          </el-form-item>

          <el-form-item>
            <el-switch v-model="serviceForm.enabled" active-text="上架" inactive-text="下架" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="serviceDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveService">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer
      v-model="providerDrawerVisible"
      :title="providerForm.id ? '编辑接口配置' : '新增接口配置'"
      size="720px"
      destroy-on-close
    >
      <div class="drawer-body">
        <el-form :model="providerForm" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="配置名称">
                <el-input v-model="providerForm.name" placeholder="例如：OpenRouter Free" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Provider 类型">
                <el-input v-model="providerForm.providerType" placeholder="例如：openrouter / openai-compatible" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Base URL">
            <el-input v-model="providerForm.baseUrl" placeholder="https://openrouter.ai/api/v1" />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模型名称">
                <el-input v-model="providerForm.model" placeholder="例如：google/gemma-3-27b-it:free" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="API Key">
                <el-input v-model="providerForm.apiKey" type="password" show-password placeholder="输入 API Key" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="额外请求头 JSON">
            <el-input
              v-model="providerForm.extraHeadersJson"
              type="textarea"
              :rows="4"
              placeholder='例如：{"HTTP-Referer":"https://your-app.com"}'
            />
          </el-form-item>

          <div class="switch-row">
            <el-switch v-model="providerForm.enabled" active-text="启用" inactive-text="停用" />
            <el-switch v-model="providerForm.isDefault" active-text="默认接口" inactive-text="普通接口" />
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="providerDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProvider">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

type ServiceConfig = {
  id: number | null
  serviceKey: string
  name: string
  description: string
  tagLabel: string
  styleLabel: string
  systemPrompt: string
  sortOrder: number
  enabled: boolean
  apiConfigId: number | null
  defaultIntent: string
  providerName?: string
}

type ProviderConfig = {
  id: number | null
  name: string
  providerType: string
  baseUrl: string
  apiKey: string
  model: string
  extraHeadersJson: string
  enabled: boolean
  isDefault: boolean
}

const activeTab = ref<'services' | 'providers'>('services')
const loading = ref(false)
const saving = ref(false)
const serviceDrawerVisible = ref(false)
const providerDrawerVisible = ref(false)

const services = ref<ServiceConfig[]>([])
const providers = ref<ProviderConfig[]>([])

const tabOptions = [
  { label: '服务配置', value: 'services' },
  { label: 'API 配置', value: 'providers' }
]

const createEmptyService = (): ServiceConfig => ({
  id: null,
  serviceKey: '',
  name: '',
  description: '',
  tagLabel: '',
  styleLabel: '',
  systemPrompt: '',
  sortOrder: 0,
  enabled: true,
  apiConfigId: null,
  defaultIntent: 'chat'
})

const createEmptyProvider = (): ProviderConfig => ({
  id: null,
  name: '',
  providerType: 'openai-compatible',
  baseUrl: '',
  apiKey: '',
  model: '',
  extraHeadersJson: '',
  enabled: true,
  isDefault: false
})

const serviceForm = reactive<ServiceConfig>(createEmptyService())
const providerForm = reactive<ProviderConfig>(createEmptyProvider())

const filteredServices = computed(() => [...services.value].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)))
const filteredProviders = computed(() => providers.value)

const resetServiceForm = () => Object.assign(serviceForm, createEmptyService())
const resetProviderForm = () => Object.assign(providerForm, createEmptyProvider())

const fetchServices = async () => {
  const res: any = await request.get('/admin/ai-services')
  services.value = res.data || []
}

const fetchProviders = async () => {
  const res: any = await request.get('/admin/ai-providers')
  providers.value = res.data || []
}

const fetchAll = async () => {
  loading.value = true
  try {
    await Promise.all([fetchServices(), fetchProviders()])
  } finally {
    loading.value = false
  }
}

const openCreateDrawer = () => {
  if (activeTab.value === 'services') {
    resetServiceForm()
    if (!serviceForm.apiConfigId && providers.value[0]?.id) {
      serviceForm.apiConfigId = providers.value[0].id
    }
    serviceDrawerVisible.value = true
    return
  }

  resetProviderForm()
  providerDrawerVisible.value = true
}

const editService = (row: ServiceConfig) => {
  Object.assign(serviceForm, {
    ...createEmptyService(),
    ...row
  })
  serviceDrawerVisible.value = true
}

const editProvider = (row: ProviderConfig) => {
  Object.assign(providerForm, {
    ...createEmptyProvider(),
    ...row
  })
  providerDrawerVisible.value = true
}

const saveService = async () => {
  if (!serviceForm.serviceKey || !serviceForm.name) {
    ElMessage.warning('请先填写服务名称和服务标识')
    return
  }

  saving.value = true
  try {
    await request.post('/admin/ai-services/save', serviceForm)
    ElMessage.success('健康服务已保存')
    serviceDrawerVisible.value = false
    await fetchServices()
  } finally {
    saving.value = false
  }
}

const saveProvider = async () => {
  if (!providerForm.name || !providerForm.providerType || !providerForm.model) {
    ElMessage.warning('请先填写接口名称、类型和模型')
    return
  }

  saving.value = true
  try {
    const expectDefault = providerForm.isDefault
    await request.post('/admin/ai-providers/save', providerForm)
    await fetchProviders()

    if (expectDefault) {
      const matched = providers.value.find(item =>
        item.name === providerForm.name &&
        item.model === providerForm.model &&
        item.baseUrl === providerForm.baseUrl
      )
      if (matched?.id) {
        await request.post(`/admin/ai-providers/${matched.id}/default`)
        await fetchProviders()
      }
    }

    ElMessage.success('接口配置已保存')
    providerDrawerVisible.value = false
  } finally {
    saving.value = false
  }
}

const toggleService = async (row: ServiceConfig) => {
  await request.post(`/admin/ai-services/${row.id}/toggle`)
  ElMessage.success(row.enabled ? '服务已下架' : '服务已上架')
  await fetchServices()
}

const toggleProvider = async (row: ProviderConfig) => {
  await request.post(`/admin/ai-providers/${row.id}/toggle`)
  ElMessage.success(row.enabled ? '接口已停用' : '接口已启用')
  await fetchProviders()
}

const setDefaultProvider = async (row: ProviderConfig) => {
  await request.post(`/admin/ai-providers/${row.id}/default`)
  ElMessage.success('默认接口已切换')
  await fetchProviders()
}

onMounted(fetchAll)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.ph-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ph-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--text-main);
}

.ph-desc {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}

.ph-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.service-name {
  font-weight: 700;
  color: var(--text-main);
}

.service-meta {
  font-size: 12px;
  color: var(--text-light);
}

.drawer-body {
  padding: 24px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
}

.switch-row {
  display: flex;
  gap: 24px;
  align-items: center;
}

@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .ph-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
