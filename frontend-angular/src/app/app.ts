import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { ChatMessage, Conversation, RagApiService } from './rag-api.service';
import { Language, translations } from './i18n';

type View = 'chat' | 'sources';
type StoredSource = { url: string; chunks: number; indexedAt: string };

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  host: { '[attr.lang]': 'language()', '[attr.dir]': 'direction()' },
})
export class App implements OnInit {
  readonly view = signal<View>('chat');
  readonly sidebarOpen = signal(false);
  readonly conversations = signal<Conversation[]>([]);
  readonly activeConversation = signal<Conversation | null>(null);
  readonly messages = signal<ChatMessage[]>([]);
  readonly sending = signal(false);
  readonly error = signal('');
  readonly sources = signal<StoredSource[]>([]);
  readonly ingesting = signal(false);
  readonly ingestionSuccess = signal('');
  readonly conversationToDelete = signal<Conversation | null>(null);
  readonly sourceToDelete = signal<StoredSource | null>(null);
  readonly language = signal<Language>(localStorage.getItem('orange-language') === 'en' ? 'en' : localStorage.getItem('orange-language') === 'ar' ? 'ar' : 'fr');
  readonly direction = computed(() => this.language() === 'ar' ? 'rtl' : 'ltr');
  question = '';
  sourceUrl = '';

  readonly initials = computed(() => this.auth.displayName.slice(0, 2).toUpperCase());

  constructor(readonly auth: AuthService, private readonly api: RagApiService) {}

  async ngOnInit(): Promise<void> {
    this.setLanguage(this.language());
    await this.auth.initialize();
    this.restoreLocalData();
    if (this.auth.isAdmin()) {
      try {
        this.sources.set(await this.api.sources());
        this.persistSources();
      } catch { /* Conserver la copie locale si le serveur est indisponible. */ }
    } else {
      this.view.set('chat');
    }
  }

  setLanguage(language: Language): void {
    this.language.set(language);
    localStorage.setItem('orange-language', language);
    document.documentElement.lang = language;
    document.documentElement.dir = language === 'ar' ? 'rtl' : 'ltr';
  }

  changeLanguage(event: Event): void {
    this.setLanguage((event.target as HTMLSelectElement).value as Language);
  }

  t(key: string): string { return translations[this.language()][key] ?? key; }

  setView(view: View): void {
    if (view === 'sources' && !this.auth.isAdmin()) return;
    this.view.set(view);
    this.sidebarOpen.set(false);
    this.error.set('');
  }

  async newConversation(): Promise<void> {
    this.error.set('');

    if (this.activeConversation() && this.messages().length === 0) {
      this.question = '';
      this.setView('chat');
      return;
    }

    try {
      const conversation = await this.api.createConversation();
      this.conversations.update((items) => [conversation, ...items]);
      this.activeConversation.set(conversation);
      this.messages.set([]);
      this.persistConversations();
      this.setView('chat');
    } catch { this.error.set(this.t('createError')); }
  }

  async openConversation(conversation: Conversation): Promise<void> {
    this.activeConversation.set(conversation);
    this.sidebarOpen.set(false);
    this.error.set('');
    try { this.messages.set(await this.api.history(conversation.id)); }
    catch { this.error.set(this.t('historyError')); }
  }

  async confirmConversationDeletion(): Promise<void> {
    const conversation = this.conversationToDelete();
    if (!conversation) return;
    this.error.set('');
    try {
      await this.api.deleteConversation(conversation.id);
      this.conversations.update((items) => items.filter((item) => item.id !== conversation.id));
      if (this.activeConversation()?.id === conversation.id) {
        this.activeConversation.set(null);
        this.messages.set([]);
      }
      this.persistConversations();
      this.conversationToDelete.set(null);
    } catch { this.error.set(this.t('deleteError')); }
  }

