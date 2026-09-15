import { Injectable, signal } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly applicationUrl = window.location.origin + '/';
  private readonly keycloak = new Keycloak({
    url: 'http://localhost:8180',
    realm: 'rag',
    clientId: 'rag-client',
  });

  readonly ready = signal(false);
  readonly authenticated = signal(false);
  readonly profile = signal<KeycloakProfile | null>(null);
  readonly roles = signal<string[]>([]);

  async initialize(): Promise<void> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
        redirectUri: this.applicationUrl,
      });
      this.authenticated.set(authenticated);
      this.roles.set(this.keycloak.realmAccess?.roles ?? []);
      if (authenticated) {
        this.profile.set(await this.keycloak.loadUserProfile());
        window.setInterval(() => void this.keycloak.updateToken(30), 20_000);
      }
    } finally {
      this.cleanAuthenticationCallback();
      this.ready.set(true);
    }
  }

  isAdmin(): boolean {
    return this.roles().some((role) => role.toUpperCase() === 'ADMIN');
  }

  async token(): Promise<string> {
    await this.keycloak.updateToken(30);
    return this.keycloak.token ?? '';
  }

  login(locale = 'fr'): void {
    void this.keycloak.login({ redirectUri: this.applicationUrl, locale });
  }

  register(locale = 'fr'): void {
    void this.keycloak.register({ redirectUri: this.applicationUrl, locale });
  }

  logout(): void {
    void this.keycloak.logout({ redirectUri: this.applicationUrl });
  }

  private cleanAuthenticationCallback(): void {
    if (/(^|[&#])(code|state|session_state|error|iss)=/.test(window.location.hash)) {
      window.history.replaceState({}, document.title, this.applicationUrl);
    }
  }

  get displayName(): string {
    const profile = this.profile();
    return profile?.firstName || profile?.username || 'Utilisateur';
  }
}
