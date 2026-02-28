import { Redirect } from "expo-router";
import { useAuthStore } from "@/store/useAuthStore";

export default function Index() {
  const token = useAuthStore((s) => s.token);

  if (!token) {
    return <Redirect href="/welcome" />;
  }

  return <Redirect href="/(tabs)" />;
}
