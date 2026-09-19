import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../core/services/chatbot.service';
import { AuthService } from '../../core/services/auth.service';

interface Message {
  sender: 'user' | 'bot';
  text: string;
  time: Date;
  suggestions?: string[];
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chatbot-page py-8">
      <div class="container max-w-4xl">
        
        <div class="chat-card card">
          
          <!-- Chat Header -->
          <div class="chat-header p-5 border-b border-slate-200 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="bot-avatar">
                <span class="material-symbols-outlined">smart_toy</span>
              </div>
              <div>
                <h1 class="text-base font-bold text-dark">CropDeal AI Agricultural Advisor</h1>
                <div class="text-xs text-emerald-700 font-semibold flex items-center gap-1">
                  <span class="status-dot"></span> Online • Market Intelligence & Advisory
                </div>
              </div>
            </div>
          </div>

          <!-- Message History -->
          <div class="chat-messages p-6">
            <div 
              *ngFor="let msg of messages" 
              class="message-bubble-wrapper"
              [class.user]="msg.sender === 'user'"
            >
              <div class="bubble">
                <div class="bubble-text">{{ msg.text }}</div>
                <div class="bubble-time">{{ msg.time | date:'shortTime' }}</div>
              </div>

              <!-- Quick Action Suggestions -->
              <div *ngIf="msg.suggestions && msg.suggestions.length > 0" class="suggestions-list flex gap-2 mt-2 flex-wrap">
                <button 
                  *ngFor="let s of msg.suggestions" 
                  class="sugg-btn"
                  (click)="sendQuickMessage(s)"
                >
                  {{ s }}
                </button>
              </div>
            </div>

            <div *ngIf="isLoading" class="message-bubble-wrapper">
              <div class="bubble typing">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>

          <!-- Input Bar -->
          <div class="chat-input-bar p-4 border-t border-slate-200 bg-white">
            <div class="flex gap-2">
              <input 
                type="text" 
                [(ngModel)]="userInput" 
                (keyup.enter)="sendMessage()"
                placeholder="Ask about crop prices, seasonal sowing, disease control, or market trends..."
                class="form-control"
              />
              <button 
                class="btn btn-primary"
                [disabled]="isLoading || !userInput.trim()"
                (click)="sendMessage()"
              >
                <span class="material-symbols-outlined">send</span>
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
  `,
  styles: [`
    .chat-card {
      height: 640px;
      display: flex;
      flex-direction: column;
      border-radius: var(--radius-xl);
      overflow: hidden;
    }
    .bot-avatar {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .status-dot {
      width: 8px;
      height: 8px;
      background: var(--success);
      border-radius: 50%;
      display: inline-block;
    }
    .chat-messages {
      flex: 1;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
      background: #f8fafc;
    }
    .message-bubble-wrapper {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      max-width: 80%;
    }
    .message-bubble-wrapper.user {
      align-self: flex-end;
      align-items: flex-end;
    }
    .bubble {
      padding: 0.875rem 1.25rem;
      border-radius: var(--radius-lg);
      background: #ffffff;
      box-shadow: var(--shadow-sm);
      border: 1px solid var(--border-light);
      font-size: 0.9375rem;
      line-height: 1.5;
    }
    .message-bubble-wrapper.user .bubble {
      background: var(--primary);
      color: #ffffff;
      border-color: var(--primary);
    }
    .bubble-time {
      font-size: 0.6875rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
      text-align: right;
    }
    .message-bubble-wrapper.user .bubble-time {
      color: #bbf7d0;
    }
    .sugg-btn {
      background: #ffffff;
      border: 1px solid var(--primary-border);
      color: var(--primary-dark);
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.35rem 0.75rem;
      border-radius: var(--radius-full);
      cursor: pointer;
    }
    .sugg-btn:hover {
      background: var(--primary-subtle);
    }
    .bubble.typing {
      display: flex;
      gap: 4px;
      padding: 0.75rem 1rem;
    }
    .bubble.typing .dot {
      width: 6px;
      height: 6px;
      background: var(--text-muted);
      border-radius: 50%;
      animation: bounce 1.4s infinite ease-in-out;
    }
    .bubble.typing .dot:nth-child(1) { animation-delay: -0.32s; }
    .bubble.typing .dot:nth-child(2) { animation-delay: -0.16s; }
    @keyframes bounce {
      0%, 80%, 100% { transform: scale(0); }
      40% { transform: scale(1); }
    }
  `]
})
export class ChatbotComponent {
  private chatbotService = inject(ChatbotService);
  private authService = inject(AuthService);

  userInput = '';
  isLoading = false;

  messages: Message[] = [
    {
      sender: 'bot',
      text: 'Namaste! I am your CropDeal Agricultural Assistant. I can help you with today\'s Mandi price rates, best harvesting windows, crop disease identification, or fair pricing guidance. How can I help you today?',
      time: new Date(),
      suggestions: [
        'What is today\'s Tomato Mandi price in Tamil Nadu?',
        'How does CropDeal Escrow protection work?',
        'What are the best seasonal vegetables for Rabi season?'
      ]
    }
  ];

  sendMessage(): void {
    const text = this.userInput.trim();
    if (!text || this.isLoading) return;

    this.messages.push({
      sender: 'user',
      text,
      time: new Date()
    });

    this.userInput = '';
    this.isLoading = true;

    const userId = this.authService.getUserId() ?? undefined;
    const role = this.authService.getUserRole() ?? undefined;

    this.chatbotService.askAssistant({
      message: text,
      userId,
      role
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.messages.push({
          sender: 'bot',
          text: res.reply || 'Thank you for your question. You can review official rates on our Mandi Rates portal.',
          time: new Date(),
          suggestions: res.suggestions
        });
      },
      error: () => {
        this.isLoading = false;
        this.messages.push({
          sender: 'bot',
          text: 'I am currently processing high network volume. Please check our Mandi Rates portal or reach out to toll-free 1800-419-CROP.',
          time: new Date()
        });
      }
    });
  }

  sendQuickMessage(text: string): void {
    this.userInput = text;
    this.sendMessage();
  }
}
