import axios from "axios";
import Constants from "expo-constants";
import { useAuthStore } from "@/store/useAuthStore";

const apiClient = axios.create({
  baseURL: Constants.expoConfig?.extra?.apiUrl ?? "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  console.log(`[API] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config.data ?? "");
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API] ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error(`[API ERROR] ${error.config?.url}`, error.response?.status, error.response?.data ?? error.message);
    return Promise.reject(error);
  },
);

export default apiClient;
