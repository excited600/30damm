import {
  View,
  Text,
  TextInput,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Pressable,
  Alert,
  useWindowDimensions,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { useRegisterStore } from "@/store/useRegisterStore";
import { useSignup } from "@/features/auth/hooks/useAuth";

export default function CreateProfileScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { email, password, reset } = useRegisterStore();
  const signup = useSignup();
  const [nickname, setNickname] = useState("");
  const { height } = useWindowDimensions();

  const handleSignup = () => {
    if (!email || !password) {
      Alert.alert("오류", "처음부터 다시 시도해주세요.");
      router.replace("/(auth)/register");
      return;
    }
    if (nickname.length < 2 || nickname.length > 10) {
      Alert.alert("알림", "닉네임은 2~10자로 입력해주세요.");
      return;
    }
    signup.mutate(
      { email, password, nickname },
      {
        onSuccess: () => {
          reset();
          router.replace("/(gathering)/GatheringCardListScreen");
        },
        onError: (err) => {
          const message =
            (err as any)?.response?.status === 409
              ? "이미 사용 중인 이메일입니다."
              : "회원가입에 실패했습니다. 다시 시도해주세요.";
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
          <Text style={styles.headerTitle}>프로필 생성</Text>
          <View style={styles.headerSpacer} />
        </View>

        {/* Notice */}
        <View style={styles.noticeSection}>
          <Text style={styles.noticeTitle}>프로필을 설정해주세요</Text>
          <Text style={styles.noticeSubtitle}>회원가입의 마지막 절차예요</Text>
        </View>

        <View style={{ height: height * 0.08 }} />

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
          label={signup.isPending ? "가입 중..." : "시작하기"}
          onPress={handleSignup}
          disabled={signup.isPending}
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
  genderSection: {
    width: "100%",
    gap: 8,
  },
  genderLabel: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.text.secondary,
  },
  genderRow: {
    flexDirection: "row",
    gap: 12,
  },
  genderOption: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 12,
    borderRadius: 8,
    borderWidth: 2,
    borderColor: "#2C2C2C",
  },
  genderOptionSelected: {
    borderColor: colors.accent.primary,
    backgroundColor: colors.accent.primary,
  },
  genderText: {
    fontSize: 16,
    fontWeight: "700",
    color: colors.text.secondary,
  },
  genderTextSelected: {
    color: colors.text.primary,
  },
  button: {
    width: "100%",
  },
});
