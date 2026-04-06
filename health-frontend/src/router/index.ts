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
                path: 'my-activities',
                name: 'MyActivities',
                component: () => import('../views/MyActivities.vue')
            },
            {
                path: 'mine',
                name: 'Mine',
                component: () => import('../views/Dashboard.vue')
            },
            {
                // Keep path under /app/explore so Layout sidebar logic still treats it as "探索"
                path: 'explore/webai',
                name: 'WebAIChat',
                component: () => import('../views/WebAIChat.vue')
            },
        ]
    },
    {
        path: '/admin',
        component: () => import('../views/admin/AdminLayout.vue'),
        redirect: '/admin/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'AdminDashboard',
                component: () => import('../views/admin/Dashboard.vue')
            },
            {
                path: 'users',
                name: 'UserAdmin',
                component: () => import('../views/admin/UserAdmin.vue')
            },
            {
                // Community Management - Sub routes
                path: 'community',
                redirect: '/admin/community/posts',
                children: [
                    {
                        path: 'posts',
                        name: 'AdminCommunityPosts',
                        component: () => import('../views/admin/CommunityPosts.vue')
                    },
                    {
                        path: 'activities',
                        name: 'AdminCommunityActivities',
                        component: () => import('../views/admin/CommunityActivities.vue')
                    }
                ]
            },
            {
                // Content Library - Sub routes
                path: 'content-library',
                redirect: '/admin/content-library/actions',
                children: [
                    {
                        path: 'actions',
                        name: 'AdminLibraryActions',
                        component: () => import('../views/admin/LibraryActions.vue')
                    },
                    {
                        path: 'courses',
                        name: 'AdminLibraryCourses',
                        component: () => import('../views/admin/LibraryCourses.vue')
                    },
                    {
                        path: 'plans',
                        name: 'AdminLibraryPlans',
                        component: () => import('../views/admin/LibraryPlans.vue')
                    },
                    {
                        path: 'audit',
                        name: 'AdminLibraryAudit',
                        component: () => import('../views/admin/LibraryAudit.vue')
                    }
                ]
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    let token = localStorage.getItem('token')
    if (token === 'null' || token === 'undefined' || token === '') {
        token = null
        localStorage.removeItem('token')
    }
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
