import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadCurrentUser();
  }

  private loadCurrentUser(): void {
    // Load current user from API using cookie authentication
    this.getCurrentUser().subscribe({
      next: (user) => this.currentUserSubject.next(user),
      error: () => this.currentUserSubject.next(null)
    });
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrls.user}/me`, { withCredentials: true });
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ADMIN';
  }

  logout(): Observable<any> {
    return this.http.post(`${environment.apiGatewayUrl}/logout`, {}, { withCredentials: true })
      .pipe(tap(() => this.currentUserSubject.next(null)));
  }

  refreshUser(): void {
    this.loadCurrentUser();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }
}
