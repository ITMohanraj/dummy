import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <footer class="footer">
      <div class="container">
        <div class="footer-grid">
          
          <!-- Column 1: Brand & Mission -->
          <div class="footer-col">
            <div class="logo flex items-center gap-2 mb-3">
              <div class="logo-icon">
                <span class="material-symbols-outlined">eco</span>
              </div>
              <span class="brand-title">Crop<span class="brand-highlight">Deal</span></span>
            </div>
            <p class="footer-desc">
              Empowering India's farmers and agricultural dealers with direct crop trade, real-time government APMC Mandi rates, live bidding, and secure escrow settlements.
            </p>
            <div class="gov-badge flex items-center gap-2 mt-4">
              <span class="material-symbols-outlined text-emerald-400">verified</span>
              <span>Data.gov.in / Agmarknet Integrated</span>
            </div>
          </div>

          <!-- Column 2: Marketplace Links -->
          <div class="footer-col">
            <h4 class="footer-heading">Marketplace</h4>
            <ul class="footer-links">
              <li><a routerLink="/crops" [queryParams]="{category: 'VEGETABLES'}">Fresh Vegetables</a></li>
              <li><a routerLink="/crops" [queryParams]="{category: 'FRUITS'}">Organic Fruits</a></li>
              <li><a routerLink="/crops" [queryParams]="{category: 'CEREALS'}">Grains & Cereals</a></li>
              <li><a routerLink="/crops" [queryParams]="{category: 'PULSES'}">Pulses & Legumes</a></li>
              <li><a routerLink="/crops" [queryParams]="{category: 'SPICES'}">Spices & Condiments</a></li>
              <li><a routerLink="/market-prices">Live Mandi Daily Rates</a></li>
            </ul>
          </div>

          <!-- Column 3: Portals & Tools -->
          <div class="footer-col">
            <h4 class="footer-heading">Portals & Solutions</h4>
            <ul class="footer-links">
              <li><a routerLink="/farmer/dashboard">Farmer Management Hub</a></li>
              <li><a routerLink="/dealer/dashboard">Dealer B2B Trade Floor</a></li>
              <li><a routerLink="/delivery/dashboard">Delivery Partner Hub</a></li>
              <li><a routerLink="/auctions">Live Crop Auctions</a></li>
              <li><a routerLink="/assistant">AI Agricultural Advisor</a></li>
              <li><a routerLink="/admin/dashboard">Admin Escrow Resolution</a></li>
            </ul>
          </div>

          <!-- Column 4: Contact & Help -->
          <div class="footer-col">
            <h4 class="footer-heading">Toll-Free Kisan Helpline</h4>
            <p class="help-number">📞 1800-419-CROP</p>
            <p class="help-sub">Mon - Sat: 6:00 AM - 9:00 PM (IST)</p>
            <div class="mt-4 text-xs text-slate-400">
              <p>Email: support&#64;cropdeal.gov.in</p>
              <p class="mt-1">Headquarters: New Delhi, India</p>
            </div>
          </div>

        </div>

        <div class="footer-bottom flex justify-between items-center text-xs">
          <div>© 2026 CropDeal Technologies Ltd. All rights reserved.</div>
          <div class="flex gap-4">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
            <a href="#">Security & Escrow Guarantee</a>
          </div>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background: #0f172a;
      color: #94a3b8;
      padding: 4rem 0 2rem;
      margin-top: auto;
      border-top: 1px solid #1e293b;
    }
    .footer-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1.5fr;
      gap: 3rem;
      margin-bottom: 3rem;
    }
    .logo-icon {
      width: 32px;
      height: 32px;
      border-radius: var(--radius-sm);
      background: var(--primary);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .logo-icon span { font-size: 20px; }
    .brand-title {
      font-size: 1.35rem;
      font-weight: 800;
      color: #ffffff;
    }
    .brand-highlight { color: #4ade80; }
    .footer-desc {
      font-size: 0.875rem;
      line-height: 1.6;
      max-width: 340px;
    }
    .gov-badge {
      background: rgba(34, 197, 94, 0.1);
      border: 1px solid rgba(34, 197, 94, 0.2);
      padding: 0.35rem 0.75rem;
      border-radius: var(--radius-full);
      font-size: 0.75rem;
      color: #86efac;
      display: inline-flex;
    }
    .gov-badge span:first-child { font-size: 16px; }
    .footer-heading {
      color: #ffffff;
      font-size: 1rem;
      font-weight: 700;
      margin-bottom: 1.25rem;
    }
    .footer-links {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.625rem;
    }
    .footer-links a {
      color: #94a3b8;
      font-size: 0.875rem;
      transition: color 0.2s ease;
    }
    .footer-links a:hover {
      color: #4ade80;
    }
    .help-number {
      font-size: 1.25rem;
      font-weight: 800;
      color: #fbbf24;
      margin-bottom: 0.25rem;
    }
    .help-sub { font-size: 0.75rem; }
    .footer-bottom {
      border-top: 1px solid #1e293b;
      padding-top: 1.5rem;
      color: #64748b;
    }
    .footer-bottom a { color: #64748b; }
    .footer-bottom a:hover { color: #94a3b8; }
    @media (max-width: 1024px) {
      .footer-grid {
        grid-template-columns: 1fr 1fr;
        gap: 2rem;
      }
    }
    @media (max-width: 640px) {
      .footer-grid {
        grid-template-columns: 1fr;
      }
      .footer-bottom {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
      }
    }
  `]
})
export class FooterComponent {}
