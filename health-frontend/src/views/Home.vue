<template>
  <div class="home-container">
    <el-header class="home-header">
      <div class="logo">
        <el-icon :size="24" color="#67C23A"><Odometer /></el-icon>
        <span>HealthPlatform</span>
      </div>
      <div class="nav-right">
        <template v-if="!isLoggedIn">
          <el-button type="primary" link @click="router.push('/login')">登录</el-button>
          <el-button type="success" @click="router.push('/register')">加入我们</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/app/dashboard')">进入控制台</el-button>
        </template>
      </div>
    </el-header>

    <main class="hero-section">
      <div class="hero-content">
        <h1>关注健康，遇见更好的自己</h1>
        <p>全面的健康指标监测、智能训练计划管理、专业的健康资讯，助您开启科学生活每一天。</p>
        
        <!-- Quick Login Form on Home Page -->
        <div v-if="!isLoggedIn" class="quick-login-box">
          <h3>立即登录</h3>
          <el-form :model="loginForm" label-position="top">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-button type="success" class="login-button" :loading="loading" @click="handleLogin">
              登录
            </el-button>
          </el-form>
          <div class="quick-links">
            <el-button link type="primary" @click="router.push('/register')">还没有账号？立即注册</el-button>
          </div>
        </div>
        
        <div v-else class="cta-buttons">
          <el-button type="success" size="large" @click="router.push('/app/dashboard')">查看我的健康情况</el-button>
        </div>
      </div>
      <div class="hero-image">
        <el-icon :size="200" color="#E1F3D8"><TrendCharts /></el-icon>
      </div>
    </main>

    <section class="features">
      <el-row :gutter="40">
        <el-col :span="8">
          <el-card shadow="hover" class="feature-card">
            <h3>指标监测</h3>
            <p>实时记录体重、步数、心率等核心数据，动态趋势可视化。</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="feature-card">
            <h3>训练计划</h3>
            <p>个性化定制运动方案，日程管理，见证每一个小目标达成。</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="feature-card">
            <h3>健康资讯</h3>
            <p>精选权威文章，为您提供最科学的膳食与生活方式建议。</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <el-footer class="home-footer">
      <p>© 2026 HealthPlatform. 您的健康管理专家.</p>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Odometer, TrendCharts, User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const isLoggedIn = ref(false)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

onMounted(() => {
  isLoggedIn.value = !!localStorage.getItem('token')
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', loginForm)
    localStorage.setItem('token', res.data)
    ElMessage.success('登录成功')
    router.push('/app/dashboard')
    isLoggedIn.value = true
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.home-header {
  height: 70px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 50px;
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100px 10%;
  background: linear-gradient(135deg, #f0f9eb 0%, #ffffff 100%);
  flex: 1;
}
.hero-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
  color: #303133;
}
.hero-content p {
  font-size: 18px;
  color: #606266;
  max-width: 600px;
  margin-bottom: 40px;
  line-height: 1.6;
}
.cta-buttons {
  display: flex;
  gap: 20px;
}
.features {
  padding: 80px 10%;
  background: white;
}
.feature-card {
  text-align: center;
  padding: 20px;
  border-radius: 15px;
  height: 100%;
}
.feature-card h3 {
  margin-bottom: 15px;
}
.home-footer {
  text-align: center;
  padding: 40px;
  color: #909399;
  border-top: 1px solid #eee;
}
.quick-login-box {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 350px;
  margin-top: 20px;
}
.quick-login-box h3 {
  margin-bottom: 20px;
  text-align: center;
  color: #303133;
}
.login-button {
  width: 100%;
}
.quick-links {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
}
</style>
