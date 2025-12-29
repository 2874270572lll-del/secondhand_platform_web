import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
  path: '/product/edit/:id',
  name: 'EditProduct',
  component: () => import('../views/PublishProduct.vue'),
  meta: { requiresAuth: true }
},
  {
    path: '/publish',
    name: 'PublishProduct',
    component: () => import('../views/PublishProduct.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my',
    name: 'My',
    component: () => import('../views/My.vue'),
    meta: { requiresAuth: true },
    redirect: '/my/products',
    children: [
      {
        path: 'products',
        name: 'MyProducts',
        component: () => import('../views/MyProducts.vue')
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('../views/MyOrders.vue')
      },
      {
        path: 'favorites',
        name: 'MyFavorites',
        component: () => import('../views/MyFavorites.vue')
      },
      {
        path: 'messages',
        name: 'MyMessages',
        component: () => import('../views/MyMessages.vue')
      },
      {
        path: 'profile',
        name: 'MyProfile',
        component: () => import('../views/MyProfile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

export default router
