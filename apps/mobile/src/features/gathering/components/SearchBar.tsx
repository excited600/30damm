import { View, TextInput, StyleSheet, Pressable, Text } from "react-native";
import { useState } from "react";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { typography } from "@/shared/constants/typography";

interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  onClear?: () => void;
}

export function SearchBar({
  value,
  onChangeText,
  placeholder = "검색",
  onClear,
}: SearchBarProps) {
  const [focused, setFocused] = useState(false);

  return (
    <View style={[styles.container, focused && styles.containerFocused]}>
      <Text style={styles.icon}>🔍</Text>
      <TextInput
        style={styles.input}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.text.tertiary}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
      />
      {value.length > 0 && (
        <Pressable onPress={onClear} style={styles.clearButton}>
          <Text style={styles.clearText}>✕</Text>
        </Pressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: 44,
    borderRadius: 10,
    backgroundColor: colors.surface,
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: spacing.sm,
    gap: spacing.sm,
  },
  containerFocused: {
    borderWidth: 1,
    borderColor: colors.primary,
  },
  icon: {
    fontSize: 14,
  },
  input: {
    flex: 1,
    ...typography.body.md,
    color: colors.text.primary,
    padding: 0,
  },
  clearButton: {
    width: 20,
    height: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  clearText: {
    ...typography.body.sm,
    color: colors.text.tertiary,
  },
});
