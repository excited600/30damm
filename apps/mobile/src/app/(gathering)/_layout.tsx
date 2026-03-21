import { Stack, Redirect } from "expo-router";
import { useAuthStore } from "@/store/useAuthStore";

export default function GatheringLayout() {
  const token = useAuthStore((s) => s.token);

  if (!token) {
    return <Redirect href="/welcome" />;
  }

  return (
    <Stack
      screenOptions={{
        headerShown: false,
      }}
    />
  );
}
