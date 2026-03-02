import { View, StyleSheet, ScrollView, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { GatheringCard } from "@/features/gathering/components/GatheringCard";

const MOCK_IMAGE = "https://picsum.photos/seed/gathering/400/300";

const MOCK_GATHERINGS = Array.from({ length: 9 }, (_, i) => ({
  id: String(i),
  title: "제목입니다.제목입니다.제목입니다.",
  location: "서대문구",
  dateTime: "12.28(월) 오전 11시",
  duration: "2시간",
  participants: "7:3",
  hostName: "루트",
  price: "20,000원",
  thumbnailUri: i % 2 === 0 ? MOCK_IMAGE : undefined,
}));

export default function GatheringCardListScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  return (
    <View style={[styles.gatheringCardListScreen, { paddingTop: insets.top }]}>
      {/* ButtonHeader */}
      <View style={styles.buttonHeader}>
        <View style={styles.leftBlank} />
        <Pressable style={styles.plusButton}>
          <Ionicons name="add" size={30} color={colors.text.primary} />
        </Pressable>
        <View style={styles.menuButton}>
          <Ionicons name="menu" size={28} color={colors.text.primary} />
        </View>
      </View>

      {/* GatheringCardList */}
      <ScrollView
        style={styles.gatheringCardList}
        showsVerticalScrollIndicator={false}
      >
        {MOCK_GATHERINGS.map((item) => (
          <GatheringCard
            key={item.id}
            title={item.title}
            location={item.location}
            dateTime={item.dateTime}
            duration={item.duration}
            participants={item.participants}
            hostName={item.hostName}
            price={item.price}
            thumbnailUri={item.thumbnailUri}
            onPress={() =>
              router.push({
                pathname: "/(gathering)/GatheringDetailScreen",
                params: item.thumbnailUri ? { imageUrl: item.thumbnailUri } : {},
              })
            }
          />
        ))}
      </ScrollView>

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
  gatheringCardList: {
    flex: 1,
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
