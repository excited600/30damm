import { View, Text, Image, StyleSheet, ScrollView, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter, useLocalSearchParams } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";

const MOCK_DETAIL = {
  description:
    "어쩌구저쩌구 소개소개소개 어쩌구저쩌구 소개소개소개어쩌구저쩌구 소개소개소개\n\n어쩌구저쩌구 소개소개소개 어쩌구저쩌구 소개소개소개어쩌구저쩌구 소개소개소개\n\n어쩌구저쩌구 소개소개소개 어쩌구저쩌구 소개소개소개어쩌구저쩌구 소개소개소개",
  participants: [
    { id: "1", name: "루트", isHost: true },
    { id: "2", name: "루트", isHost: false },
    { id: "3", name: "루트", isHost: false },
    { id: "4", name: "루트", isHost: false },
  ],
  detailInfo: [
    "카테고리",
    "선착순",
    "총 2명 ~ 20명",
    "남 2/10 여 1/10",
    "12.28(금) 오전 11시 · 2시간",
    "신촌역 2호선",
    "20,000원",
  ],
};

export default function GatheringDetailScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { imageUrl } = useLocalSearchParams<{ imageUrl?: string }>();
  const hasImage = !!imageUrl;

  return (
    <View style={[styles.gatheringDetailScreen, { paddingTop: insets.top }]}>
      {/* ScrollContent */}
      <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
        {hasImage ? (
          <Image source={{ uri: imageUrl }} style={styles.gatheringPicture} />
        ) : (
          <View style={styles.space} />
        )}

        {/* BodySection */}
        <View style={styles.bodySection}>
          <Text style={styles.bodyText}>{MOCK_DETAIL.description}</Text>
        </View>

        {/* ParticipantSection */}
        <View style={styles.participantSection}>
          <View style={styles.sectionTitle}>
            <Text style={styles.sectionTitleText}>함께하는 사람들</Text>
          </View>
          <View style={styles.participants}>
            {MOCK_DETAIL.participants.map((p) => (
              <View key={p.id} style={styles.participant}>
                <View style={styles.participantProfile}>
                  <View style={styles.profilePlaceholder} />
                </View>
                <Text style={styles.participantName}>{p.name}</Text>
                {p.isHost && (
                  <View style={styles.hostIcon}>
                    <Ionicons name="shield-checkmark" size={14} color={colors.accent.primary} />
                  </View>
                )}
              </View>
            ))}
          </View>
        </View>

        {/* DetailInfoSection */}
        <View style={styles.detailInfoSection}>
          <View style={styles.sectionTitle}>
            <Text style={styles.sectionTitleText}>상세정보</Text>
          </View>
          <View style={styles.detailInfo}>
            {MOCK_DETAIL.detailInfo.map((info, i) => (
              <Text key={i} style={styles.detailInfoText}>
                {info}
              </Text>
            ))}
          </View>
        </View>
      </ScrollView>

      {/* BottomCTAOnlyButton */}
      <View style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}>
        <Button
          label="참여하기"
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
      </View>

      {/* Header (absolute, overlaps image when hasImage=true) */}
      <View style={[styles.header, { top: insets.top }]}>
        <Pressable onPress={() => router.back()} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <View style={styles.headerBlank} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  gatheringDetailScreen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  scrollContent: {
    flex: 1,
  },
  gatheringPicture: {
    width: "100%",
    aspectRatio: 390 / 210,
    backgroundColor: colors.surface,
  },
  space: {
    height: 56,
  },
  bodySection: {
    paddingHorizontal: 10,
    paddingTop: 20,
  },
  bodyText: {
    fontSize: 16,
    fontWeight: "500",
    color: colors.text.primary,
    lineHeight: 24,
  },
  participantSection: {
    paddingHorizontal: 20,
    gap: 5,
    marginTop: 20,
  },
  sectionTitle: {
    width: "100%",
  },
  sectionTitleText: {
    fontSize: 18,
    fontWeight: "600",
    lineHeight: 26,
    color: colors.accent.primary,
  },
  participants: {
    gap: 1,
  },
  participant: {
    flexDirection: "row",
    alignItems: "center",
    gap: 7,
    paddingVertical: 5,
  },
  participantProfile: {
    alignItems: "center",
  },
  profilePlaceholder: {
    width: 54,
    height: 54,
    borderRadius: 27,
    backgroundColor: colors.surface,
  },
  participantName: {
    fontSize: 16,
    fontWeight: "600",
    color: colors.text.primary,
  },
  hostIcon: {
    overflow: "hidden",
  },
  detailInfoSection: {
    paddingHorizontal: 20,
    gap: 5,
    marginTop: 20,
    marginBottom: 20,
  },
  detailInfo: {
    gap: 4,
  },
  detailInfoText: {
    fontSize: 14,
    fontWeight: "700",
    lineHeight: 20,
    color: colors.text.primary,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
  header: {
    position: "absolute",
    left: 0,
    right: 0,
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  headerBlank: {
    width: 24,
    height: 24,
  },
});
