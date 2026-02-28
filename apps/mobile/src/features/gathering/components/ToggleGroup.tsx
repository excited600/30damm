import { View, Text, StyleSheet, Pressable } from "react-native";
import { colors } from "@/shared/constants/colors";
import { typography } from "@/shared/constants/typography";

interface ToggleGroupProps {
  leftLabel: string;
  rightLabel: string;
  selected: "left" | "right";
  onSelect: (value: "left" | "right") => void;
}

export function ToggleGroup({ leftLabel, rightLabel, selected, onSelect }: ToggleGroupProps) {
  return (
    <View style={styles.container}>
      <Pressable
        style={[styles.option, selected === "left" && styles.optionSelected]}
        onPress={() => onSelect("left")}
      >
        <Text style={[styles.label, selected === "left" && styles.labelSelected]}>
          {leftLabel}
        </Text>
      </Pressable>
      <Pressable
        style={[styles.option, selected === "right" && styles.optionSelected]}
        onPress={() => onSelect("right")}
      >
        <Text style={[styles.label, selected === "right" && styles.labelSelected]}>
          {rightLabel}
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    height: 52,
    borderRadius: 12,
    backgroundColor: colors.surface,
    padding: 4,
  },
  option: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 10,
  },
  optionSelected: {
    backgroundColor: colors.primary,
  },
  label: {
    ...typography.label.md,
    color: colors.text.secondary,
  },
  labelSelected: {
    color: colors.white,
  },
});
