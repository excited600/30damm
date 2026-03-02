import { View, Text, StyleSheet, Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useState } from "react";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { WheelPicker } from "@/shared/components/ui/WheelPicker";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 1;

export default function CreateGatheringParticipant() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [minCount, setMinCount] = useState(2);
  const [maxCount, setMaxCount] = useState(10);

  const handleMinChange = (val: number) => {
    setMinCount(val);
    if (val > maxCount) {
      setMaxCount(val);
    }
  };

  const handleMaxChange = (val: number) => {
    setMaxCount(val);
    if (val < minCount) {
      setMinCount(val);
    }
  };

  return (
    <View style={[styles.createGathering1, { paddingTop: insets.top }]}>
      {/* ProgressBar */}
      <View style={styles.progressBar}>
        {Array.from({ length: TOTAL_STEPS }, (_, i) => (
          <View
            key={i}
            style={[
              styles.progressSegment,
              { backgroundColor: i < CURRENT_STEP ? colors.accent.primary : colors.text.primary },
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
        {/* Title */}
        <View style={styles.title}>
          <Text style={styles.titleText}>참여 인원(호스트 포함)</Text>
        </View>

        {/* Space1 */}
        <View style={styles.space} />

        {/* MinCount & MaxCount */}
        <View style={styles.countsRow}>
          <View style={styles.countWrapper}>
            <WheelPicker
              title="최소"
              min={2}
              max={maxCount}
              value={minCount}
              onChange={handleMinChange}
            />
          </View>
          <View style={styles.countWrapper}>
            <WheelPicker
              title="최대"
              min={minCount}
              max={99}
              value={maxCount}
              onChange={handleMaxChange}
            />
          </View>
        </View>

        {/* Space1 */}
        <View style={styles.space} />
      </View>

      {/* BottomCTAOnlyButton */}
      <View style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}>
        <Button
          label="다음으로"
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  createGathering1: {
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
    maxHeight: 150,
  },
  countsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 27,
  },
  countWrapper: {
    padding: 10,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
