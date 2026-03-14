import axios from "axios";
import Constants from "expo-constants";
import { useAuthStore } from "@/store/useAuthStore";

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

apiClient.interceptors.response.use(
  (response) => {
    if (__DEV__) {
      console.log(`[API] ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error) => {
    if (__DEV__) {
      console.error(`[API ERROR] ${error.config?.url}`, error.response?.status, error.response?.data ?? error.message);
    }

    // 401 Unauthorized → auto-logout
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
    }

    return Promise.reject(error);
  },
);

export default apiClient;
