import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView, Alert, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Input } from "@/shared/components/ui/Input";
import { Button } from "@/shared/components/ui/Button";
import { useLogin } from "@/features/auth/hooks/useAuth";

export default function LoginScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const login = useLogin();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = () => {
    if (!email.trim() || !password.trim()) {
      Alert.alert("알림", "이메일과 비밀번호를 입력해주세요.");
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      Alert.alert("알림", "올바른 이메일 형식을 입력해주세요.");
      return;
    }
    login.mutate(
      { email, password },
      {
        onSuccess: () => {
          router.replace("/(tabs)" as any);
        },
        onError: (err) => {
          const message =
            (err as any)?.response?.status === 401
              ? "이메일 또는 비밀번호가 일치하지 않습니다."
              : "로그인에 실패했습니다. 다시 시도해주세요.";
          Alert.alert("오류", message);
        },
      },
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <ScrollView
        contentContainerStyle={[
          styles.container,
          { paddingTop: insets.top, paddingBottom: insets.bottom },
        ]}
        keyboardShouldPersistTaps="handled"
      >
        {/* Header */}
        <View style={styles.header}>
          <Pressable onPress={() => router.back()} hitSlop={8}>
            <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
          </Pressable>
          <View style={styles.headerSpacer} />
        </View>
        <View style={styles.spacer} />
        <Text style={styles.title}>
          <Text style={styles.titleWhite}>서티</Text>
          <Text style={styles.titleAccent}>담</Text>
        </Text>
        <View style={styles.spacer} />
        <View style={styles.formGroup}>
          <Input
            label="이메일"
            placeholder="이메일을 입력해주세요"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <Input
            label="비밀번호"
            placeholder="비밀번호를 입력해주세요"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
        </View>
        <Button
          label={login.isPending ? "로그인 중..." : "시작하기"}
          onPress={handleLogin}
          disabled={login.isPending}
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
        <View style={styles.spacer} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    backgroundColor: colors.background,
  },
  container: {
    flexGrow: 1,
    paddingHorizontal: 20,
    alignItems: "center",
    gap: 24,
  },
  spacer: {
    flex: 1,
  },
  title: {
    fontSize: 24,
    fontWeight: "700",
    lineHeight: 32,
    textAlign: "center",
  },
  titleWhite: {
    color: colors.text.primary,
  },
  titleAccent: {
    color: colors.accent.primary,
  },
  formGroup: {
    width: "100%",
    gap: 12,
  },
  button: {
    width: "100%",
  },
  header: {
    width: "100%",
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 12,
  },
  headerSpacer: {
    width: 24,
    height: 24,
  },
});
