<template>
  <!-- Unified Premium Training Resource Viewer (Light Theme - Refined Timeline) -->
  <div class="trv-premium" :class="[`type-${type}`, 'light-theme']">
    <div class="trv-container">
      
      <!-- LEFT SIDE: Cover Top, Basic Info Bottom -->
      <div class="trv-side-left">
        <div class="trv-media-wrap">
          <img v-if="coverSrc" :src="coverSrc" class="trv-main-img" />
          <div v-else class="trv-img-placeholder">
            <el-icon v-if="type==='exercise'"><VideoCamera /></el-icon>
            <el-icon v-else-if="type==='course'"><Lightning /></el-icon>
            <el-icon v-else><TrophyBase /></el-icon>
          </div>
          <div class="trv-type-badge">{{ typeLabel }}</div>
        </div>

        <div class="trv-hero-info">
          <h1 class="trv-title">{{ item.title || item.name }}</h1>
          
          <div class="trv-main-stats">
            <div class="trv-stat-box">
              <span class="stat-label">难度</span>
              <span class="stat-val" :class="item.difficulty">{{ item.difficulty || '入门' }}</span>
            </div>
            <div class="trv-stat-box">
              <span class="stat-label">预计时长</span>
              <span class="stat-val">{{ item.duration || (item.durationMinutes ? item.durationMinutes + ' 分钟' : '待定') }}</span>
            </div>
          </div>

          <div class="trv-basic-info-list" v-if="type === 'plan' || type === 'course'">
            <div class="info-item" v-if="item.description">
              <div class="info-label">内容详情</div>
              <div class="info-val">{{ item.description }}</div>
            </div>
            <div class="info-item" v-if="item.audience || item.category">
              <div class="info-label">适合人群 / 分类</div>
              <div class="info-val">{{ item.audience || item.category || '全部人群' }}</div>
            </div>
          </div>

          <!-- Quick Actions Slot -->
          <div class="trv-actions-slot">
            <slot name="left-actions"></slot>
          </div>
        </div>
      </div>

      <!-- RIGHT SIDE: Content Area -->
      <div class="trv-main-content">
        <div class="content-scroll-area">
          
          <!-- ═══ EXERCISE (ACTION) CONTENT ═══ -->
          <template v-if="type === 'exercise'">
            <!-- Right Top: GIF/Video -->
            <div class="trv-section no-margin-top">
              <div class="demo-gif-box">
              <template v-if="demoMediaUrl">
                <video
                  v-if="isDemoVideo"
                  controls
                  class="demo-gif"
                  :src="demoMediaUrl"
                  @error="(e:any)=>e.target.style.display='none'"
                ></video>
                <img
                  v-else
                  :src="demoMediaUrl"
                  class="demo-gif"
                  @error="(e:any)=>e.target.src='https://api.dicebear.com/7.x/shapes/svg?seed=demo'"
                />
              </template>
              <div v-else class="demo-stub">
                <el-icon><VideoPlay /></el-icon>
                <span>演示正在准备中...</span>
              </div>
            </div>
            </div>
            
            <!-- Right Bottom: Detailed Info -->
            <div class="trv-details-grid">
               <div class="detail-block">
                 <h4>动作类型</h4>
                 <p>{{ item.type || '未指定' }}</p>
               </div>
               <div class="detail-block">
                 <h4>目标肌群</h4>
                 <p>{{ item.muscle || '全身' }}</p>
               </div>
               <div class="detail-block">
                 <h4>所需器材</h4>
                 <p>{{ item.equipment || '无需器械' }}</p>
               </div>
            </div>

            <div class="trv-section">
              <h3 class="section-h">动作要点</h3>
              <p class="instruction-text">{{ item.instruction || '请保持背部挺直，感受目标肌群发力。' }}</p>
            </div>
            
            <div class="trv-section" v-if="parsedErrors.length > 0">
              <h3 class="section-h warning-text">常见错误</h3>
              <div class="error-list-light">
                <div v-for="(err, i) in parsedErrors" :key="i" class="error-item">
                   <div class="err-dot"></div>
                   {{ err }}
                </div>
              </div>
            </div>
          </template>

          <!-- ═══ COURSE CONTENT (TIMELINE) ═══ -->
          <template v-else-if="type === 'course'">
            <div class="timeline-container">
              <template v-if="parsedActions.length > 0">
                <!-- Phase Label: Start -->
                <div class="timeline-node phase-node">
                  <div class="node-marker dot-only"></div>
                  <div class="node-content">
                    <span class="phase-label">训练开始</span>
                  </div>
                </div>

                <template v-for="(act, idx) in parsedActions" :key="idx">
                  <!-- Action Node with Card -->
                  <div class="timeline-node item-node">
                    <div class="node-line"></div>
                    <div class="node-marker dot-only primary-dot"></div>
                    <div class="node-content">
                      <div class="node-card">
                         <div class="nc-thumb">
                            <img :src="`https://api.dicebear.com/7.x/shapes/svg?seed=${act.name}`" />
                         </div>
                         <div class="nc-info">
                            <div class="nc-title">{{ act.name }}</div>
                            <div class="nc-desc">{{ act.sets }}</div>
                         </div>
                      </div>
                    </div>
                  </div>

                  <!-- Rest Node -->
                  <div class="timeline-node rest-node" v-if="act.rest">
                    <div class="node-line"></div>
                    <div class="node-marker dot-only warning-dot"></div>
                    <div class="node-content">
                      <div class="rest-label">休息 {{ act.rest }}</div>
                    </div>
                  </div>
                </template>

                <!-- Phase Label: End -->
                <div class="timeline-node phase-node">
                  <div class="node-marker dot-only"></div>
                  <div class="node-content">
                    <span class="phase-label">训练结束</span>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="no-content">
                  <el-icon class="no-content-icon"><Timer /></el-icon>
                  <h3 class="no-content-title">课程详情</h3>
                  <p class="no-content-desc">该课程的详细内容正在准备中，加入训练后即可查看完整的训练安排。</p>
                </div>
              </template>
            </div>
          </template>

          <!-- ═══ PLAN CONTENT (TIMELINE) ═══ -->
          <template v-else-if="type === 'plan'">
            <div class="timeline-container plan-timeline">
              <template v-if="parsedDays.length > 0">
                <template v-for="(day, idx) in parsedDays" :key="idx">
                  <!-- Week Label (Every 7 days) -->
                  <div class="timeline-node phase-node" v-if="idx === 0 || idx % 7 === 0">
                    <div class="node-marker dot-only week-dot"></div>
                    <div class="node-content">
                      <span class="phase-label">第 {{ Math.floor(idx / 7) + 1 }} 周</span>
                    </div>
                  </div>

                  <!-- Day Node with Card -->
                  <div class="timeline-node item-node">
                    <div class="node-line"></div>
                    <div class="node-marker dot-only" :class="day.type === '休息' ? 'rest-dot' : 'plan-dot'"></div>
                    <div class="node-content">
                      <div class="node-card" :class="{ 'is-rest': day.type === '休息' }">
                         <div class="nc-thumb">
                            <span v-if="day.type === '休息'" class="rest-emoji">🛌</span>
                            <img v-else :src="`https://api.dicebear.com/7.x/shapes/svg?seed=${day.courseTitle || 'T'}`" />
                         </div>
                         <div class="nc-info">
                            <div class="nc-title">Day {{ Number(idx)+1 }} · {{ day.type === '休息' ? '休息日' : (day.courseTitle || '训练日') }}</div>
                            <div class="nc-desc">{{ day.type === '休息' ? '身体机能恢复' : '执行本日训练指令' }}</div>
                         </div>
                      </div>
                    </div>
                  </div>
                </template>
              </template>
              <template v-else>
                <div class="no-content">
                  <el-icon class="no-content-icon"><Timer /></el-icon>
                  <h3 class="no-content-title">训练计划详情</h3>
                  <p class="no-content-desc">该训练计划的详细内容正在准备中，加入训练后即可查看完整的训练安排。</p>
                </div>
              </template>
            </div>
          </template>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  VideoCamera, TrophyBase, Lightning, VideoPlay, Timer
} from '@element-plus/icons-vue'

