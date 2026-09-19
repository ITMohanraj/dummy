import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Public Marketplace Routes (Accessible to Guests without Login)
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

  // Onboarding Profile Wizard
  {
    path: 'profile/complete',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile-complete/profile-complete.component').then(m => m.ProfileCompleteComponent)
  },

  // Shared Authenticated Routes
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
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

  // Farmer Portal Routes
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
    path: 'farmer/bidding',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-bidding/farmer-bidding.component').then(m => m.FarmerBiddingComponent)
  },
  {
    path: 'farmer/negotiations',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-negotiations/farmer-negotiations.component').then(m => m.FarmerNegotiationsComponent)
  },
  {
    path: 'farmer/orders',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-orders/farmer-orders.component').then(m => m.FarmerOrdersComponent)
  },
  {
    path: 'farmer/reports',
    canActivate: [roleGuard],
    data: { roles: ['FARMER', 'ADMIN'] },
    loadComponent: () => import('./features/farmer/farmer-reports/farmer-reports.component').then(m => m.FarmerReportsComponent)
  },

  // Dealer Portal Routes
  {
    path: 'dealer/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-dashboard/dealer-dashboard.component').then(m => m.DealerDashboardComponent)
  },
  {
    path: 'dealer/bidding',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-bidding/dealer-bidding.component').then(m => m.DealerBiddingComponent)
  },
  {
    path: 'dealer/negotiations',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-negotiations/dealer-negotiations.component').then(m => m.DealerNegotiationsComponent)
  },
  {
    path: 'dealer/orders',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-orders/dealer-orders.component').then(m => m.DealerOrdersComponent)
  },
  {
    path: 'dealer/reports',
    canActivate: [roleGuard],
    data: { roles: ['DEALER', 'ADMIN'] },
    loadComponent: () => import('./features/dealer/dealer-reports/dealer-reports.component').then(m => m.DealerReportsComponent)
  },

  // Delivery Partner Portal Routes
  {
    path: 'delivery/dashboard',
    canActivate: [roleGuard],
    data: { roles: ['DELIVERY_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./features/delivery/delivery-dashboard/delivery-dashboard.component').then(m => m.DeliveryDashboardComponent)
  },
  {
    path: 'delivery/requests',
    canActivate: [roleGuard],
    data: { roles: ['DELIVERY_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./features/delivery/delivery-requests/delivery-requests.component').then(m => m.DeliveryRequestsComponent)
  },
  {
    path: 'delivery/active',
    canActivate: [roleGuard],
    data: { roles: ['DELIVERY_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./features/delivery/delivery-active/delivery-active.component').then(m => m.DeliveryActiveComponent)
  },
  {
    path: 'delivery/history',
    canActivate: [roleGuard],
    data: { roles: ['DELIVERY_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./features/delivery/delivery-history/delivery-history.component').then(m => m.DeliveryHistoryComponent)
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
