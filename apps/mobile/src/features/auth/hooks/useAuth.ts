import { useMutation } from "@tanstack/react-query";
import { authClient } from "@/api/clients/authClient";
import { useAuthStore } from "@/store/useAuthStore";
import type { LoginRequest, SignupRequest } from "@/api/types/auth";

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth);

  return useMutation({
    mutationFn: (request: LoginRequest) => authClient.login(request),
    onSuccess: (data) => {
      setAuth(data.accessToken, data.refreshToken, data.userUuid);
    },
  });
}

export function useSignup() {
  const setAuth = useAuthStore((state) => state.setAuth);

  return useMutation({
    mutationFn: (request: SignupRequest) => authClient.signup(request),
    onSuccess: (data) => {
      setAuth(data.accessToken, data.refreshToken, data.userUuid);
    },
  });
}

export function useDeleteUser() {
  const logout = useAuthStore((state) => state.logout);

  return useMutation({
    mutationFn: () => authClient.deleteUser(),
    onSuccess: () => {
      logout();
    },
  });
}
