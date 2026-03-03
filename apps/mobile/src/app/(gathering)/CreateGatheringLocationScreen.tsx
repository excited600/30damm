import { useState } from "react";
import { View, Text, StyleSheet, Pressable, KeyboardAvoidingView, Platform, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { UnderlineInput } from "@/shared/components/ui/UnderlineInput";
import { useCreateGatheringStore } from "@/store/useCreateGatheringStore";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 6;
const LOCATION_MAX = 20;

export default function CreateGatheringLocationScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const store = useCreateGatheringStore();
  const [location, setLocation] = useState(store.location ?? "");

  const locationOverflow = location.length > LOCATION_MAX;

  const handleNext = () => {
    store.setLocation(location || null);
    router.push("/(gathering)/CreateGatheringWhenScreen");
  };

  return (
    <KeyboardAvoidingView
      style={[styles.createGatheringLocation, { paddingTop: insets.top }]}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
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
      <ScrollView
        style={styles.content}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {/* Title */}
        <View style={styles.title}>
          <Text style={styles.titleText}>어디서 만날까요?</Text>
        </View>

        <UnderlineInput
          label="장소를 정해주세요"
          placeholder="예) 강남역 1번 출구(20자 이내)"
          value={location}
          onChangeText={setLocation}
          icon={
            <Ionicons name="location-outline" size={22} color={colors.text.primary} />
          }
          error={locationOverflow ? "글자수(20자)를 초과했습니다" : undefined}
        />
      </ScrollView>

      {/* BottomCTAOnlyButton */}
      <View
        style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}
      >
        <Button
          label="다음으로"
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
          onPress={handleNext}
        />
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  createGatheringLocation: {
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
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
