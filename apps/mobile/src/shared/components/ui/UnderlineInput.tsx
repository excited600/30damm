import { ReactNode } from "react";
import { View, Text, TextInput, StyleSheet, TextInputProps } from "react-native";
import { colors } from "@/shared/constants/colors";

interface UnderlineInputProps {
  label: string;
  placeholder?: string;
  value: string;
  onChangeText: (text: string) => void;
  maxLength?: number;
  multiline?: boolean;
  error?: string;
  icon?: ReactNode;
  textInputProps?: TextInputProps;
}

export function UnderlineInput({
  label,
  placeholder,
  value,
  onChangeText,
  multiline,
  error,
  icon,
  textInputProps,
}: UnderlineInputProps) {
  return (
    <View style={styles.formField}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <View style={styles.valueRow}>
        {icon && <View style={styles.iconWrapper}>{icon}</View>}
        <TextInput
          style={[styles.fieldInput, icon ? styles.fieldInputWithIcon : undefined]}
          placeholder={placeholder}
          placeholderTextColor={colors.text.tertiary}
          value={value}
          onChangeText={onChangeText}
          multiline={multiline}
          {...textInputProps}
        />
      </View>
      <View style={styles.underline} />
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  formField: {
    gap: 8,
  },
  fieldLabel: {
    fontSize: 14,
    fontWeight: "400",
    lineHeight: 20,
    color: colors.text.primary,
  },
  valueRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  iconWrapper: {
    width: 24,
    height: 24,
    alignItems: "center",
    justifyContent: "center",
  },
  fieldInput: {
    flex: 1,
    fontSize: 14,
    fontWeight: "700",
    color: colors.text.primary,
    paddingVertical: 2,
  },
  fieldInputWithIcon: {
    flex: 1,
  },
  underline: {
    height: 1,
    backgroundColor: colors.surface,
  },
  errorText: {
    fontSize: 12,
    fontWeight: "400",
    color: colors.error,
    marginTop: 2,
  },
});
