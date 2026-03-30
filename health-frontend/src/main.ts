import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

const revealObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
        if (!entry.isIntersecting) continue
        const el = entry.target as HTMLElement
        el.classList.add('is-in')
        revealObserver.unobserve(el)
    }
}, { threshold: 0.16 })

app.directive('reveal', {
    mounted(el: HTMLElement) {
        el.classList.add('reveal')
        requestAnimationFrame(() => revealObserver.observe(el))
    },
    unmounted(el: HTMLElement) {
        revealObserver.unobserve(el)
    }
})

app.mount('#app')
