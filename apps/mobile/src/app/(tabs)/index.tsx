import { View, Text, StyleSheet } from "react-native";
import { colors } from "@/shared/constants/colors";
import { typography } from "@/shared/constants/typography";

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>서티담</Text>
      <Text style={styles.subtitle}>모임을 시작해보세요</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.background,
  },
  title: {
    ...typography.heading.lg,
    color: colors.text.primary,
    marginBottom: 8,
  },
  subtitle: {
    ...typography.body.lg,
    color: colors.text.secondary,
  },
});
