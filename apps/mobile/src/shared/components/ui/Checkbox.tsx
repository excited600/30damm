import { Pressable, View, Text, StyleSheet } from "react-native";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";

interface CheckboxProps {
  label: string;
  checked: boolean;
  onPress: () => void;
}

export function Checkbox({ label, checked, onPress }: CheckboxProps) {
  return (
    <Pressable style={styles.container} onPress={onPress}>
      <View style={[styles.box, checked && styles.boxChecked]}>
        {checked && <Ionicons name="checkmark" size={18} color={colors.text.primary} />}
      </View>
      <Text style={styles.label}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  box: {
    width: 26,
    height: 26,
    borderRadius: 5,
    borderWidth: 2,
    borderColor: colors.text.primary,
    alignItems: "center",
    justifyContent: "center",
  },
  boxChecked: {
    backgroundColor: colors.accent.primary,
    borderColor: colors.accent.primary,
  },
  label: {
    fontSize: 16,
    fontWeight: "400",
    lineHeight: 24,
    color: colors.text.primary,
  },
});
