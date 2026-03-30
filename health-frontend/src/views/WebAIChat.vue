<template>
  <div class="webai-page">
    <div class="webai-header">
      <div class="webai-header-left">
        <el-button link type="primary" @click="goBack">返回</el-button>
        <div class="webai-title">AI 健康助手</div>
      </div>
    </div>

    <div class="webai-body">
      <div class="webai-messages">
        <el-scrollbar height="100%">
          <div class="msg-list">
            <div v-if="introVisible" class="msg-row">
              <div class="msg-bubble">
                <div class="msg-meta">
                  <span class="msg-role">助手</span>
                  <span class="msg-time">{{ formatTime() }}</span>
                </div>
                <div class="msg-content" style="white-space: pre-wrap">{{ introText }}</div>
              </div>
            </div>

            <div
              v-for="m in messages"
              :key="m.id"
              class="msg-row"
              :class="{ 'is-user': m.role === 'user' }"
            >
              <div class="msg-bubble">
                <div class="msg-meta">
                  <span class="msg-role">{{ m.role === 'user' ? '你' : '助手' }}</span>
                  <span class="msg-time">{{ m.time }}</span>
                </div>
                <div class="msg-content" style="white-space: pre-wrap">{{ m.content }}</div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <div class="webai-input">
        <div class="input-shell">
          <el-input
            v-model="inputText"
            type="textarea"
            placeholder="输入你的问题..."
            :rows="3"
            resize="none"
            @keyup.enter="onEnter"
          />
          <div class="webai-input-actions">
            <el-button type="primary" :loading="sending" :disabled="!inputText.trim()" @click="send">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

type Preset = {
  key: string
  title: string
  // Used for request body to backend later.
  presetPrompt: string
  // Fallback simulation template.
  fallbackHeader: string
  defaultStyleLabel: string
}

const servicePresets: Preset[] = [
  {
    key: 'mental_counseling',
    title: '心理咨询（情绪疏导）',
    presetPrompt:
      '你是一名耐心的心理咨询助手。用共情、澄清问题、给出可执行的小建议。语气温和、不过度诊断。必要时提醒寻求专业帮助。',
    fallbackHeader: '（模拟）',
    defaultStyleLabel: '温和共情风'
  },
  {
    key: 'fitness_coach',
    title: '健身带教（训练指导）',
    presetPrompt:
      '你是一名专业健身教练。根据用户目标与情况，给出安全的训练建议、动作要点与替代方案。语气坚定、结构化。',
    fallbackHeader: '（模拟）',
    defaultStyleLabel: '科学教练风'
  },
  {
    key: 'rehab_coach',
    title: '康复评估（循序恢复）',
    presetPrompt:
      '你是一名康复训练助手。强调安全边界，给出渐进式恢复建议、注意事项与何时就医。',
    fallbackHeader: '（模拟）',
    defaultStyleLabel: '循序渐进风'
  },
  {
    key: 'nutrition_coach',
    title: '营养指导（饮食策略）',
    presetPrompt:
      '你是一名营养教练。根据用户饮食偏好与目标，提出可落地的饮食策略，并给出替换建议。保持理性、不夸张。',
    fallbackHeader: '（模拟）',
    defaultStyleLabel: '理性规划风'
  }
]

const selectedPresetKey = ref<string>((route.query.preset as string) || servicePresets[0].key)
const selectedStyleLabel = ref<string>((route.query.style as string) || '')
const selectedPreset = computed(() => servicePresets.find(p => p.key === selectedPresetKey.value) || servicePresets[0])

type ChatMsg = { id: number; role: 'user' | 'assistant'; content: string; time: string }
const messages = ref<ChatMsg[]>([])
const inputText = ref<string>('')
const sending = ref(false)

const presetPromptForRequest = computed(() => selectedPreset.value.presetPrompt)

