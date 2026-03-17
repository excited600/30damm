import { useState } from "react";
import { View, Text, StyleSheet, Pressable, KeyboardAvoidingView, Platform, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter, useLocalSearchParams } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { typography } from "@/shared/constants/typography";
import { Checkbox } from "@/shared/components/ui/Checkbox";
import { UnderlineInput } from "@/shared/components/ui/UnderlineInput";
import { BottomCTA } from "@/shared/components/ui/BottomCTA";

const REPORT_REASONS = ["불쾌한 문구", "불법, 허위 정보", "기타"] as const;
type ReportReason = (typeof REPORT_REASONS)[number];

export default function ReportScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { gatheringUuid } = useLocalSearchParams<{ gatheringUuid: string }>();

  const [selectedReason, setSelectedReason] = useState<ReportReason | null>(null);
  const [description, setDescription] = useState("");
  const [showError, setShowError] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!selectedReason) {
      setShowError(true);
      return;
    }
    if (submitting) return;
    setSubmitting(true);
    try {
      // TODO: API 호출 (나중에 연결)
      // await reportClient.report({ gatheringUuid, reason: selectedReason, description });
      router.push({
        pathname: "/(gathering)/ReportCompleteScreen",
        params: { gatheringUuid },
      } as any);
    } finally {
      setSubmitting(false);
    }
  };

  const handleSelectReason = (reason: ReportReason) => {
    setSelectedReason(reason);
    setShowError(false);
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerBlank} />
        <Text style={styles.headerTitle}>신고하기</Text>
        <Pressable onPress={() => router.back()} hitSlop={8}>
          <Ionicons name="close" size={24} color={colors.text.primary} />
        </Pressable>
      </View>

      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView
          style={styles.flex}
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
        >
          {/* 안내 문구 */}
          <View style={styles.notice}>
            <Text style={styles.noticeText}>
              {"회원님의 신고는 익명으로 처리됩니다.  \n신고가 누적된 모임의 호스트는 활동이 제재됩니다."}
            </Text>
          </View>

          {/* 체크박스 */}
          <View style={styles.checkboxGroup}>
            {REPORT_REASONS.map((reason) => (
              <Checkbox
                key={reason}
                label={reason}
                checked={selectedReason === reason}
                onPress={() => handleSelectReason(reason)}
              />
            ))}
            {showError && (
              <Text style={styles.errorText}>신고 사유를 골라주세요</Text>
            )}
          </View>

          {/* 신고 사유 입력 */}
          <View style={styles.inputWrapper}>
            <UnderlineInput
              label="신고 사유"
              placeholder="신고하시는 이유를 좀 더 설명해주세요(300자 이내)"
              value={description}
              onChangeText={setDescription}
              textInputProps={{ maxLength: 300 }}
            />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>

      <BottomCTA
        label={submitting ? "제출 중..." : "제출하기"}
        onPress={handleSubmit}
        disabled={submitting}
        color={colors.accent.primary}
        labelColor={colors.text.primary}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  flex: {
    flex: 1,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  headerBlank: {
    width: 32,
    height: 32,
  },
  headerTitle: {
    flex: 1,
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
    textAlign: "center",
  },
  scrollContent: {
    gap: 10,
  },
  notice: {
    alignItems: "center",
    justifyContent: "center",
    padding: 10,
    width: "100%",
  },
  noticeText: {
    ...typography.body.md,
    fontWeight: "700",
    color: colors.text.primary,
    textAlign: "center",
  },
  checkboxGroup: {
    paddingHorizontal: 20,
    gap: 21,
    width: "100%",
  },
  errorText: {
    fontSize: 12,
    fontWeight: "400",
    color: colors.error,
    marginTop: 2,
  },
  inputWrapper: {
    paddingHorizontal: 20,
    width: "100%",
  },
});
