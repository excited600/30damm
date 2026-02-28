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
  return config;
});

export default apiClient;
