import { useState } from "react";
import { View, Text, Image, StyleSheet, ScrollView, Pressable, ActivityIndicator } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter, useLocalSearchParams } from "expo-router";
import { useQuery } from "@tanstack/react-query";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { Toast } from "@/shared/components/ui/Toast";
import { gatheringClient } from "@/api/clients/gatheringClient";
import type { GatheringDetailResponse } from "@/api/types/gathering";

const EMOJIS = ["😀", "😎", "🤩", "🥳", "😺", "🐶", "🐱", "🦊", "🐻", "🐼", "🐸", "🐵", "🦁", "🐯", "🐰", "🐨", "🐷", "🌸", "🌺", "🍀", "🔥", "⭐", "🎉", "🎈", "🍕", "🎸", "🏀", "⚽", "🎮", "🚀"];

function getRandomEmoji(seed: string): string {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0;
  }
  return EMOJIS[Math.abs(hash) % EMOJIS.length];
}

const CATEGORY_LABELS: Record<string, string> = {
  NONE: "없음",
  PARTY: "파티",
  FOOD_DRINK: "맛집/음료",
  ACTIVITY: "액티비티",
};

function formatDetailInfo(detail: GatheringDetailResponse): string[] {
  const info: string[] = [];
  info.push(CATEGORY_LABELS[detail.category] ?? detail.category);
  info.push(`총 ${detail.minCapacity}명 ~ ${detail.maxCapacity}명`);
  if (detail.isGenderRatioEnabled) {
    info.push(
      `남 ${detail.currentMaleCount}/${detail.maxMaleCapacity ?? "-"} 여 ${detail.currentFemaleCount}/${detail.maxFemaleCapacity ?? "-"}`,
    );
  } else {
    info.push(
      `남 ${detail.currentMaleCount} 여 ${detail.currentFemaleCount}`,
    );
  }
  if (detail.date) {
    const parts = [detail.date];
    if (detail.startTime) parts.push(detail.startTime);
    if (detail.duration) parts.push(`${detail.duration}시간`);
    info.push(parts.join(" · "));
  }
  if (detail.location) info.push(detail.location);
  if (detail.isFree) {
    info.push("무료");
  } else if (detail.price != null) {
    info.push(`${detail.price.toLocaleString()}원${detail.isSplit ? " (1/N)" : ""}`);
  }
  return info;
}

export default function GatheringDetailScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { gatheringUuid, showToast } = useLocalSearchParams<{
    gatheringUuid: string;
    showToast?: string;
  }>();
  const [toastVisible, setToastVisible] = useState(showToast === "true");

  const { data: detail, isLoading, isError, refetch } = useQuery({
    queryKey: ["gathering", gatheringUuid],
    queryFn: () => gatheringClient.getDetail(gatheringUuid!),
    enabled: !!gatheringUuid,
  });

  console.log("detail.isHost:", detail?.isHost);

  if (isLoading) {
    return (
      <View style={[styles.gatheringDetailScreen, styles.centered, { paddingTop: insets.top }]}>
        <ActivityIndicator size="large" color={colors.accent.primary} />
      </View>
    );
  }

  if (isError || !detail) {
    return (
      <View style={[styles.gatheringDetailScreen, styles.centered, { paddingTop: insets.top }]}>
        <Pressable onPress={() => router.back()} style={[styles.errorBackButton, { top: insets.top + 12 }]} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <Text style={styles.errorText}>모임 정보를 불러오지 못했습니다.</Text>
        <Pressable style={styles.retryButton} onPress={() => refetch()}>
          <Text style={styles.retryButtonText}>다시 시도</Text>
        </Pressable>
      </View>
    );
  }

  const hasImage = !!detail.imgUrl;
  const detailInfo = formatDetailInfo(detail);
  const participants = [
    { ...detail.host, isHost: true },
    ...detail.guests.map((g) => ({ ...g, isHost: false })),
  ];

  return (
    <View style={[styles.gatheringDetailScreen, { paddingTop: insets.top }]}>
      {/* ScrollContent */}
      <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
        {hasImage ? (
          <Image source={{ uri: detail.imgUrl! }} style={styles.gatheringPicture} />
        ) : (
          <View style={styles.gatheringPictureEmpty}>
            <Text style={styles.gatheringPictureEmoji}>{getRandomEmoji(detail.title)}</Text>
          </View>
        )}

        {/* BodySection */}
        <View style={styles.bodySection}>
          <Text style={styles.bodyTitle}>{detail.title}</Text>
          <Text style={styles.bodyText}>{detail.description}</Text>
        </View>

        {/* ParticipantSection */}
        <View style={styles.participantSection}>
          <View style={styles.sectionTitle}>
            <Text style={styles.sectionTitleText}>함께하는 사람들</Text>
          </View>
          <View style={styles.participants}>
            {participants.map((p) => (
              <View key={p.userUuid} style={styles.participant}>
                <View style={styles.participantProfile}>
                  {p.profileImageUrl ? (
                    <Image
                      source={{ uri: p.profileImageUrl }}
                      style={styles.profileImage}
                    />
                  ) : (
                    <View style={styles.profilePlaceholder}>
                      <Text style={styles.profileEmoji}>{getRandomEmoji(p.nickname)}</Text>
                    </View>
                  )}
                </View>
                <Text style={styles.participantName}>{p.nickname}</Text>
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
            {detailInfo.map((info, i) => (
              <Text key={`detail-${i}`} style={styles.detailInfoText}>
                {info}
              </Text>
            ))}
          </View>
        </View>
      </ScrollView>

      {/* BottomCTAOnlyButton */}
      <View style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}>
        {detail.isHost ? (
          <Button
            label="호스트 입니다"
            disabled
            style={styles.button}
          />
        ) : (
          <Button
            label="참여하기"
            color={colors.accent.primary}
            labelColor={colors.text.primary}
            style={styles.button}
          />
        )}
      </View>

      {/* Toast */}
      <Toast
        message="모임이 열렸습니다!"
        visible={toastVisible}
        duration={1500}
        onHide={() => setToastVisible(false)}
      />

      {/* Header (absolute, overlaps image when hasImage=true) */}
      <View style={[styles.header, { top: insets.top }]}>
        <Pressable onPress={() => router.replace("/(gathering)/GatheringCardListScreen")} hitSlop={8}>
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
  centered: {
    alignItems: "center",
    justifyContent: "center",
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
  gatheringPictureEmpty: {
    width: "100%",
    aspectRatio: 390 / 210,
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  gatheringPictureEmoji: {
    fontSize: 64,
  },
  bodySection: {
    paddingHorizontal: 10,
    paddingTop: 20,
    gap: 8,
  },
  bodyTitle: {
    fontSize: 20,
    fontWeight: "700",
    color: colors.text.primary,
    lineHeight: 28,
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
  profileImage: {
    width: 54,
    height: 54,
    borderRadius: 27,
  },
  profilePlaceholder: {
    width: 54,
    height: 54,
    borderRadius: 27,
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  profileEmoji: {
    fontSize: 28,
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
  errorBackButton: {
    position: "absolute",
    left: 20,
  },
  errorText: {
    fontSize: 16,
    color: colors.text.secondary,
    textAlign: "center",
    marginBottom: 8,
  },
  retryButton: {
    backgroundColor: colors.accent.primary,
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 24,
  },
  retryButtonText: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.text.primary,
  },
});
