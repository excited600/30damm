import { useState } from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { useCreateGatheringStore } from "@/store/useCreateGatheringStore";
import type { GatheringCategory } from "@/api/types/gathering";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 4;

const CATEGORIES: { label: string; value: GatheringCategory }[] = [
  { label: "파티", value: "PARTY" },
  { label: "맛집/음료", value: "FOOD_DRINK" },
  { label: "액티비티", value: "ACTIVITY" },
  { label: "없음", value: "NONE" },
];

export default function CreateGatheringCategoryScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const store = useCreateGatheringStore();
  const [category, setCategory] = useState<GatheringCategory>(store.category);

  const handleNext = () => {
    store.setCategory(category);
    router.push("/(gathering)/CreateGatheringIntroductionScreen");
  };

  return (
    <View style={[styles.screen, { paddingTop: insets.top }]}>
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
        <View style={styles.title}>
          <Text style={styles.titleText}>어떤 모임인가요?</Text>
        </View>

        <View style={styles.space} />

        <View style={styles.categoryList}>
          {CATEGORIES.map((cat) => (
            <Pressable
              key={cat.value}
              style={[
                styles.categoryOption,
                category === cat.value && styles.categoryOptionSelected,
              ]}
              onPress={() => setCategory(cat.value)}
            >
              <Text
                style={[
                  styles.categoryText,
                  category === cat.value && styles.categoryTextSelected,
                ]}
              >
                {cat.label}
              </Text>
            </Pressable>
          ))}
        </View>

        <View style={styles.space} />
      </View>

      {/* BottomCTAOnlyButton */}
      <View style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}>
        <Button
          label="다음으로"
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
          onPress={handleNext}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
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
  title: {
    paddingVertical: 20,
  },
  titleText: {
    fontSize: 20,
    fontWeight: "700",
    lineHeight: 28,
    color: colors.text.primary,
  },
  space: {
    flex: 1,
    maxHeight: 100,
  },
  categoryList: {
    gap: 12,
  },
  categoryOption: {
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.surface,
    alignItems: "center",
  },
  categoryOptionSelected: {
    backgroundColor: colors.accent.primary,
    borderColor: colors.accent.primary,
  },
  categoryText: {
    fontSize: 16,
    fontWeight: "700",
    color: colors.text.secondary,
  },
  categoryTextSelected: {
    color: colors.text.primary,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
