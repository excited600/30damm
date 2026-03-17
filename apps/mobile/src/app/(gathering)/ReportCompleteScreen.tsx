import { View, Text, StyleSheet, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { typography } from "@/shared/constants/typography";
import { BottomCTA } from "@/shared/components/ui/BottomCTA";

export default function ReportCompleteScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  const handleComplete = () => {
    router.replace("/(gathering)/GatheringCardListScreen" as any);
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerBlank} />
        <Text style={styles.headerTitle}>신고하기</Text>
        <Pressable onPress={handleComplete} hitSlop={8}>
          <Ionicons name="close" size={24} color={colors.text.primary} />
        </Pressable>
      </View>

      {/* Center Content */}
      <View style={styles.centerContent}>
        <View style={styles.checkIcon}>
          <Ionicons name="checkmark-circle" size={95} color={colors.text.secondary} />
        </View>
        <Text style={styles.title}>소중한 의견 감사합니다</Text>
        <Text style={styles.subtitle}>신고를 검토하는 데 최대 24시간이 소요됩니다.</Text>
      </View>

      <BottomCTA
        label="완료하기"
        onPress={handleComplete}
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
    ...typography.heading.md,
    fontSize: 20,
    lineHeight: 28,
    color: colors.text.primary,
    textAlign: "center",
  },
  centerContent: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: 10,
  },
  checkIcon: {
    marginBottom: 10,
  },
  title: {
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
  },
  subtitle: {
    ...typography.body.md,
    fontWeight: "700",
    color: colors.text.tertiary,
  },
});
