import { useState } from "react";
import { View, Text, Image, StyleSheet, Pressable, Alert } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter, useLocalSearchParams } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { ProfileMenuBottomSheet } from "@/shared/components/ui/ProfileMenuBottomSheet";
import { blockClient } from "@/api/clients/blockClient";
import { queryClient } from "@/api/queryClient";

const EMOJIS = ["😀", "😎", "🤩", "🥳", "😺", "🐶", "🐱", "🦊", "🐻", "🐼", "🐸", "🐵", "🦁", "🐯", "🐰", "🐨", "🐷", "🌸", "🌺", "🍀", "🔥", "⭐", "🎉", "🎈", "🍕", "🎸", "🏀", "⚽", "🎮", "🚀"];

function getRandomEmoji(seed: string): string {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0;
  }
  return EMOJIS[Math.abs(hash) % EMOJIS.length];
}

export default function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { userUuid, nickname, profileImageUrl, viewerRelation } = useLocalSearchParams<{
    userUuid: string;
    nickname: string;
    profileImageUrl?: string;
    viewerRelation: string;
  }>();

  const isStranger = viewerRelation !== "SELF";
  const [menuVisible, setMenuVisible] = useState(false);

  return (
    <View style={[styles.screen, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <View style={styles.headerCenter}>
          <Text style={styles.headerTitle}>{nickname}</Text>
        </View>
        {isStranger ? (
          <Pressable
            onPress={() => setMenuVisible(true)}
            hitSlop={8}
            style={styles.headerMore}
          >
            <View style={styles.moreDot} />
            <View style={styles.moreDot} />
            <View style={styles.moreDot} />
          </Pressable>
        ) : (
          <View style={styles.headerRight} />
        )}
      </View>

      {/* Profile Image */}
      <View style={styles.profileSection}>
        {profileImageUrl ? (
          <Image source={{ uri: profileImageUrl }} style={styles.profileImage} />
        ) : (
          <View style={styles.profilePlaceholder}>
            <Text style={styles.profileEmoji}>{getRandomEmoji(nickname ?? "")}</Text>
          </View>
        )}
      </View>

      {/* Content Area */}
      <View style={styles.contentArea}>
        <View style={styles.contentSurface} />
      </View>

      {/* BottomSheet */}
      <ProfileMenuBottomSheet
        visible={menuVisible}
        onClose={() => setMenuVisible(false)}
        onBlock={() => {
          setMenuVisible(false);
          Alert.alert("차단하기", `${nickname}님을 차단하시겠습니까?`, [
            { text: "취소", style: "cancel" },
            {
              text: "차단",
              style: "destructive",
              onPress: async () => {
                try {
                  await blockClient.block({ blockedUserUuid: userUuid! });
                  await queryClient.invalidateQueries({ queryKey: ["gatherings"] });
                  await queryClient.invalidateQueries({ queryKey: ["gathering"] });
                  Alert.alert("차단 완료", `${nickname}님을 차단했습니다.`, [
                    { text: "확인", onPress: () => router.replace("/(tabs)" as any) },
                  ]);
                } catch {
                  Alert.alert("오류", "차단에 실패했습니다.", [{ text: "확인" }]);
                }
              },
            },
          ]);
        }}
        onReport={() => {
          setMenuVisible(false);
          router.push({
            pathname: "/(gathering)/ReportScreen",
            params: { targetType: "USER", targetUuid: userUuid },
          } as any);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  headerCenter: {
    flex: 1,
    paddingHorizontal: 20,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
  },
  headerRight: {
    width: 24,
    height: 24,
  },
  headerMore: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
  },
  moreDot: {
    width: 5,
    height: 5,
    borderRadius: 2.5,
    backgroundColor: colors.text.primary,
  },
  profileSection: {
    paddingHorizontal: 20,
  },
  profileImage: {
    width: 100,
    height: 100,
    borderRadius: 50,
  },
  profilePlaceholder: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  profileEmoji: {
    fontSize: 48,
  },
  contentArea: {
    flex: 1,
    paddingTop: 10,
  },
  contentSurface: {
    flex: 1,
    backgroundColor: colors.surface,
  },
});
