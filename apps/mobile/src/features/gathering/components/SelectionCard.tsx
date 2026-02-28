import { Text, StyleSheet, Pressable } from "react-native";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { typography } from "@/shared/constants/typography";

interface SelectionCardProps {
  label: string;
  selected?: boolean;
  onPress?: () => void;
}

export function SelectionCard({ label, selected = false, onPress }: SelectionCardProps) {
  return (
    <Pressable
      onPress={onPress}
      style={[styles.container, selected && styles.selected]}
    >
      <Text style={[styles.label, selected && styles.labelSelected]}>
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    height: 72,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.background,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  selected: {
    borderColor: colors.primary,
    backgroundColor: colors.background,
  },
  label: {
    ...typography.body.lg,
    color: colors.text.secondary,
  },
  labelSelected: {
    color: colors.text.primary,
  },
});
