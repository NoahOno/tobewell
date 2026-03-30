<template>
  <div class="home">
    <header class="home-header">
      <div class="logo">tobeWell</div>
      <nav class="home-nav">
        <a href="#core">核心观念</a>
        <a href="#health">健康</a>
        <a href="#community">社区</a>
        <a href="#ai">AI</a>
        <a href="#faqs">FAQs</a>
      </nav>
      <div class="nav-actions">
        <el-button link @click="router.push('/login')">登录</el-button>
        <el-button type="primary" @click="router.push('/register')">加入我们</el-button>
      </div>
    </header>

    <main class="hero" id="top">
      <div class="hero-inner">
        <p class="hero-eyebrow">为长期稳定的身心状态而设计</p>
        <h1>让健康更可持续，<br />让你更相信自己能坚持</h1>
        <p class="hero-desc">
          tobeWell 把健康管理拆解为可理解、可执行、可复盘的日常节奏。
          你不会被提醒轰炸，而是获得恰到好处的支持与反馈。
        </p>
        <a class="hero-link" href="#core">了解我们的核心观念</a>
      </div>
      <div class="hero-grid">
        <div v-reveal class="hero-card delay-1">
          <div class="card-kicker">核心观念</div>
          <div class="card-title">节奏优先，不靠意志力硬撑</div>
          <div class="card-desc">用可持续的安排替代短期冲刺，减少反复和疲惫。</div>
        </div>
        <div v-reveal class="hero-card delay-2">
          <div class="card-kicker">健康</div>
          <div class="card-title">习惯、情绪与行动一体化</div>
          <div class="card-desc">把每天能做到的小事，变成看得见的长期进步。</div>
        </div>
        <div v-reveal class="hero-card delay-3">
          <div class="card-kicker">AI</div>
          <div class="card-title">建议只在真正需要时出现</div>
          <div class="card-desc">不是更多提醒，而是更少、更准、更有用的支持。</div>
        </div>
      </div>
    </main>

    <section id="core" class="section">
      <div class="section-head">
        <p class="section-kicker">核心观念</p>
        <h2>从追逐目标，转向建立节奏</h2>
        <p class="section-desc">把健康变成能长期执行的生活机制，而不是阶段性任务。</p>
      </div>
      <div class="section-grid">
        <div v-reveal class="info-card delay-1">
          <h3>可持续优先</h3>
          <p>重视每天能做到的微小动作，长期积累才会真正改变状态。</p>
        </div>
        <div v-reveal class="info-card delay-2">
          <h3>行为可解释</h3>
          <p>每条建议都应当有原因，让你理解为什么做，而不是被动执行。</p>
        </div>
        <div v-reveal class="info-card delay-3">
          <h3>反馈更真实</h3>
          <p>看趋势而不是看一时起伏，让决策基于事实而不是情绪。</p>
        </div>
      </div>
    </section>

    <section id="health" class="section alt">
      <div class="section-head">
        <p class="section-kicker">健康</p>
        <h2>把身体状态与心理感受放在同一张图里</h2>
        <p class="section-desc">睡眠、训练、饮食与压力被放在一起理解，变化才会更清楚。</p>
      </div>
      <div class="section-grid">
        <div v-reveal class="info-card delay-1">
          <h3>清晰的每日概览</h3>
          <p>一眼看见当天的关键状态，知道今天应该修正和保留什么。</p>
        </div>
        <div v-reveal class="info-card delay-2">
          <h3>温和的趋势复盘</h3>
          <p>通过连续记录观察变化，不用被单次波动打乱判断。</p>
        </div>
        <div v-reveal class="info-card delay-3">
          <h3>低负担的行动建议</h3>
          <p>建议足够轻，才能真正进入日常，而不是停留在计划里。</p>
        </div>
      </div>
    </section>

    <section id="community" class="section">
      <div class="section-head">
        <p class="section-kicker">社区</p>
        <h2>在正确的语境中获得支持</h2>
        <p class="section-desc">社区不是噪声池，而是围绕习惯、目标和复盘形成的同伴关系。</p>
      </div>
      <div class="section-grid">
        <div v-reveal class="info-card delay-1">
          <h3>同频小组</h3>
          <p>找到和你目标相近、节奏相似的人，一起推进长期习惯。</p>
        </div>
        <div v-reveal class="info-card delay-2">
          <h3>真实分享</h3>
          <p>讨论执行过程中的困难与变化，让经验更具可参考性。</p>
        </div>
        <div v-reveal class="info-card delay-3">
          <h3>温和激励</h3>
          <p>少一点比较，多一点基于事实的支持，帮助你稳定前进。</p>
        </div>
      </div>
    </section>

    <section id="ai" class="section alt">
      <div class="section-head">
        <p class="section-kicker">AI</p>
        <h2>更少干扰，更准判断</h2>
        <p class="section-desc">AI 不替你决定，而是帮助你在关键时刻更快看清选择。</p>
      </div>
      <div class="section-grid">
        <div v-reveal class="info-card delay-1">
          <h3>个性化策略</h3>
          <p>结合你的状态与目标，给出适合当前阶段的行动建议。</p>
        </div>
        <div v-reveal class="info-card delay-2">
          <h3>即时复盘</h3>
          <p>帮助你理解波动背后的原因，而不是只告诉你结果。</p>
        </div>
        <div v-reveal class="info-card delay-3">
          <h3>可控的提醒</h3>
          <p>提醒频率由你控制，系统默认保持低干扰和高相关性。</p>
        </div>
      </div>
    </section>

    <section id="faqs" class="section">
      <div class="section-head">
        <p class="section-kicker">FAQs</p>
        <h2>你可能关心的问题</h2>
        <p class="section-desc">先把最常见的问题回答清楚，降低理解成本。</p>
      </div>
      <el-collapse class="faq" accordion>
        <el-collapse-item name="1" title="tobeWell 适合什么样的人？">
          适合希望建立长期健康习惯、又不想被过度打扰的人。
        </el-collapse-item>
        <el-collapse-item name="2" title="必须每天记录吗？">
          不需要。我们更重视稳定节奏，而不是形式化打卡。
        </el-collapse-item>
        <el-collapse-item name="3" title="AI 建议会不会太频繁？">
          默认是低干扰模式，只有在真正相关的时候才会提示。
        </el-collapse-item>
        <el-collapse-item name="4" title="社区会不会很吵？">
          社区更偏向小组协作和主题讨论，不做无效社交堆积。
        </el-collapse-item>
      </el-collapse>
    </section>

    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">tobeWell</div>
        <div class="footer-rights">© 2026 tobeWell</div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
