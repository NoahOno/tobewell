<template>
  <div class="login-experience">
    <div class="premium-card login-card-inner">
      <div class="login-header">
        <div class="logo-wrapper">
          <el-icon :size="32" color="white"><TrendCharts /></el-icon>
        </div>
        <h1>欢迎回来</h1>
        <p>开启您的每日健康之旅</p>
      </div>

      <el-form :model="loginForm" label-position="top" class="form-premium">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入您的账户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入您的登录密码" show-password />
        </el-form-item>
        
        <el-button type="primary" class="login-btn-premium" :loading="loading" @click="handleLogin" round>
          立即开启健康生活
        </el-button>
        
        <div class="login-actions">
          <span>还没有加入？</span>
          <el-button link type="primary" class="register-link-premium" @click="router.push('/register')">
            立即创建账号
          </el-button>
        </div>
      </el-form>

      <div class="demo-hints">
        <p>管理员演示: <strong>admin</strong> / 123456</p>
        <p>普通用户演示: <strong>user</strong> / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { TrendCharts } from '@element-plus/icons-vue'
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
    return ElMessage.warning('请输入完整登录凭据')
  }
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', loginForm)
    localStorage.setItem('token', res.data)
    
    // Fetch info to determine redirect
    const infoRes: any = await request.get('/auth/info')
    if (infoRes.data.role === 'ADMIN') {
      ElMessage.success('管理员登录成功，进入控制中心')
      router.push('/admin')
    } else {
      ElMessage.success('登录成功，准备就绪')
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
.login-experience {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-main);
  background-image: 
    radial-gradient(at 0% 0%, rgba(74, 222, 128, 0.1) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(59, 130, 246, 0.1) 0px, transparent 50%);
}

.login-card-inner {
  width: 440px;
  padding: 48px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 20px -5px rgba(74, 222, 128, 0.4);
}

.login-header h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: var(--text-main);
  letter-spacing: -0.02em;
}

.login-header p {
  color: var(--text-muted);
  font-size: 16px;
  margin: 0;
}

.form-premium :deep(.el-form-item) {
  margin-bottom: 24px;
}

.form-premium :deep(.el-form-item__label) {
  font-weight: 700;
  color: var(--text-muted);
  padding-bottom: 8px;
}

.login-btn-premium {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  margin-top: 12px;
  box-shadow: 0 10px 15px -3px rgba(74, 222, 128, 0.3);
}

.login-actions {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-muted);
}

.register-link-premium {
  font-weight: 700;
  margin-left: 4px;
}

.demo-hints {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #F1F5F9;
  font-size: 13px;
  color: var(--text-light);
  text-align: center;
}

.demo-hints p {
  margin: 4px 0;
}

.demo-hints strong {
  color: var(--text-muted);
}
</style>
