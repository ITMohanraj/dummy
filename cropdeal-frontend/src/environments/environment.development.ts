export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8080',
  wsBiddingUrl: 'http://localhost:8080/ws-bidding',
  endpoints: {
    auth: '/api/v1/auth',
    users: '/api/v1/users',
    crops: '/api/v1/crops',
    prices: '/api/v1/prices',
    orders: '/api/v1/orders',
    negotiations: '/api/v1/negotiations',
    invoices: '/api/v1/invoices',
    payments: '/api/v1/payments',
    wallets: '/api/v1/wallets',
    bids: '/api/v1/bids',
    auctions: '/api/v1/auctions',
    deliveries: '/api/v1/deliveries',
    notifications: '/api/v1/notifications',
    reviews: '/api/v1/reviews',
    reports: '/api/v1/reports',
    audit: '/api/v1/audit',
    chatbot: '/api/v1/chatbot'
  }
};
