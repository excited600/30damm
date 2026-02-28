import { Pressable, Text, StyleSheet, ViewStyle } from "react-native";
import { colors } from "@/shared/constants/colors";
import { typography } from "@/shared/constants/typography";

interface ButtonProps {
  label: string;
  onPress?: () => void;
  disabled?: boolean;
  color?: string;
  labelColor?: string;
  style?: ViewStyle;
}

export function Button({ label, onPress, disabled = false, color, labelColor, style }: ButtonProps) {
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        styles.container,
        color ? { backgroundColor: color } : undefined,
        disabled && styles.disabled,
        pressed && !disabled && styles.pressed,
        style,
      ]}
    >
      <Text style={[styles.label, labelColor ? { color: labelColor } : undefined, disabled && styles.labelDisabled]}>
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.primary,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 24,
    alignItems: "center",
    justifyContent: "center",
  },
  disabled: {
    backgroundColor: colors.disabled,
  },
  pressed: {
    opacity: 0.8,
  },
  label: {
    ...typography.button.lg,
    color: colors.white,
  },
  labelDisabled: {
    color: colors.text.tertiary,
  },
});
