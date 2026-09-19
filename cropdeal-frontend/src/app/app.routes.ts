import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Public Marketplace Routes
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'crops',
    loadComponent: () => import('./features/marketplace/marketplace.component').then(m => m.MarketplaceComponent)
  },
  {
    path: 'crops/:id',
    loadComponent: () => import('./features/crop-details/crop-details.component').then(m => m.CropDetailsComponent)
  },
  {
    path: 'market-prices',
    loadComponent: () => import('./features/market-prices/market-prices.component').then(m => m.MarketPricesComponent)
  },
  {
    path: 'auctions',
    loadComponent: () => import('./features/live-auctions/live-auctions.component').then(m => m.LiveAuctionsComponent)
  },
  {
    path: 'assistant',
    loadComponent: () => import('./features/chatbot/chatbot.component').then(m => m.ChatbotComponent)
  },

  // Auth Routes
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },

  // Shared Authenticated Routes
  {
    path: 'wallet',
    canActivate: [authGuard],
    loadComponent: () => import('./features/wallet/wallet.component').then(m => m.WalletComponent)
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent)
  },

  // Farmer Portal
  {
    path: 'farmer/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-dashboard/farmer-dashboard.component').then(m => m.FarmerDashboardComponent)
  },
  {
    path: 'farmer/crops',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/my-crops/my-crops.component').then(m => m.MyCropsComponent)
  },
  {
    path: 'farmer/crops/new',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/add-crop/add-crop.component').then(m => m.AddCropComponent)
  },
  {
    path: 'farmer/orders',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-orders/farmer-orders.component').then(m => m.FarmerOrdersComponent)
  },

  // Dealer Portal
  {
    path: 'dealer/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-dashboard/dealer-dashboard.component').then(m => m.DealerDashboardComponent)
  },
  {
    path: 'dealer/orders',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-orders/dealer-orders.component').then(m => m.DealerOrdersComponent)
  },

  // Delivery Partner Portal
  {
    path: 'delivery/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['DELIVERY_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./features/delivery/delivery-dashboard/delivery-dashboard.component').then(m => m.DeliveryDashboardComponent)
  },

  // Admin Console
  {
    path: 'admin/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent)
  },

  // Fallback
  {
    path: '**',
    redirectTo: ''
  }
];
