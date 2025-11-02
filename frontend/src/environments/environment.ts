export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8903',
  apiUrls: {
    user: 'http://localhost:8081/api/users',
    policy: 'http://localhost:8082/api/policies',
    payment: 'http://localhost:8084/api/payments',
    claims: 'http://localhost:8083/api/claims',
    notification: 'http://localhost:8085/api/notifications'
  }
};
