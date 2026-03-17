<template>
  <div class="register-experience">
    <div class="premium-card register-card-inner">
      <div class="register-header">
        <h1>加入健康社区 🤝</h1>
        <p>开启您的个性化健康管理新篇章</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="form-premium">
        <el-form-item label="登录账户" prop="username">
          <el-input v-model="form.username" placeholder="建议使用您的手机或邮箱" />
        </el-form-item>
        
        <el-form-item label="社区昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="大家该如何称呼您？" />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设置密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="安全性至关重要" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次确认" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-button type="primary" class="register-btn-premium" :loading="loading" @click="handleRegister" round>
          创建我的健康档案
        </el-button>

        <div class="register-actions">
          <span>已经是会员？</span>
          <el-button link type="primary" class="login-link-premium" @click="router.push('/login')">
            返回登录
          </el-button>
        </div>
      </el-form>
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
  username: [{ required: true, message: '请填写登录账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请填写昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请设置登录密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认您的密码', trigger: 'blur' },
    { validator: (rule: any, value: any, callback: any) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      }, trigger: 'blur' }
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
      ElMessage.success('注册成功！欢迎加入健康管理平台')
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
.register-experience {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-main);
  background-image: 
    radial-gradient(at 0% 0%, rgba(74, 222, 128, 0.1) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(59, 130, 246, 0.1) 0px, transparent 50%);
}

.register-card-inner {
  width: 500px;
  padding: 48px;
}

.register-header {
  margin-bottom: 40px;
  text-align: center;
}

.register-header h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: var(--text-main);
  letter-spacing: -0.02em;
}

.register-header p {
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

.register-btn-premium {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  margin-top: 12px;
  box-shadow: 0 10px 15px -3px rgba(74, 222, 128, 0.3);
}

.register-actions {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-muted);
}

.login-link-premium {
  font-weight: 700;
  margin-left: 4px;
}
</style>
