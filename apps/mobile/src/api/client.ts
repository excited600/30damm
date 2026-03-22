import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import Constants from "expo-constants";
import { useAuthStore } from "@/store/useAuthStore";
import type { RefreshResponse } from "./types/auth";

const getApiUrl = (): string => {
  const extra =
    Constants.expoConfig?.extra ??
    (Constants.manifest2 as any)?.extra?.expoClient?.extra ??
    (Constants.manifest as any)?.extra;
  return extra?.apiUrl ?? "http://localhost:8080";
};

const resolvedApiUrl = getApiUrl();

const apiClient = axios.create({
  baseURL: resolvedApiUrl,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (__DEV__) {
    console.log(`[API] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config.data ?? "");
  }
  return config;
});

let refreshPromise: Promise<RefreshResponse> | null = null;

apiClient.interceptors.response.use(
  (response) => {
    if (__DEV__) {
      console.log(`[API] ${response.status} ${response.config.url}`);
    }
    return response;
  },
  async (error: AxiosError) => {
    if (__DEV__) {
      console.error(`[API ERROR] ${error.config?.url}`, error.response?.status, error.response?.data ?? error.message);
    }

    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    const { refreshToken } = useAuthStore.getState();
    if (!refreshToken) {
      useAuthStore.getState().logout();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = axios
          .post<RefreshResponse>(`${resolvedApiUrl}/api/v1/users/refresh`, {
            refreshToken,
          })
          .then((res) => res.data);
      }

      const data = await refreshPromise;
      useAuthStore.getState().setTokens(data.accessToken, data.refreshToken);

      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
      return await apiClient(originalRequest);
    } catch {
      useAuthStore.getState().logout();
      return Promise.reject(error);
    } finally {
      refreshPromise = null;
    }
  },
);

export default apiClient;
