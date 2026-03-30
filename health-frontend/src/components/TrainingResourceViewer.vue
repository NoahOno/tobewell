<template>
  <!-- Unified Training Resource Viewer — used by both User and Admin side -->
  <div class="trv-container">

    <!-- ═══ EXERCISE ═══ -->
    <template v-if="type === 'exercise'">
      <div class="trv-hero ex-hero">
        <div class="hero-icon-wrap">
          <el-icon class="hero-icon"><VideoCamera /></el-icon>
        </div>
        <div class="hero-info">
          <div class="hero-title">{{ item.name }}</div>
          <div class="hero-chips">
            <span class="chip chip-blue">{{ item.muscle }}</span>
            <span class="chip chip-purple">{{ item.type }}</span>
            <span class="chip chip-green">{{ item.equipment }}</span>
            <span class="chip" :class="difficultyChipClass(item.difficulty)">{{ item.difficulty }}</span>
          </div>
        </div>
      </div>

      <div class="trv-body">
        <div class="trv-section" v-if="item.instruction">
          <div class="trv-section-label"><el-icon><Document /></el-icon> 动作说明</div>
          <p class="trv-text">{{ item.instruction }}</p>
        </div>

        <div class="trv-two-col">
          <div class="trv-info-block" v-if="item.recommendedSets">
            <div class="info-block-label">推荐组数</div>
            <div class="info-block-value">{{ item.recommendedSets }}</div>
          </div>
          <div class="trv-info-block" v-if="item.difficulty">
            <div class="info-block-label">难度级别</div>
            <div class="info-block-value">{{ item.difficulty }}</div>
          </div>
        </div>

        <div class="trv-section" v-if="parsedErrors.length > 0">
          <div class="trv-section-label"><el-icon><Warning /></el-icon> 常见易错点</div>
          <div class="error-pill-list">
            <span class="error-pill" v-for="(err, i) in parsedErrors" :key="i">{{ err }}</span>
          </div>
        </div>

        <div class="trv-section" v-if="item.videoUrl">
          <div class="trv-section-label"><el-icon><VideoPlay /></el-icon> 演示视频</div>
          <div class="video-stub">
            <a :href="item.videoUrl" target="_blank" class="video-link">
              <el-icon><VideoCameraFilled /></el-icon> 点击观看演示
            </a>
          </div>
        </div>
      </div>
    </template>

    <!-- ═══ PLAN ═══ -->
    <template v-else-if="type === 'plan'">
      <div class="trv-hero plan-hero">
        <div class="hero-cover" v-if="item.coverImage">
          <img :src="item.coverImage" alt="封面" />
        </div>
        <div class="hero-cover plan-cover-placeholder" v-else>
          <el-icon class="cover-icon"><TrophyBase /></el-icon>
        </div>
        <div class="hero-info">
          <div class="hero-title">{{ item.title }}</div>
          <div class="hero-chips">
            <span class="chip chip-blue">{{ item.category }}</span>
            <span class="chip chip-green" v-if="item.duration">{{ item.duration }}</span>
            <span class="chip chip-orange" v-if="item.frequency">{{ item.frequency }}</span>
          </div>
          <p class="hero-desc">{{ item.description }}</p>
        </div>
      </div>

      <div class="trv-body">
        <div class="trv-section-label"><el-icon><Calendar /></el-icon> 详细日程安排</div>
        <div class="schedule-list" v-if="parsedDays.length > 0">
          <div
            v-for="(day, idx) in parsedDays"
            :key="idx"
            class="schedule-day-card"
            :class="{ 'rest-day': day.type === '休息' }"
          >
            <div class="sdc-left">
              <div class="sdc-day-badge">{{ day.day || ('D' + (idx + 1)) }}</div>
            </div>
            <div class="sdc-right">
              <div class="sdc-head">
                <span class="sdc-title">{{ day.title || day.type }}</span>
                <el-tag
                  size="small"
                  :type="day.type === '休息' ? 'success' : 'primary'"
                  effect="light"
                >{{ day.type }}</el-tag>
              </div>
              <div class="sdc-actions" v-if="day.type === '训练' && day.actions?.length">
                <span class="action-badge" v-for="(act, ai) in day.actions" :key="ai">
                  {{ act.name }}
                  <span v-if="act.sets" class="action-sets">{{ act.sets }}</span>
                </span>
              </div>
              <div v-else-if="day.type === '休息'" class="sdc-rest-hint">充分休息，让身体更好恢复 💤</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="该计划暂未编排具体日程" :image-size="70" />
      </div>
    </template>

    <!-- ═══ COURSE ═══ -->
    <template v-else-if="type === 'course'">
      <div class="trv-hero course-hero">
        <div class="hero-cover" v-if="item.coverImage">
          <img :src="item.coverImage" alt="封面" />
        </div>
        <div class="hero-cover course-cover-placeholder" v-else>
          <el-icon class="cover-icon"><Lightning /></el-icon>
        </div>
        <div class="hero-info">
          <div class="hero-title">{{ item.title }}</div>
          <div class="hero-chips">
            <span class="chip chip-blue">{{ item.category }}</span>
            <span class="chip chip-purple" v-if="item.difficulty">{{ item.difficulty }}</span>
            <span class="chip chip-green" v-if="item.durationMinutes">{{ item.durationMinutes }} 分钟</span>
          </div>
          <p class="hero-desc" v-if="item.description">{{ item.description }}</p>
        </div>
      </div>

      <div class="trv-body">
        <div class="trv-section-label"><el-icon><List /></el-icon> 动作序列</div>
        <div class="action-timeline" v-if="parsedActions.length > 0">
          <div
            v-for="(act, idx) in parsedActions"
            :key="idx"
            class="atl-item"
          >
            <div class="atl-num">{{ idx + 1 }}</div>
            <div class="atl-card">
              <div class="atl-name">{{ act.name }}</div>
              <div class="atl-meta">
                <span class="atl-sets" v-if="act.sets"><el-icon><RefreshRight /></el-icon> {{ act.sets }}</span>
                <span class="atl-rest" v-if="act.rest"><el-icon><Timer /></el-icon> 休息 {{ act.rest }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="该课程暂未编排具体动作" :image-size="70" />
      </div>
    </template>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  VideoCamera, VideoPlay, VideoCameraFilled, Document, Warning,
  Calendar, RefreshRight, Timer, TrophyBase, Lightning, List
} from '@element-plus/icons-vue'

