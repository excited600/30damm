import { View, Text, StyleSheet, Pressable } from "react-native";
import { useRouter } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { Button } from "@/shared/components/ui/Button";

export default function WelcomeScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      <View style={styles.spacer} />
      <View style={styles.centerContent}>
        <View style={styles.logo} />
        <Text style={styles.title}>
          <Text style={styles.titleWhite}>서티</Text>
          <Text style={styles.titleAccent}>담</Text>
        </Text>
        <Text style={styles.subtitle}>30대, 우리끼리 모여요</Text>
      </View>
      <View style={styles.spacer} />
      <View style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, spacing.md) }]}>
        <Button
          label="시작하기"
          onPress={() => router.push("/(auth)/register")}
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
        <Pressable onPress={() => router.push("/(auth)/login")}>
          <Text style={styles.loginText}>이미 계정이 있나요? 로그인</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 20,
    alignItems: "center",
  },
  spacer: {
    flex: 1,
  },
  centerContent: {
    alignItems: "center",
    justifyContent: "center",
    gap: 12,
  },
  logo: {
    width: 118,
    height: 118,
    backgroundColor: "#D9D9D9",
  },
  title: {
    fontSize: 32,
    fontWeight: "400",
    textAlign: "center",
  },
  titleWhite: {
    color: colors.text.primary,
  },
  titleAccent: {
    color: colors.accent.primary,
  },
  subtitle: {
    fontSize: 16,
    fontWeight: "400",
    color: colors.text.primary,
  },
  bottomCTA: {
    width: "100%",
    paddingVertical: 16,
    alignItems: "center",
    gap: 12,
  },
  button: {
    width: "100%",
  },
  loginText: {
    fontSize: 12,
    lineHeight: 16,
    fontWeight: "400",
    color: colors.text.secondary,
    textAlign: "center",
  },
});
