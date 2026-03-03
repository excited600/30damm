import { Slot, SplashScreen } from "expo-router";
import { useFonts } from "expo-font";
import { useEffect } from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import { AppProviders } from "@/providers/AppProviders";
import { colors } from "@/shared/constants/colors";

SplashScreen.preventAutoHideAsync();

export function ErrorBoundary({ error, retry }: { error: Error; retry: () => void }) {
  return (
    <View style={errorStyles.container}>
      <Text style={errorStyles.title}>예기치 않은 오류가 발생했습니다.</Text>
      <Text style={errorStyles.message}>{error.message}</Text>
      <Pressable style={errorStyles.retryButton} onPress={retry}>
        <Text style={errorStyles.retryText}>다시 시도</Text>
      </Pressable>
    </View>
  );
}

const errorStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    alignItems: "center",
    justifyContent: "center",
    padding: 20,
    gap: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: "700",
    color: colors.text.primary,
    textAlign: "center",
  },
  message: {
    fontSize: 14,
    color: colors.text.secondary,
    textAlign: "center",
  },
  retryButton: {
    backgroundColor: colors.accent.primary,
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 24,
  },
  retryText: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.text.primary,
  },
});

export default function RootLayout() {
  const [loaded] = useFonts({});

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync().catch(() => {});
      return;
    }
    const timeout = setTimeout(() => {
      SplashScreen.hideAsync().catch(() => {});
    }, 3000);
    return () => clearTimeout(timeout);
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  return (
    <AppProviders>
      <Slot />
    </AppProviders>
  );
}
