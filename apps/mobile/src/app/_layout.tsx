import { Slot, SplashScreen } from "expo-router";
import { useFonts } from "expo-font";
import { useEffect } from "react";
import { AppProviders } from "@/providers/AppProviders";

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [loaded] = useFonts({});

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
    }
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
