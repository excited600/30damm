import { View, Text, TextInput, StyleSheet, TextInputProps } from "react-native";
import { colors } from "@/shared/constants/colors";

interface InputProps extends TextInputProps {
  label: string;
}

export function Input({ label, ...textInputProps }: InputProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.fieldGroup}>
        <View style={styles.field}>
          <TextInput
            style={styles.input}
            placeholderTextColor="#6B6B6B"
            {...textInputProps}
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    gap: 8,
  },
  label: {
    fontSize: 14,
    fontWeight: "400",
    lineHeight: 20,
    color: colors.text.primary,
  },
  fieldGroup: {
    width: "100%",
  },
  field: {
    borderWidth: 2,
    borderColor: "#2C2C2C",
    borderRadius: 8,
    padding: 16,
    width: "100%",
  },
  input: {
    fontSize: 14,
    fontWeight: "700",
    lineHeight: 20,
    color: colors.text.primary,
    padding: 0,
  },
});