  formatAnswer(content: string): string {
    const escaped = content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const inline = (value: string) => value
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/`(.+?)`/g, '<code>$1</code>');
    const lines = escaped.split(/\r?\n/);
    const output: string[] = [];
    for (let index = 0; index < lines.length; index++) {
      const line = lines[index].trim();
      if (!line) continue;
      if (line.includes('|') && index + 1 < lines.length && /^\s*\|?[\s:|-]+\|\s*$/.test(lines[index + 1])) {
        const headers = line.replace(/^\||\|$/g, '').split('|').map((cell) => `<th>${inline(cell.trim())}</th>`).join('');
        const rows: string[] = [];
        index += 2;
        while (index < lines.length && lines[index].includes('|')) {
          const cells = lines[index].trim().replace(/^\||\|$/g, '').split('|').map((cell) => `<td>${inline(cell.trim())}</td>`).join('');
          rows.push(`<tr>${cells}</tr>`);
          index++;
        }
        index--;
        output.push(`<div class="answer-table"><table><thead><tr>${headers}</tr></thead><tbody>${rows.join('')}</tbody></table></div>`);
      } else if (/^[-*]\s+/.test(line)) {
        const items: string[] = [];
        while (index < lines.length && /^\s*[-*]\s+/.test(lines[index])) {
          items.push(`<li>${inline(lines[index].replace(/^\s*[-*]\s+/, ''))}</li>`);
          index++;
        }
        index--;
        output.push(`<ul>${items.join('')}</ul>`);
      } else if (/^#{1,3}\s+/.test(line)) {
        output.push(`<h3>${inline(line.replace(/^#{1,3}\s+/, ''))}</h3>`);
      } else {
        output.push(`<p>${inline(line)}</p>`);
      }
    }
    return output.join('');
  }

  async sendQuestion(preset?: string): Promise<void> {
    const text = (preset ?? this.question).trim();
    if (!text || this.sending()) return;
    const isFirstQuestion = !this.messages().some((message) => message.role === 'USER');
    this.question = '';
    this.sending.set(true);
    this.error.set('');
    try {
      let conversation = this.activeConversation();
      if (!conversation) {
        conversation = await this.api.createConversation();
        this.conversations.update((items) => [conversation!, ...items]);
        this.activeConversation.set(conversation);
        this.persistConversations();
      }
      if (isFirstQuestion) {
        conversation = { ...conversation, title: this.titleFromQuestion(text) };
        this.activeConversation.set(conversation);
        this.conversations.update((items) => items.map((item) => item.id === conversation!.id ? conversation! : item));
        this.persistConversations();
      }
      const assistantId = crypto.randomUUID();
      this.messages.update((items) => [...items,
        { id: crypto.randomUUID(), role: 'USER', content: text },
        { id: assistantId, role: 'ASSISTANT', content: '' },
      ]);
      await this.api.streamAnswer(conversation.id, text, (chunk) => {
        this.messages.update((items) => items.map((item) => item.id === assistantId ? { ...item, content: item.content + chunk } : item));
      });
    } catch { this.error.set(this.t('chatError')); }
    finally { this.sending.set(false); }
  }

  onComposerKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      void this.sendQuestion();
    }
  }

  async addSource(): Promise<void> {
    const url = this.sourceUrl.trim();
    if (!url || this.ingesting()) return;
    this.ingesting.set(true);
    this.error.set('');
    this.ingestionSuccess.set('');
    try {
      const result = await this.api.ingest(url);
      this.sources.update((items) => [{ url: result.url, chunks: result.chunks, indexedAt: new Date().toISOString() }, ...items.filter((item) => item.url !== result.url)]);
      this.persistSources();
      this.sourceUrl = '';
      this.ingestionSuccess.set(this.t('pageAdded'));
    } catch { this.error.set(this.t('pageError')); }
    finally { this.ingesting.set(false); }
  }

  async confirmSourceDeletion(): Promise<void> {
    const source = this.sourceToDelete();
    if (!source) return;
    this.error.set('');
    try {
      await this.api.deleteSource(source.url);
      this.sources.update((items) => items.filter((item) => item.url !== source.url));
      this.persistSources();
      this.sourceToDelete.set(null);
    } catch { this.error.set(this.t('sourceDeleteError')); }
  }

  private storageKey(type: string): string {
    return `rag-${this.auth.profile()?.username ?? 'user'}-${type}`;
  }
  private titleFromQuestion(question: string): string {
    const normalized = question.replace(/\s+/g, ' ').trim();
    if (normalized.length <= 52) return normalized;
    const shortened = normalized.slice(0, 52);
    const lastSpace = shortened.lastIndexOf(' ');
    return `${shortened.slice(0, lastSpace > 30 ? lastSpace : 52).trim()}…`;
  }
  private restoreLocalData(): void {
    try {
      this.conversations.set(JSON.parse(localStorage.getItem(this.storageKey('conversations')) ?? '[]'));
      this.sources.set(JSON.parse(localStorage.getItem(this.storageKey('sources')) ?? '[]'));
    } catch { this.conversations.set([]); this.sources.set([]); }
  }
  private persistConversations(): void { localStorage.setItem(this.storageKey('conversations'), JSON.stringify(this.conversations())); }
  private persistSources(): void { localStorage.setItem(this.storageKey('sources'), JSON.stringify(this.sources())); }
}
