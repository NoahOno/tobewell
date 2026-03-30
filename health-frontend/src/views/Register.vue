<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-copy">
        <div class="brand">tobeWell</div>
        <h1>加入我们</h1>
        <p>建立属于你的健康节奏。我们只问必要信息，不打扰你的日常。</p>
        <ul>
          <li>建立可执行的行动计划</li>
          <li>获得清晰的状态反馈</li>
          <li>用更少提醒获得更多结果</li>
        </ul>
      </div>

      <div class="auth-card">
        <div class="card-head">
          <h2>创建账号</h2>
          <p>用最少步骤开始你的健康旅程。</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="auth-form">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" placeholder="手机号或邮箱" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="你希望被如何称呼" />
          </el-form-item>

          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="设置密码" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入" show-password />
              </el-form-item>
            </el-col>
          </el-row>

          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister">
            创建账号
          </el-button>

          <div class="auth-switch">
            已有账号？
            <el-button link type="primary" @click="router.push('/login')">返回登录</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请填写账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请填写昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请设置密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res: any = await request.post('/auth/register', {
      username: form.username,
      nickname: form.nickname,
      password: form.password
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，欢迎加入')
      router.push('/login')
    } else {
      ElMessage.error(res.msg)
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