import { View, Text, StyleSheet, ScrollView, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { GatheringCard } from "@/features/gathering/components/GatheringCard";

const MOCK_GATHERINGS = Array.from({ length: 9 }, (_, i) => ({
  id: String(i),
  title: "제목입니다.제목입니다.제목입니다.",
  location: "서대문구",
  dateTime: "12.28(월) 오전 11시",
  duration: "2시간",
  participants: "7:3",
  hostName: "루트",
  price: "20,000원",
}));

export default function GatheringCardListScreen() {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerSide} />
        <Pressable style={styles.plusButton}>
          <Ionicons name="add" size={24} color={colors.background} />
        </Pressable>
        <View style={[styles.headerSide, styles.headerRight]}>
          <Ionicons name="menu" size={28} color={colors.text.primary} />
        </View>
      </View>

      {/* Card List */}
      <ScrollView style={styles.list} contentContainerStyle={styles.listContent}>
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
          />
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 10,
    gap: 10,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  headerSide: {
    flex: 1,
  },
  headerRight: {
    alignItems: "flex-end",
  },
  plusButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.accent.primary,
    alignItems: "center",
    justifyContent: "center",
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: 10,
  },
});
