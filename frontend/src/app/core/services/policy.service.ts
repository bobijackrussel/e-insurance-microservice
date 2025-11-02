import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PolicyTemplate, CustomerPolicy, PolicyPurchaseRequest, PolicyStatistics, PolicyType } from '../models/policy.model';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  private apiUrl = environment.apiUrls.policy;

  constructor(private http: HttpClient) {}

  // Policy Templates (Marketplace)
  getActivePolicyTemplates(): Observable<PolicyTemplate[]> {
    return this.http.get<PolicyTemplate[]>(`${this.apiUrl}/templates/active`, { withCredentials: true });
  }

  getAllPolicyTemplates(page: number = 0, size: number = 20): Observable<PagedResponse<PolicyTemplate>> {
    return this.http.get<PagedResponse<PolicyTemplate>>(`${this.apiUrl}/templates?page=${page}&size=${size}`, { withCredentials: true });
  }

  getPolicyTemplateById(id: string): Observable<PolicyTemplate> {
    return this.http.get<PolicyTemplate>(`${this.apiUrl}/templates/${id}`, { withCredentials: true });
  }

  getPolicyTemplatesByType(type: PolicyType): Observable<PolicyTemplate[]> {
    return this.http.get<PolicyTemplate[]>(`${this.apiUrl}/templates/type/${type}`, { withCredentials: true });
  }

  createPolicyTemplate(template: Partial<PolicyTemplate>): Observable<ApiResponse<PolicyTemplate>> {
    return this.http.post<ApiResponse<PolicyTemplate>>(`${this.apiUrl}/templates`, template, { withCredentials: true });
  }

  updatePolicyTemplate(id: string, template: Partial<PolicyTemplate>): Observable<ApiResponse<PolicyTemplate>> {
    return this.http.put<ApiResponse<PolicyTemplate>>(`${this.apiUrl}/templates/${id}`, template, { withCredentials: true });
  }

  deletePolicyTemplate(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/templates/${id}`, { withCredentials: true });
  }

  // Customer Policies
  getMyPolicies(): Observable<CustomerPolicy[]> {
    return this.http.get<CustomerPolicy[]>(`${this.apiUrl}/my-policies`, { withCredentials: true });
  }

  getPolicyById(id: string): Observable<CustomerPolicy> {
    return this.http.get<CustomerPolicy>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  initiatePolicyPurchase(request: PolicyPurchaseRequest): Observable<ApiResponse<CustomerPolicy>> {
    return this.http.post<ApiResponse<CustomerPolicy>>(`${this.apiUrl}/purchase/initiate`, request, { withCredentials: true });
  }

  cancelPolicy(policyId: string): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${policyId}/cancel`, {}, { withCredentials: true });
  }

  // Statistics
  getPolicyStatistics(): Observable<PolicyStatistics> {
    return this.http.get<PolicyStatistics>(`${this.apiUrl}/statistics`, { withCredentials: true });
  }
}