const formatTime = () => {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const goBack = () => {
  // Prefer returning to services module entry.
  router.push('/app/explore?tab=services')
}

const introVisible = computed(() => messages.value.every(m => m.role !== 'user'))
const introText = computed(() => {
  const label = selectedStyleLabel.value || selectedPreset.value.defaultStyleLabel
  return (
    `欢迎来到「${selectedPreset.value.title}」。当前风格：${label}。\n` +
    `把你的情况说清楚就行：目标、频率、限制（伤痛/时间/器械/饮食偏好等）。\n` +
    `我会给你可执行的下一步。`
  )
})

const simulateAssistant = (presetKey: string, userText: string) => {
  const header = selectedPreset.value.fallbackHeader
  if (presetKey === 'mental_counseling') {
    return (
      `${header}我先理解你的感受：你希望在压力/情绪上得到更稳定的支持。\n` +
      `为了更准确地帮你，我想问两个小问题：\n` +
      `1）最近让你最难受的是哪一件事？\n` +
      `2）当情绪上来时，你的身体反应是什么（紧张/失眠/胃不舒服等）？\n\n` +
      `你可以先做一个“2分钟落地”：` +
      `深呼吸 6 秒吸气 + 6 秒呼气，共 6 组；然后把此刻的想法用一句话写下来。`
    )
  }
  if (presetKey === 'fitness_coach') {
    return (
      `${header}好的，我可以按「安全 + 可执行」来给你训练方案。\n` +
      `你先补充：1）目标（减脂/增肌/体态/体能） 2）每周可训练几天 3）有没有伤病或疼痛点。\n\n` +
      `基于你描述的内容（模拟），我建议你先从 2-3 个基础动作开始：` +
      `深蹲/臀桥 + 推举 + 划船/核心收紧。` +
      `如果某个动作引发疼痛，请立刻停止并换成替代动作。`
    )
  }
  if (presetKey === 'rehab_coach') {
    return (
      `${header}我会以“循序渐进 + 安全边界”为原则。\n` +
      `请你描述：疼痛部位（具体位置）/疼痛程度 0-10 / 什么时候最疼。\n\n` +
      `模拟建议：先做不引发明显疼痛的活动度训练（低强度）、再逐步加力量。` +
      `如果持续加重或出现麻木无力，请尽快就医评估。`
    )
  }
  return (
    `${header}营养方面我会按“可坚持 + 简单替换”来帮你。\n` +
    `你可以告诉我：身高体重/目标（减脂增肌等）/日常饮食习惯/是否有忌口。\n\n` +
    `模拟建议：先把每餐的蛋白质来源固定下来（鸡蛋/鱼/豆制品/瘦肉），` +
    `再用蔬菜和主食比例做微调；同时把饮水与睡眠一起纳入。`
  )
}

const send = async () => {
  const text = inputText.value.trim()
  if (!text) return

  inputText.value = ''

  const userMsg: ChatMsg = { id: Date.now(), role: 'user', content: text, time: formatTime() }
  messages.value.push(userMsg)

  sending.value = true
  try {
    // Backend integration later:
    // POST /ai/chat with presetKey + presetPrompt + user message.
    const res: any = await request.post('/ai/chat', {
      message: text,
      presetKey: selectedPresetKey.value,
      presetPrompt: presetPromptForRequest.value,
      style: selectedStyleLabel.value || selectedPreset.value.defaultStyleLabel
    })

    // Try to extract the assistant reply from common response shapes.
    const reply =
      (typeof res?.data === 'string' ? res.data : null) ||
      (typeof res?.data?.reply === 'string' ? res.data.reply : null) ||
      (typeof res?.data?.message === 'string' ? res.data.message : null) ||
      (typeof res?.data?.content === 'string' ? res.data.content : null)

    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: reply || '收到你的信息了。后端 AI 接入后我将返回更具体的建议。',
      time: formatTime()
    })
  } catch (e) {
    // Graceful fallback so the UI works before AI backend is ready.
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: simulateAssistant(selectedPresetKey.value, text),
      time: formatTime()
    })
    ElMessage.warning('AI 接入尚未完成：已使用模拟回复（请稍后对接后端）。')
  } finally {
    sending.value = false
  }
}

const onEnter = (e: KeyboardEvent) => {
  // Keep enter behavior consistent: Ctrl+Enter sends.
  if (e.ctrlKey || e.metaKey) {
    send()
  }
}

onMounted(() => {
  if (!selectedStyleLabel.value) selectedStyleLabel.value = selectedPreset.value.defaultStyleLabel
})
</script>

<style scoped>
.webai-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.webai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid #F1F5F9;
}

.webai-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.webai-title {
  font-weight: 900;
  color: #0F172A;
  font-size: 18px;
}

.webai-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  background:
    radial-gradient(at 0% 0%, rgba(56, 189, 248, 0.12) 0px, transparent 55%),
    radial-gradient(at 100% 0%, rgba(251, 146, 60, 0.1) 0px, transparent 60%),
    radial-gradient(at 100% 100%, rgba(74, 222, 128, 0.12) 0px, transparent 55%),
    #F8FAFF;
  flex-direction: column;
}

.webai-messages {
  flex: 1;
  overflow: hidden;
  padding: 22px 20px;
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-row {
  display: flex;
}

.msg-row.is-user {
  justify-content: flex-end;
}

.msg-bubble {
  max-width: 720px;
  background: white;
  border: 1px solid #F1F5F9;
  border-radius: 14px;
  padding: 12px 14px;
}

.msg-row.is-user .msg-bubble {
  background: #EFF6FF;
  border-color: #BFDBFE;
}

.msg-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.msg-role {
  font-weight: 800;
  color: #0F172A;
  font-size: 12px;
}

.msg-time {
  font-size: 12px;
  color: #94A3B8;
}

.msg-content {
  color: #334155;
  line-height: 1.8;
  font-size: 14px;
}

.webai-input {
  padding: 16px 20px 22px;
  display: flex;
  justify-content: center;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
  border-top: 1px solid rgba(226, 232, 240, 0.7);
}

.input-shell {
  width: min(860px, 100%);
  background: white;
  border: 1px solid rgba(226, 232, 240, 0.7);
  border-radius: 18px;
  padding: 12px 12px;
  box-shadow: 0 18px 42px -28px rgba(15, 23, 42, 0.28);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-shell :deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  padding: 10px 12px !important;
}

.webai-input-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
}
</style>