const props = defineProps<{
  item: any
  type: 'exercise' | 'plan' | 'course'
}>()

const typeLabel = computed(() => {
  if (props.type === 'exercise') return 'SINGLE ACTION'
  if (props.type === 'course') return 'WORKOUT SESSION'
  return 'TRAINING PLAN'
})

const parsedErrors = computed<string[]>(() => {
  const raw = props.item?.commonErrors
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  try { return JSON.parse(raw) } catch { return raw.split(',').map((s: string) => s.trim()).filter(Boolean) }
})

const parsedDays = computed<any[]>(() => {
  const raw = props.item?.actions
  if (!raw) return []
  try { 
    const p = JSON.parse(raw)
    return Array.isArray(p) ? p : []
  } catch { return [] }
})

const coverSrc = computed(() => {
  return props.item?.coverImage || props.item?.cover_image || props.item?.imageUrl || props.item?.image_url || ''
})

const demoMediaUrl = computed(() => {
  return props.item?.videoUrl || props.item?.video_url || props.item?.imageUrl || props.item?.image_url || ''
})

const isDemoVideo = computed(() => {
  const url = demoMediaUrl.value || ''
  return /\.(mp4|webm|ogg)$/i.test(url)
})

const parsedActions = computed<any[]>(() => {
  const raw = props.item?.actionsJson || props.item?.actions
  if (!raw) return []
  try { 
    const p = JSON.parse(raw)
    return Array.isArray(p) ? p : []
  } catch { return [] }
})
</script>

<style scoped>
.trv-premium {
  width: 100%;
  height: 80vh;
  max-height: 850px;
  background: white;
  overflow: hidden;
  font-family: var(--font-family);
}

.trv-container {
  display: flex;
  height: 100%;
}

/* LEFT SIDE */
.trv-side-left {
  flex: 1;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e2e8f0;
}