const props = defineProps<{
  item: any
  type: 'exercise' | 'plan' | 'course'
}>()

const difficultyChipClass = (d: string) => {
  if (d === '高级') return 'chip-red'
  if (d === '中级') return 'chip-orange'
  return 'chip-green'
}

const parsedErrors = computed<string[]>(() => {
  const raw = props.item?.commonErrors
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  try { return JSON.parse(raw) } catch { return raw.split(',').map((s: string) => s.trim()).filter(Boolean) }
})

const parsedDays = computed<any[]>(() => {
  const raw = props.item?.actions
  if (!raw) return []
  try { return JSON.parse(raw) } catch { return [] }
})

const parsedActions = computed<any[]>(() => {
  const raw = props.item?.actionsJson || props.item?.actions
  if (!raw) return []
  try { return JSON.parse(raw) } catch { return [] }
})
</script>

<style scoped>
.trv-container {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ── Hero Banner ── */
.trv-hero {
  display: flex;
  gap: 20px;
  padding: 24px;
  border-radius: 16px 16px 0 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%);
  color: white;
  align-items: center;
}
.plan-hero { background: linear-gradient(135deg, #1f8a70 0%, #2d6cdf 100%); }
.course-hero { background: linear-gradient(135deg, #7c3aed 0%, #ef7d4f 100%); }
.ex-hero { background: linear-gradient(135deg, #0f172a 0%, #1f8a70 100%); }

/* Cover Image */
.hero-cover {
  width: 88px;
  height: 88px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
}
.hero-cover img { width: 100%; height: 100%; object-fit: cover; }
.plan-cover-placeholder,
.course-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.15);
}
.cover-icon { font-size: 40px; color: rgba(255,255,255,0.85); }

/* Hero Icon for Exercise */
.hero-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hero-icon { font-size: 36px; color: white; }

.hero-info { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.hero-title { font-size: 20px; font-weight: 800; letter-spacing: -0.02em; color: white; }
.hero-desc { font-size: 13px; color: rgba(255,255,255,0.75); margin: 0; line-height: 1.5; }

/* Chips */
.hero-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 100px;
  background: rgba(255,255,255,0.18);
  color: white;
  letter-spacing: 0.02em;
}
.chip-blue { background: rgba(59,130,246,0.35); }
.chip-green { background: rgba(16,185,129,0.35); }
.chip-purple { background: rgba(139,92,246,0.35); }
.chip-orange { background: rgba(249,115,22,0.35); }
.chip-red { background: rgba(239,68,68,0.4); }

/* ── Body ── */
.trv-body {
  background: #f8fafc;
  border-radius: 0 0 16px 16px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.trv-section { display: flex; flex-direction: column; gap: 8px; }
.trv-section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}
.trv-text { margin: 0; font-size: 14px; color: #334155; line-height: 1.7; }

/* Info Blocks */
.trv-two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.trv-info-block {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
}
.info-block-label { font-size: 11px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }
.info-block-value { font-size: 16px; font-weight: 800; color: #1e293b; margin-top: 4px; }

/* Error Pills */
.error-pill-list { display: flex; flex-wrap: wrap; gap: 8px; }
.error-pill {
  display: flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 8px;
  background: #fff1f2;
  color: #be123c;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #fecdd3;
}

/* Video Stub */
.video-stub {
  background: #f1f5f9;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}
.video-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #3b82f6;
  font-weight: 700;
  font-size: 14px;
  text-decoration: none;
}

/* ── Schedule Day Cards (Plan) ── */
.schedule-list { display: flex; flex-direction: column; gap: 10px; }
.schedule-day-card {
  display: flex;
  gap: 14px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  align-items: flex-start;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.schedule-day-card:hover { border-color: #93c5fd; box-shadow: 0 4px 12px rgba(59,130,246,0.08); }
.schedule-day-card.rest-day { background: #f0fdf4; border-color: #bbf7d0; }

.sdc-left { flex-shrink: 0; }
.sdc-day-badge {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2d6cdf, #1f8a70);
  color: white;
  font-size: 11px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: -0.02em;
}
.rest-day .sdc-day-badge { background: linear-gradient(135deg, #10b981, #34d399); }

.sdc-right { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.sdc-head { display: flex; justify-content: space-between; align-items: center; }
.sdc-title { font-weight: 700; color: #1e293b; font-size: 14px; }
.sdc-rest-hint { font-size: 12px; color: #10b981; }
.sdc-actions { display: flex; flex-wrap: wrap; gap: 6px; }
.action-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #475569;
  font-weight: 600;
}
.action-sets { color: #94a3b8; font-weight: 500; }

/* ── Action Timeline (Course) ── */
.action-timeline { display: flex; flex-direction: column; gap: 10px; }
.atl-item {
  display: flex;
  gap: 14px;
  align-items: stretch;
}
.atl-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #ef7d4f);
  color: white;
  font-size: 13px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 10px;
}
.atl-card {
  flex: 1;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.atl-card:hover { border-color: #c4b5fd; box-shadow: 0 4px 12px rgba(124,58,237,0.08); }
.atl-name { font-weight: 700; color: #1e293b; font-size: 14px; margin-bottom: 6px; }
.atl-meta { display: flex; gap: 14px; font-size: 12px; color: #64748b; align-items: center; }
.atl-sets { display: flex; align-items: center; gap: 4px; font-weight: 600; color: #334155; }
.atl-rest { display: flex; align-items: center; gap: 4px; color: #f59e0b; font-weight: 600; }
</style>
