import { useState } from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { UnderlineInput } from "@/shared/components/ui/UnderlineInput";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 5;
const TITLE_MAX = 30;
const DETAIL_MAX = 200;

export default function CreateGatheringIntroductionScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [detail, setDetail] = useState("");

  const titleOverflow = title.length > TITLE_MAX;
  const detailOverflow = detail.length > DETAIL_MAX;

  return (
    <View style={[styles.createGatheringIntroduction, { paddingTop: insets.top }]}>
      {/* ProgressBar */}
      <View style={styles.progressBar}>
        {Array.from({ length: TOTAL_STEPS }, (_, i) => (
          <View
            key={i}
            style={[
              styles.progressSegment,
              {
                backgroundColor:
                  i < CURRENT_STEP
                    ? colors.accent.primary
                    : colors.text.primary,
              },
            ]}
          />
        ))}
      </View>

      {/* Header */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <Text style={styles.headerTitle} />
        <View style={styles.headerBlank} />
      </View>

      {/* Content */}
      <View style={styles.content}>
        {/* Title */}
        <View style={styles.titleSection}>
          <Text style={styles.titleText}>모임을 소개해주세요</Text>
        </View>

        {/* Introduction */}
        <View style={styles.introduction}>
          <UnderlineInput
            label="제목"
            placeholder="예) 30, 우리끼리 모여요(30자 이내)"
            value={title}
            onChangeText={setTitle}
            error={titleOverflow ? "글자수(30자)를 초과했습니다" : undefined}
          />
          <UnderlineInput
            label="상세소개"
            placeholder="모임을 조금 더 소개해주세요(200자 이내)"
            value={detail}
            onChangeText={setDetail}
            multiline
            error={detailOverflow ? "글자수(200자)를 초과했습니다" : undefined}
          />
        </View>
      </View>

      {/* BottomCTAOnlyButton */}
      <View
        style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}
      >
        <Button
          label="다음으로"
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
          onPress={() => router.push("/(gathering)/CreateGatheringLocationScreen")}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  createGatheringIntroduction: {
    flex: 1,
    backgroundColor: colors.background,
    gap: 10,
  },
  progressBar: {
    flexDirection: "row",
    height: 3,
    width: "100%",
  },
  progressSegment: {
    flex: 1,
    height: 3,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  headerTitle: {
    flex: 1,
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
    textAlign: "center",
  },
  headerBlank: {
    width: 24,
    height: 24,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    gap: 10,
    overflow: "hidden",
  },
  titleSection: {
    paddingVertical: 20,
  },
  titleText: {
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
  },
  introduction: {
    gap: 15,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
