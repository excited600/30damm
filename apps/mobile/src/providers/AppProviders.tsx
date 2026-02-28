import { SafeAreaProvider } from "react-native-safe-area-context";
import { QueryProvider } from "./QueryProvider";

interface AppProvidersProps {
  children: React.ReactNode;
}

export function AppProviders({ children }: AppProvidersProps) {
  return (
    <SafeAreaProvider>
      <QueryProvider>{children}</QueryProvider>
    </SafeAreaProvider>
  );
}
