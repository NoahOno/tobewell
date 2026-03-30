<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-copy">
        <div class="brand">tobeWell</div>
        <h1>欢迎回来</h1>
        <p>登录后继续你的健康节奏。我们只保留必要信息，保证体验简洁。</p>
        <ul>
          <li>清晰的状态概览</li>
          <li>可持续的行动建议</li>
          <li>低干扰的提醒机制</li>
        </ul>
      </div>

      <div class="auth-card">
        <div class="card-head">
          <h2>登录</h2>
          <p>使用你的账号进入 tobeWell。</p>
        </div>

        <el-form :model="loginForm" label-position="top" class="auth-form">
          <el-form-item label="账号">
            <el-input v-model="loginForm.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>

          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
            进入 tobeWell
          </el-button>

          <div class="auth-switch">
            还没有账号？
            <el-button link type="primary" @click="router.push('/register')">加入我们</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入完整登录信息')
  }
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', loginForm)
    localStorage.setItem('token', res.data)

    const infoRes: any = await request.get('/auth/info')
    if (infoRes.data.role === 'ADMIN') {
      ElMessage.success('管理员登录成功')
      router.push('/admin')
    } else {
      ElMessage.success('登录成功')
      router.push('/app/dashboard')
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(at 0% 0%, rgba(31, 138, 112, 0.08) 0px, transparent 45%),
    radial-gradient(at 100% 100%, rgba(45, 108, 223, 0.08) 0px, transparent 50%),
    #f6f7f9;
  padding: 32px;
}

.auth-shell {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.auth-copy {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 24px;
  padding: 36px;
}

.brand {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 24px;
}

.auth-copy h1 {
  font-size: 32px;
  margin: 0 0 12px;
}

.auth-copy p {
  color: #6b7280;
  margin: 0 0 20px;
  line-height: 1.7;
}

.auth-copy ul {
  padding-left: 18px;
  margin: 0;
  color: #4b5563;
  display: grid;
  gap: 8px;
}

.auth-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 24px;
  padding: 36px;
  box-shadow: 0 24px 60px -40px rgba(17, 24, 39, 0.35);
}

.card-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
}

.card-head p {
  margin: 0 0 24px;
  color: #6b7280;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #4b5563;
  padding-bottom: 6px;
}

.auth-btn {
  width: 100%;
  height: 48px;
  font-weight: 700;
  margin-top: 6px;
}

.auth-switch {
  margin-top: 16px;
  color: #6b7280;
  text-align: center;
}

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }
}
</style>