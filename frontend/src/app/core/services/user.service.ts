import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserRegistrationRequest, UserStatistics } from '../models/user.model';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrls.user;

  constructor(private http: HttpClient) {}

  registerUser(request: UserRegistrationRequest): Observable<ApiResponse<User>> {
    return this.http.post<ApiResponse<User>>(`${this.apiUrl}/register`, request, { withCredentials: true });
  }

  getUserProfile(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`, { withCredentials: true });
  }

  updateUserProfile(userId: string, user: Partial<User>): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.apiUrl}/${userId}`, user, { withCredentials: true });
  }

  getAllUsers(page: number = 0, size: number = 20): Observable<PagedResponse<User>> {
    return this.http.get<PagedResponse<User>>(`${this.apiUrl}?page=${page}&size=${size}`, { withCredentials: true });
  }

  searchUsers(searchTerm: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/search?q=${searchTerm}`, { withCredentials: true });
  }

  deactivateUser(userId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${userId}`, { withCredentials: true });
  }

  getUserStatistics(): Observable<UserStatistics> {
    return this.http.get<UserStatistics>(`${this.apiUrl}/statistics`, { withCredentials: true });
  }
}
