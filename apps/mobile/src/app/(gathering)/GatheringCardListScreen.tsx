import { useCallback } from "react";
import { View, Text, StyleSheet, FlatList, Pressable, ActivityIndicator } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter, useFocusEffect } from "expo-router";
import { useInfiniteQuery } from "@tanstack/react-query";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { GatheringCard } from "@/features/gathering/components/GatheringCard";
import { gatheringClient } from "@/api/clients/gatheringClient";
import type { GatheringListItem } from "@/api/types/gathering";

const PAGE_SIZE = 20;

function formatGatheringCard(item: GatheringListItem) {
  const date = item.date ?? "";
  const time = item.startTime ?? "";
  const duration = item.duration
    ? item.duration % 60 > 0
      ? `${Math.floor(item.duration / 60)}시간 ${item.duration % 60}분`
      : `${Math.floor(item.duration / 60)}시간`
    : "";
  const participants = `${item.maleCount + item.femaleCount + 1}명`;
  const price = item.isFree
    ? "무료"
    : item.isSplit
      ? "1/N"
      : item.price
        ? `${item.price.toLocaleString()}원`
        : "";

  return {
    title: item.title,
    location: item.location ?? "",
    date,
    time,
    duration,
    participants,
    hostName: item.host.nickname,
    hostAvatarUri: item.host.profileImageUrl ?? undefined,
    price,
    thumbnailUri: item.imgUrl ?? undefined,
  };
}

export default function GatheringCardListScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError, refetch } =
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

  useFocusEffect(
    useCallback(() => {
      refetch();
    }, [refetch]),
  );

  const gatherings = data?.pages.flatMap((page) => page.list) ?? [];

  const handleEndReached = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const renderItem = useCallback(
    ({ item }: { item: GatheringListItem }) => {
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
    },
    [router],
  );

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
        <View style={styles.menuButton} />
      </View>

      {/* GatheringCardList */}
      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.accent.primary} />
        </View>
      ) : isError ? (
        <View style={styles.centeredContainer}>
          <Text style={styles.stateText}>모임을 불러오지 못했습니다.</Text>
          <Pressable style={styles.retryButton} onPress={() => refetch()}>
            <Text style={styles.retryButtonText}>다시 시도</Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={gatherings}
          keyExtractor={(item) => item.gatheringUuid}
          renderItem={renderItem}
          style={styles.gatheringCardList}
          showsVerticalScrollIndicator={false}
          onEndReached={handleEndReached}
          onEndReachedThreshold={0.5}
          ListEmptyComponent={
            <View style={styles.centeredContainer}>
              <Text style={styles.stateText}>아직 모임이 없습니다.</Text>
            </View>
          }
          ListFooterComponent={
            isFetchingNextPage ? (
              <View style={styles.footer}>
                <ActivityIndicator size="small" color={colors.accent.primary} />
              </View>
            ) : null
          }
        />
      )}

      {/* BottomTabBar - temporarily hidden */}
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
  centeredContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingTop: 80,
    gap: 16,
  },
  stateText: {
    fontSize: 16,
    color: colors.text.secondary,
    textAlign: "center",
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
