import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
    },
    {
        path: '/dashboard',
        redirect: '/app/dashboard'
    },
    {
        path: '/records',
        redirect: '/app/records'
    },
    {
        path: '/explore',
        redirect: '/app/explore'
    },
    {
        path: '/community',
        redirect: '/app/community'
    },
    {
        path: '/profile',
        redirect: '/app/profile'
    },
    {
        path: '/app',
        component: () => import('../views/Layout.vue'),
        redirect: '/app/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue')
            },
            {
                path: 'records',
                name: 'Records',
                component: () => import('../views/Records.vue')
            },
            {
                path: 'community',
                name: 'Community',
                component: () => import('../views/Community.vue')
            },
            {
                path: 'explore',
                name: 'Explore',
                component: () => import('../views/Explore.vue')
            },
            {
                path: 'training',
                name: 'Training',
                component: () => import('../views/Training.vue')
            },
            {
                path: 'exercises',
                name: 'Exercises',
                component: () => import('../views/ExerciseLibrary.vue')
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue')
            },
            {
                path: 'collections',
                name: 'Collections',
                component: () => import('../views/Collections.vue')
            },
            {
                path: 'mine',
                name: 'Mine',
                component: () => import('../views/Dashboard.vue')
            },
        ]
    },
    {
        path: '/admin',
        component: () => import('../views/admin/AdminLayout.vue'),
        redirect: '/admin/users',
        children: [
            {
                path: 'users',
                name: 'UserAdmin',
                component: () => import('../views/admin/UserAdmin.vue')
            },
            {
                path: 'content',
                name: 'ContentAdmin',
                component: () => import('../views/admin/ContentAdmin.vue')
            },
            {
                path: 'training',
                name: 'TrainingAdmin',
                component: () => import('../views/admin/TrainingAdmin.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const publicPages = ['/', '/login', '/register']
    const authRequired = !publicPages.includes(to.path)

    if (authRequired && !token) {
        next('/login')
    } else if (token && (to.path === '/login' || to.path === '/register')) {
        next('/app/dashboard')
    } else {
        next()
    }
})

export default router
