import { View, StyleSheet } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { Button } from "./Button";

interface BottomCTAProps {
  label: string;
  onPress?: () => void;
  disabled?: boolean;
  color?: string;
  labelColor?: string;
}

export function BottomCTA({ label, onPress, disabled, color, labelColor }: BottomCTAProps) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingBottom: Math.max(insets.bottom, spacing.md) }]}>
      <Button label={label} onPress={onPress} disabled={disabled} color={color} labelColor={labelColor} style={styles.button} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: spacing.md,
    paddingTop: spacing.md,
    backgroundColor: colors.background,
  },
  button: {
    width: "100%",
  },
});
