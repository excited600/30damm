import { View, StyleSheet, FlatList, Pressable, ActivityIndicator } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useInfiniteQuery } from "@tanstack/react-query";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { GatheringCard } from "@/features/gathering/components/GatheringCard";
import { gatheringClient } from "@/api/clients/gatheringClient";
import type { GatheringListItem } from "@/api/types/gathering";

const PAGE_SIZE = 50;

export default function GatheringCardListScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } =
    useInfiniteQuery({
      queryKey: ["gatherings"],
      queryFn: ({ pageParam }) =>
        gatheringClient.scroll({
          size: PAGE_SIZE,
          uuid: pageParam?.uuid,
          score: pageParam?.score,
        }),
      initialPageParam: undefined as
        | { uuid: string; score: number }
        | undefined,
      getNextPageParam: (lastPage) =>
        lastPage.hasNext && lastPage.cursor
          ? { uuid: lastPage.cursor.uuid, score: lastPage.cursor.score }
          : undefined,
    });

  const gatherings = data?.pages.flatMap((page) => page.list) ?? [];

  const formatGatheringCard = (item: GatheringListItem) => {
    const dateTime = item.date
      ? `${item.date}${item.startTime ? ` ${item.startTime}` : ""}`
      : "";
    const duration = item.duration ? `${item.duration}시간` : "";
    const participants = `${item.maleCount}:${item.femaleCount}`;
    const price = item.isFree
      ? "무료"
      : item.price
        ? `${item.price.toLocaleString()}원`
        : "";

    return {
      title: item.title,
      location: item.location ?? "",
      dateTime,
      duration,
      participants,
      hostName: item.host.nickname,
      hostAvatarUri: item.host.profileImageUrl ?? undefined,
      price,
      thumbnailUri: item.imgUrl ?? undefined,
    };
  };

  const handleEndReached = () => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  };

  return (
    <View style={[styles.gatheringCardListScreen, { paddingTop: insets.top }]}>
      {/* ButtonHeader */}
      <View style={styles.buttonHeader}>
        <View style={styles.leftBlank} />
        <Pressable
          style={styles.plusButton}
          onPress={() => router.push("/(gathering)/CreateGatheringParticipant")}
        >
          <Ionicons name="add" size={30} color={colors.text.primary} />
        </Pressable>
        <View style={styles.menuButton}>
          <Ionicons name="menu" size={28} color={colors.text.primary} />
        </View>
      </View>

      {/* GatheringCardList */}
      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.accent.primary} />
        </View>
      ) : (
        <FlatList
          data={gatherings}
          keyExtractor={(item) => item.gatheringUuid}
          renderItem={({ item }) => {
            const cardProps = formatGatheringCard(item);
            return (
              <GatheringCard
                {...cardProps}
                onPress={() =>
                  router.push({
                    pathname: "/(gathering)/GatheringDetailScreen",
                    params: { gatheringUuid: item.gatheringUuid },
                  })
                }
              />
            );
          }}
          style={styles.gatheringCardList}
          showsVerticalScrollIndicator={false}
          onEndReached={handleEndReached}
          onEndReachedThreshold={0.5}
          ListFooterComponent={
            isFetchingNextPage ? (
              <View style={styles.footer}>
                <ActivityIndicator size="small" color={colors.accent.primary} />
              </View>
            ) : null
          }
        />
      )}

      {/* BottomTabBar */}
      <View style={[styles.bottomTabBar, { paddingBottom: Math.max(insets.bottom, 24) }]}>
        <Pressable style={styles.tabItem}>
          <Ionicons name="home" size={21} color={colors.text.primary} />
        </Pressable>
        <Pressable style={styles.tabItem}>
          <Ionicons name="chatbubble" size={21} color={colors.text.primary} />
        </Pressable>
        <Pressable style={styles.tabItem}>
          <Ionicons name="person" size={21} color={colors.text.primary} />
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  gatheringCardListScreen: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 10,
    gap: 10,
  },
  buttonHeader: {
    height: 64,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  leftBlank: {
    flex: 1,
  },
  plusButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.accent.primary,
    alignItems: "center",
    justifyContent: "center",
  },
  menuButton: {
    flex: 1,
    alignItems: "flex-end",
  },
  loadingContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  gatheringCardList: {
    flex: 1,
  },
  footer: {
    paddingVertical: 16,
    alignItems: "center",
  },
  bottomTabBar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 40,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: colors.surface,
    backgroundColor: colors.background,
  },
  tabItem: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});