:global(html) {
  scroll-behavior: smooth;
}

.home {
  --page-max: 1200px;
  --page-x: clamp(20px, 5vw, 64px);
  min-height: 100vh;
  background:
    radial-gradient(at 10% 0%, rgba(31, 138, 112, 0.08) 0px, transparent 45%),
    radial-gradient(at 90% 20%, rgba(45, 108, 223, 0.07) 0px, transparent 50%),
    #f6f7f9;
  color: #111827;
}

.home-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 20px;
  padding: 16px var(--page-x);
  backdrop-filter: blur(14px);
  background: rgba(246, 247, 249, 0.92);
  border-bottom: 1px solid #e5e7eb;
}

.logo {
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.02em;
}

.home-nav {
  display: flex;
  align-items: center;
  gap: 18px;
  justify-content: center;
  font-size: 14px;
  color: #6b7280;
}

.home-nav a {
  color: inherit;
  text-decoration: none;
  padding: 6px 4px;
  border-bottom: 1px solid transparent;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.home-nav a:hover {
  color: #111827;
  border-color: #111827;
}

.nav-actions {
  display: flex;
  gap: 8px;
}

.hero {
  width: min(var(--page-max), calc(100% - var(--page-x) * 2));
  margin: 32px auto 0;
  padding: 36px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 24px 60px -40px rgba(17, 24, 39, 0.35);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
}

.hero-inner h1 {
  font-size: clamp(30px, 4vw, 46px);
  line-height: 1.15;
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.hero-eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #6b7280;
  margin: 0 0 12px;
}

.hero-desc {
  color: #4b5563;
  font-size: 16px;
  line-height: 1.7;
  margin: 0 0 18px;
}

.hero-link {
  color: #111827;
  text-decoration: none;
  font-weight: 700;
  border-bottom: 1px solid #111827;
}

.hero-grid {
  display: grid;
  gap: 12px;
}

.hero-card {
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 16px;
  padding: 18px;
}

.card-kicker {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.card-title {
  font-weight: 800;
  margin-bottom: 6px;
}

.card-desc {
  color: #6b7280;
  font-size: 14px;
}

.section {
  width: min(var(--page-max), calc(100% - var(--page-x) * 2));
  margin: 48px auto 0;
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 24px;
  scroll-margin-top: 90px;
}

.section.alt {
  background: #f9fafb;
}

.section-head {
  margin-bottom: 22px;
}

.section-kicker {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #6b7280;
  margin: 0 0 8px;
}

.section-head h2 {
  margin: 0 0 8px;
  font-size: clamp(24px, 3vw, 32px);
  letter-spacing: -0.02em;
}

.section-desc {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  background: #ffffff;
}

.info-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.info-card p {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
}

.faq {
  margin-top: 12px;
}

:deep(.el-collapse-item__header) {
  font-weight: 700;
  font-size: 15px;
}

:deep(.el-collapse-item__content) {
  color: #4b5563;
  line-height: 1.7;
}

.home-footer {
  width: min(var(--page-max), calc(100% - var(--page-x) * 2));
  margin: 40px auto 28px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #6b7280;
  font-size: 13px;
}

.footer-brand {
  font-weight: 700;
  color: #111827;
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .home-nav {
    display: none;
  }

  .section-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero,
  .section {
    padding: 24px;
  }

  .footer-inner {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>