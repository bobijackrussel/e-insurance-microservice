import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Claim, ClaimSubmitRequest, ClaimReviewRequest, ClaimStatistics, ClaimStatus } from '../models/claim.model';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  private apiUrl = environment.apiUrls.claims;

  constructor(private http: HttpClient) {}

  // User Claims
  getMyClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/my-claims`, { withCredentials: true });
  }

  getClaimById(id: string): Observable<Claim> {
    return this.http.get<Claim>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  submitClaim(request: ClaimSubmitRequest): Observable<ApiResponse<Claim>> {
    return this.http.post<ApiResponse<Claim>>(`${this.apiUrl}`, request, { withCredentials: true });
  }

  // Admin Claims Management
  getAllClaims(page: number = 0, size: number = 20): Observable<PagedResponse<Claim>> {
    return this.http.get<PagedResponse<Claim>>(`${this.apiUrl}?page=${page}&size=${size}`, { withCredentials: true });
  }

  getClaimsByStatus(status: ClaimStatus): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/status/${status}`, { withCredentials: true });
  }

  getPendingClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/pending`, { withCredentials: true });
  }

  reviewClaim(claimId: string, request: ClaimReviewRequest): Observable<ApiResponse<Claim>> {
    return this.http.put<ApiResponse<Claim>>(`${this.apiUrl}/${claimId}/review`, request, { withCredentials: true });
  }

  // Statistics
  getClaimStatistics(): Observable<ClaimStatistics> {
    return this.http.get<ClaimStatistics>(`${this.apiUrl}/statistics`, { withCredentials: true });
  }
}
