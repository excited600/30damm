import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import { colors } from "@/shared/constants/colors";
import { Input } from "@/shared/components/ui/Input";
import { Button } from "@/shared/components/ui/Button";
import { useAuthStore } from "@/store/useAuthStore";

export default function LoginScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const setToken = useAuthStore((s) => s.setToken);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

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
            onChangeText={setPassword}
            secureTextEntry
          />
        </View>
        <Button
          label="시작하기"
          onPress={() => {
            setToken("mock-token");
            router.replace("/(gathering)/GatheringCardListScreen");
          }}
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
});
