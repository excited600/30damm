import { ActivityIndicator, View, StyleSheet } from "react-native";
import { colors } from "@/shared/constants/colors";

interface LoadingProps {
  size?: "small" | "large";
}

export function Loading({ size = "large" }: LoadingProps) {
  return (
    <View style={styles.container}>
      <ActivityIndicator size={size} color={colors.primary} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});
