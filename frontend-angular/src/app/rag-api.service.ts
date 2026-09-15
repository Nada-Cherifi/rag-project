import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

export interface Conversation { id: string; title: string; createdAt: string }
export interface ChatMessage { id: string; role: 'USER' | 'ASSISTANT'; content: string; status?: string; createdAt?: string }
export interface IngestionResult { url: string; chunks: number; status: string }
export interface IndexedSource { url: string; chunks: number; indexedAt: string }

@Injectable({ providedIn: 'root' })
export class RagApiService {
  constructor(private readonly http: HttpClient, private readonly auth: AuthService) {}

  private async headers(json = false): Promise<HttpHeaders> {
    let headers = new HttpHeaders({ Authorization: `Bearer ${await this.auth.token()}` });
    if (json) headers = headers.set('Content-Type', 'application/json');
    return headers;
  }

  async createConversation(): Promise<Conversation> {
    return firstValueFrom(this.http.post<Conversation>('/api/chat', {}, { headers: await this.headers() }));
  }

  async history(id: string): Promise<ChatMessage[]> {
    return firstValueFrom(this.http.get<ChatMessage[]>(`/api/chat/${id}/messages`, { headers: await this.headers() }));
  }

  async deleteConversation(id: string): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`/api/chat/${id}`, { headers: await this.headers() }));
  }

  async streamAnswer(id: string, message: string, onText: (text: string) => void): Promise<void> {
    const response = await fetch(`/api/chat/${id}/messages/stream`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${await this.auth.token()}`, 'Content-Type': 'application/json', Accept: 'text/event-stream' },
      body: JSON.stringify({ message }),
    });
    if (!response.ok || !response.body) throw new Error('Le chatbot est indisponible.');
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split(/\r?\n\r?\n/);
      buffer = events.pop() ?? '';
      for (const event of events) {
        const data = event.split(/\r?\n/).filter((line) => line.startsWith('data:')).map((line) => line.slice(5)).join('\n');
        if (data && data !== '[DONE]') onText(data);
      }
    }
  }

  async ingest(url: string): Promise<IngestionResult> {
    return firstValueFrom(this.http.post<IngestionResult>(`/api/rag/ingestion/orange?url=${encodeURIComponent(url)}`, {}, { headers: await this.headers() }));
  }

  async sources(): Promise<IndexedSource[]> {
    return firstValueFrom(this.http.get<IndexedSource[]>('/api/rag/ingestion/sources', { headers: await this.headers() }));
  }

  async deleteSource(url: string): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`/api/rag/ingestion/orange?url=${encodeURIComponent(url)}`, { headers: await this.headers() }));
  }
}
