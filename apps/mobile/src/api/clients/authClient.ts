import apiClient from "../client";
import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
} from "../types/auth";

export const authClient = {
  signup(request: SignupRequest): Promise<SignupResponse> {
    return apiClient
      .post<SignupResponse>("/api/v1/users/signup", request)
      .then((res) => res.data);
  },

  login(request: LoginRequest): Promise<LoginResponse> {
    return apiClient
      .post<LoginResponse>("/api/v1/users/login", request)
      .then((res) => res.data);
  },

  deleteUser(): Promise<void> {
    return apiClient.delete("/api/v1/users/me").then(() => undefined);
  },
};
