<template>
  <div class="chat-page">
    <div class="chat-shell">
      <header class="chat-header">
        <el-button class="back-btn" circle :icon="ArrowLeft" @click="goBack" />
        <div class="chat-title">{{ activeService?.name || '健康智能助手' }}</div>
        <div class="chat-spacer"></div>
      </header>

      <div ref="streamRef" class="chat-stream">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="{ user: message.role === 'user' }"
        >
          <div class="avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
          <div class="message-main">
            <div class="message-meta">
              <span>{{ message.role === 'user' ? '我' : (activeService?.name || '智能助手') }}</span>
              <span>{{ message.time }}</span>
            </div>
            <div class="message-content">{{ message.content }}</div>

            <div v-if="message.draftPayload" class="draft-card">
              <div class="draft-top">
                <div>
                  <div class="draft-type">{{ message.intentType === 'training_plan' ? '训练计划草稿' : '训练课程草稿' }}</div>
                  <div class="draft-title">{{ message.draftPayload.title }}</div>
                </div>
                <el-tag type="success" effect="plain">确认后加入个人库</el-tag>
              </div>

              <div class="draft-grid">
                <div class="draft-item">
                  <span class="label">描述</span>
                  <span>{{ message.draftPayload.description || '暂无描述' }}</span>
                </div>
                <div class="draft-item">
                  <span class="label">分类</span>
                  <span>{{ message.draftPayload.category || activeService?.tagLabel || '综合训练' }}</span>
                </div>
                <div class="draft-item">
                  <span class="label">{{ message.intentType === 'training_plan' ? '周期' : '时长' }}</span>
                  <span>{{ message.intentType === 'training_plan' ? (message.draftPayload.duration || '4周') : `${message.draftPayload.durationMinutes || 30} 分钟` }}</span>
                </div>
                <div class="draft-item">
                  <span class="label">适用人群</span>
                  <span>{{ message.draftPayload.audience || '普通用户' }}</span>
                </div>
                <div v-if="message.intentType === 'course'" class="draft-item">
                  <span class="label">难度</span>
                  <span>{{ message.draftPayload.difficulty || '初级' }}</span>
                </div>
              </div>

              <div class="draft-actions">
                <div class="draft-actions-title">动作安排</div>
                <div
                  v-for="(action, index) in normalizedActions(message)"
                  :key="`${message.id}-${index}`"
                  class="draft-action-row"
                >
                  <span>{{ action.name }}</span>
                  <span>{{ action.sets }}</span>
                </div>
              </div>

              <div class="draft-footer">
                <el-button type="primary" :loading="savingDraftId === message.id" @click="confirmDraft(message)">
                  加入个人库
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer class="composer">
        <div class="composer-box">
          <textarea
            v-model="inputText"
            class="composer-input"
            rows="1"
            placeholder="询问健康问题，或直接要求生成训练计划 / 训练课程"
            @keydown.enter.exact.prevent="send"
            @keydown.enter.shift.exact.stop
          />
          <div class="composer-meta">
            <div class="meta-left">
              <span>{{ activeService?.styleLabel || activeService?.tagLabel || '对话模式' }}</span>
            </div>
            <el-button
              type="primary"
              class="send-btn"
              :loading="sending"
              :disabled="!inputText.trim()"
              @click="send"
            >
              发送
            </el-button>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

type ServiceConfig = {
  id: number
  serviceKey: string
  name: string
  description: string
  tagLabel: string
  styleLabel: string
}

type DraftAction = {
  name: string
  sets: string
}

type ChatMessage = {
  id: number
  role: 'user' | 'assistant'
  content: string
  time: string
  intentType?: string
  draftPayload?: any
}

const router = useRouter()
const route = useRoute()

const services = ref<ServiceConfig[]>([])
const activeServiceKey = ref('')
const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const sending = ref(false)
const savingDraftId = ref<number | null>(null)
const streamRef = ref<HTMLElement | null>(null)

const formatTime = () => new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

const activeService = computed(
  () => services.value.find(item => item.serviceKey === activeServiceKey.value) || services.value[0] || null
)

const normalizedActions = (message: ChatMessage): DraftAction[] => {
  const payload = message.draftPayload || {}
  const raw = Array.isArray(payload.actions)
    ? payload.actions
    : Array.isArray(payload.actionsJson)
      ? payload.actionsJson
      : []

  return raw.map((item: any) => ({
    name: item?.name || '训练动作',
    sets: item?.sets || '按需调整'
  }))
}

