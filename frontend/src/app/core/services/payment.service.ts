import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, CheckoutSessionRequest, CheckoutSessionResponse, TransactionStatistics } from '../models/payment.model';
import { ApiResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = environment.apiUrls.payment;

  constructor(private http: HttpClient) {}

  createCheckoutSession(request: CheckoutSessionRequest): Observable<CheckoutSessionResponse> {
    return this.http.post<CheckoutSessionResponse>(`${this.apiUrl}/create-checkout-session`, request, { withCredentials: true });
  }

  getTransactionById(id: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/transactions/${id}`, { withCredentials: true });
  }

  getMyTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/my-transactions`, { withCredentials: true });
  }

  getAllTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/transactions`, { withCredentials: true });
  }

  getTransactionStatistics(): Observable<TransactionStatistics> {
    return this.http.get<TransactionStatistics>(`${this.apiUrl}/statistics`, { withCredentials: true });
  }
}