.trv-media-wrap {
  width: 100%;
  height: 240px;
  position: relative;
  background: #e2e8f0;
}
.trv-main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.trv-img-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  color: #cbd5e1;
}
.trv-type-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(255,255,255,0.9);
  padding: 4px 10px;
  border-radius: 6px;
  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.trv-hero-info {
  padding: 24px 24px 32px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: hidden;
  max-height: calc(100vh - 300px);
  box-sizing: border-box;
}

.trv-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 16px;
  line-height: 1.2;
}

.trv-main-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.trv-stat-box { display: flex; flex-direction: column; gap: 2px; }
.stat-label { font-size: 10px; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
.stat-val { font-size: 16px; font-weight: 700; color: #1e293b; }
.stat-val.初级, .stat-val.Primary { color: #10b981; }
.stat-val.中级, .stat-val.Intermediate { color: #f59e0b; }
.stat-val.高级, .stat-val.Advanced { color: #ef4444; }

.trv-basic-info-list { margin-bottom: 12px; }
.info-item { margin-bottom: 12px; }
.info-label { font-size: 10px; font-weight: 700; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px; }
.info-val { font-size: 13px; color: #475569; line-height: 1.5; }

.trv-actions-slot {
  margin-top: 8px;
  padding: 12px 0;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
  min-height: 50px;
  box-sizing: border-box;
  margin-bottom: 8px;
}

/* RIGHT SIDE */
.trv-main-content {
  flex: 1.5;
  background: white;
}

.content-scroll-area {
  height: 100%;
  overflow-y: auto;
  padding: 40px;
}

.trv-section { margin-bottom: 40px; }
.no-margin-top { margin-top: 0; }
.section-h { font-size: 18px; font-weight:900; color: #0f172a; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }

/* Exercise Layout */
.demo-gif-box {
  width: 100%;
  aspect-ratio: 16/9;
  background: #f1f5f9;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.demo-gif { width: 100%; height: 100%; object-fit: cover; }
.demo-stub { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8; gap: 12px; }

.trv-details-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}
.detail-block {
  background: #f8fafb;
  padding: 16px;
  border-radius: 12px;
}
.detail-block h4 { font-size: 11px; font-weight: 800; color: #94a3b8; margin: 0 0 4px; text-transform: uppercase; }
.detail-block p { font-size: 14px; font-weight: 700; color: #1e293b; margin: 0; }

.instruction-text { font-size: 15px; line-height: 1.8; color: #475569; }

.warning-text { color: #e11d48; }
.error-list-light { background: #fff1f2; border-radius: 16px; padding: 20px; }
.error-item { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #be123c; font-weight: 600; margin-bottom: 10px; }
.err-dot { width: 6px; height: 6px; border-radius: 50%; background: #be123c; }

/* No Content State */
.no-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
  background: #f8fafc;
  border-radius: 16px;
  margin: 20px 0;
}

.no-content-icon {
  font-size: 48px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.no-content-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.no-content-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  max-width: 400px;
}

/* REFINED TIMELINE UI */
.timeline-container {
  display: flex;
  flex-direction: column;
  padding-left: 12px;
}

.timeline-node {
  display: flex;
  gap: 20px;
  position: relative;
  padding-bottom: 24px;
}
.timeline-node:last-child { padding-bottom: 0; }

.node-line {
  position: absolute;
  left: 3px;
  top: 10px;
  bottom: -14px;
  width: 2px;
  background: #e5e7eb;
  z-index: 0;
}
.timeline-node:last-child .node-line { display: none; }

.node-marker.dot-only {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  z-index: 1;
  margin-top: 10px;
  flex-shrink: 0;
}
.primary-dot { background: #3b82f6; box-shadow: 0 0 0 4px #dbeafe; }
.warning-dot { background: #f59e0b; box-shadow: 0 0 0 4px #fef3c7; }
.week-dot { background: #6366f1; width: 10px; height: 10px; margin-left: -1px; }
.plan-dot { background: #3b82f6; }
.rest-dot { background: #10b981; }

.node-content { flex: 1; padding-top: 2px; }

/* Small Card Layout */
.node-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.node-card:hover { transform: translateY(-2px); border-color: #3b82f6; background: white; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); }
.node-card.is-rest { background: #f0fdf4; border-color: #dcfce7; }

.nc-thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nc-thumb img { width: 100%; height: 100%; object-fit: contain; }
.rest-emoji { font-size: 24px; }

.nc-info { flex: 1; min-width: 0; }
.nc-title { font-size: 15px; font-weight: 800; color: #1e293b; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.nc-desc { font-size: 12px; font-weight: 600; color: #64748b; }

.phase-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.1em; }
.rest-label { color: #f59e0b; font-size: 13px; font-weight: 700; margin-top: 4px;}

@media (max-width: 1024px) {
  .trv-container {
    flex-direction: row;
    min-width: 800px;
  }
  .trv-side-left {
    flex: 1;
    max-width: 400px;
  }
  .trv-main-content {
    flex: 1.5;
    min-width: 400px;
  }
  .trv-title {
    font-size: 24px;
  }
  .content-scroll-area {
    padding: 24px;
  }
}
</style>