const scrollToBottom = async () => {
  await nextTick()
  const el = streamRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const loadServices = async () => {
  const res: any = await request.get('/ai/services')
  services.value = res.data || []

  const queryServiceKey = String(route.query.serviceKey || '')
  activeServiceKey.value = queryServiceKey && services.value.some(item => item.serviceKey === queryServiceKey)
    ? queryServiceKey
    : (services.value[0]?.serviceKey || '')

  messages.value = [
    {
      id: 1,
      role: 'assistant',
      content: activeService.value?.description || '描述你的健康目标、训练问题或恢复需求，我会结合当前服务方向进行交流。',
      time: formatTime()
    }
  ]
}

const goBack = () => {
  router.push('/app/explore?tab=services')
}

const send = async () => {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: text,
    time: formatTime()
  })

  inputText.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const res: any = await request.post('/ai/chat', {
      message: text,
      serviceKey: activeServiceKey.value || undefined
    })

    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: res.data?.reply || '已收到你的问题。',
      time: formatTime(),
      intentType: res.data?.intentType,
      draftPayload: res.data?.draftPayload || null
    })
    await scrollToBottom()
  } catch (error) {
    ElMessage.error('AI 请求失败')
  } finally {
    sending.value = false
  }
}

const confirmDraft = async (message: ChatMessage) => {
  if (!message.draftPayload || !message.intentType) return
  savingDraftId.value = message.id

  try {
    if (message.intentType === 'training_plan') {
      await request.post('/training/save', {
        title: message.draftPayload.title,
        description: message.draftPayload.description,
        category: message.draftPayload.category,
        duration: message.draftPayload.duration,
        audience: message.draftPayload.audience,
        actions: JSON.stringify(normalizedActions(message)),
        status: 'PLANNING',
        isPublic: false
      })
    } else if (message.intentType === 'course') {
      await request.post('/course/save', {
        title: message.draftPayload.title,
        description: message.draftPayload.description,
        category: message.draftPayload.category,
        difficulty: message.draftPayload.difficulty,
        durationMinutes: Number(message.draftPayload.durationMinutes || 30),
        audience: message.draftPayload.audience,
        actionsJson: JSON.stringify(normalizedActions(message)),
        isPublic: false
      })
    }

    ElMessage.success('已加入个人库')
    router.push('/app/training?tab=created')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingDraftId.value = null
  }
}

onMounted(loadServices)
</script>

<style scoped>
.chat-page {
  min-height: 100%;
  background: linear-gradient(180deg, #fafaf7 0%, #ffffff 38%, #fcfcfb 100%);
}

.chat-shell {
  max-width: 920px;
  margin: 0 auto;
  min-height: calc(100vh - 96px);
  display: grid;
  grid-template-rows: auto 1fr auto;
  padding: 22px 18px 20px;
}

.chat-header {
  display: grid;
  grid-template-columns: 44px 1fr 44px;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.back-btn {
  border-color: #d1d5db;
}

.chat-title {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.chat-spacer {
  width: 44px;
  height: 44px;
}

.chat-stream {
  overflow-y: auto;
  padding: 12px 0 20px;
}

.message-row {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 14px;
  margin-bottom: 28px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: #111827;
  color: #fff;
}

.message-row.user .avatar {
  background: #dbeafe;
  color: #1d4ed8;
}

.message-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 8px;
}

.message-meta span:first-child {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.message-content {
  white-space: pre-wrap;
  color: #1f2937;
  line-height: 1.85;
}

.draft-card {
  margin-top: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 22px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.84);
}

.draft-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.draft-type {
  font-size: 12px;
  color: #2563eb;
  font-weight: 700;
  margin-bottom: 4px;
}

.draft-title {
  font-size: 18px;
  font-weight: 800;
  color: #111827;
}

.draft-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.draft-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 16px;
  background: #f8fafc;
  color: #334155;
  font-size: 13px;
}

.draft-item .label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94a3b8;
}

.draft-actions {
  margin-top: 18px;
}

.draft-actions-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.draft-action-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e5e7eb;
  margin-bottom: 8px;
  color: #334155;
}

.draft-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.composer {
  position: sticky;
  bottom: 0;
  padding-top: 10px;
  background: linear-gradient(180deg, rgba(252, 252, 251, 0) 0%, rgba(252, 252, 251, 0.94) 22%, rgba(252, 252, 251, 1) 100%);
}

.composer-box {
  width: min(760px, 100%);
  margin: 0 auto;
  border: 1px solid #d1d5db;
  border-radius: 26px;
  padding: 12px 16px 12px;
  background: rgba(255, 255, 255, 0.82);
}

.composer-input {
  width: 100%;
  border: none;
  resize: none;
  outline: none;
  background: transparent;
  font: inherit;
  color: #111827;
  line-height: 1.7;
  min-height: 28px;
}

.composer-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

.meta-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 12px;
}

.send-btn {
  border-radius: 999px;
  padding: 0 18px;
}

@media (max-width: 768px) {
  .chat-shell {
    padding: 16px 12px;
  }

  .message-row {
    grid-template-columns: 36px minmax(0, 1fr);
    gap: 10px;
  }

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 12px;
  }

  .composer-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
