import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import { colors } from "@/shared/constants/colors";
import { Input } from "@/shared/components/ui/Input";
import { Button } from "@/shared/components/ui/Button";
import { useRegisterStore } from "@/store/useRegisterStore";

export default function RegisterScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const setCredentials = useRegisterStore((s) => s.setCredentials);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [error, setError] = useState("");

  const handleNext = () => {
    if (!email.trim() || !password.trim()) {
      setError("이메일과 비밀번호를 입력해주세요.");
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError("올바른 이메일 형식을 입력해주세요.");
      return;
    }
    if (password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }
    if (password.length < 8 || password.length > 20) {
      setError("비밀번호는 8~20자로 입력해주세요.");
      return;
    }
    setError("");
    setCredentials(email, password);
    router.push("/(auth)/create-profile");
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
            onChangeText={(text) => {
              setPassword(text);
              setError("");
            }}
            secureTextEntry
          />
          <Input
            label="비밀번호 확인"
            placeholder="비밀번호를 확인해주세요"
            value={passwordConfirm}
            onChangeText={(text) => {
              setPasswordConfirm(text);
              setError("");
            }}
            secureTextEntry
          />
          {error !== "" && <Text style={styles.errorText}>{error}</Text>}
        </View>
        <Button
          label="다음으로"
          onPress={handleNext}
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
    gap: 15,
  },
  errorText: {
    color: "#FF4444",
    fontSize: 13,
    fontWeight: "500",
  },
  button: {
    width: "100%",
  },
});
