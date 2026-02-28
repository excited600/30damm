import { View, StyleSheet } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";

interface ScreenWrapperProps {
  children: React.ReactNode;
  padded?: boolean;
}

export function ScreenWrapper({ children, padded = true }: ScreenWrapperProps) {
  const insets = useSafeAreaInsets();

  return (
    <View
      style={[
        styles.container,
        {
          paddingTop: insets.top,
          paddingBottom: insets.bottom,
          paddingHorizontal: padded ? spacing.md : 0,
        },
      ]}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
});
