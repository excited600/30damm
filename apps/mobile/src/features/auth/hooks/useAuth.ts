import { useMutation } from "@tanstack/react-query";
import apiClient from "@/api/client";
import { useAuthStore } from "@/store/useAuthStore";
import type { LoginRequest, LoginResponse } from "../types";

async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>("/api/auth/login", request);
  return data;
}

export function useLogin() {
  const setToken = useAuthStore((state) => state.setToken);

  return useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setToken(data.accessToken);
    },
  });
}
