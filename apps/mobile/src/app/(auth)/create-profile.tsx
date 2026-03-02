import {
  View,
  Text,
  TextInput,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Pressable,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { useAuthStore } from "@/store/useAuthStore";

export default function CreateProfileScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const setToken = useAuthStore((s) => s.setToken);
  const [nickname, setNickname] = useState("");

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
            <Text style={styles.closeIcon}>✕</Text>
          </Pressable>
          <Text style={styles.headerTitle}>프로필 생성</Text>
          <View style={styles.headerSpacer} />
        </View>

        {/* Notice */}
        <View style={styles.noticeSection}>
          <Text style={styles.noticeTitle}>프로필을 설정해주세요</Text>
          <Text style={styles.noticeSubtitle}>회원가입의 마지막 절차예요</Text>
        </View>

        <View style={styles.spacer} />

        {/* Profile Image */}
        <View style={styles.profileImageContainer}>
          <View style={styles.profileImage}>
            <Text style={styles.profileIcon}>👤</Text>
          </View>
          <View style={styles.cameraIcon}>
            <Text style={styles.cameraEmoji}>📷</Text>
          </View>
        </View>

        {/* Nickname Input */}
        <View style={styles.inputContainer}>
          <TextInput
            style={styles.input}
            placeholder="닉네임을 입력해주세요"
            placeholderTextColor="#6B6B6B"
            value={nickname}
            onChangeText={setNickname}
            autoFocus
          />
        </View>

        <View style={styles.spacer} />

        {/* CTA Button */}
        <Button
          label="시작하기"
          onPress={() => {
            setToken("mock-token");
            router.replace("/(tabs)");
          }}
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
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
    gap: 10,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 12,
  },
  closeIcon: {
    fontSize: 18,
    color: colors.text.primary,
    width: 24,
    height: 24,
    textAlign: "center",
    lineHeight: 24,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "700",
    color: colors.text.primary,
    textAlign: "center",
  },
  headerSpacer: {
    width: 24,
    height: 24,
  },
  noticeSection: {
    gap: 4,
  },
  noticeTitle: {
    fontSize: 20,
    fontWeight: "700",
    color: colors.text.primary,
  },
  noticeSubtitle: {
    fontSize: 18,
    fontWeight: "600",
    color: colors.text.primary,
  },
  spacer: {
    flex: 1,
  },
  profileImageContainer: {
    alignSelf: "center",
    width: 155,
    height: 149,
    position: "relative",
  },
  profileImage: {
    width: 155,
    height: 149,
    borderRadius: 77,
    backgroundColor: "#2C2C2C",
    alignItems: "center",
    justifyContent: "center",
  },
  profileIcon: {
    fontSize: 48,
    opacity: 0.5,
  },
  cameraIcon: {
    position: "absolute",
    bottom: 0,
    right: 0,
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: "#2C2C2C",
    alignItems: "center",
    justifyContent: "center",
  },
  cameraEmoji: {
    fontSize: 18,
  },
  inputContainer: {
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
  button: {
    width: "100%",
  },
});
