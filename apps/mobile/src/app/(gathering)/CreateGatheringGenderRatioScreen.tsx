import { useState, useRef, useCallback } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  PanResponder,
  LayoutChangeEvent,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 2;
const TOTAL_POSITIONS = 11; // 10:0, 9:1, ..., 0:10
const MALE_COLOR = "#5647FF";
const FEMALE_COLOR = "#FF4747";

export default function CreateGatheringGenderRatioScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [genderRatioEnabled, setGenderRatioEnabled] = useState(false);
  const [maleRatio, setMaleRatio] = useState(5); // 0~10, default 5:5
  const graphWidth = useRef(0);

  const femaleRatio = 10 - maleRatio;

  const graphRef = useRef<View>(null);

  const onGraphLayout = useCallback((e: LayoutChangeEvent) => {
    graphWidth.current = e.nativeEvent.layout.width;
    graphRef.current?.measureInWindow((x: number) => {
      graphX.current = x;
    });
  }, []);

  const graphX = useRef(0);

  const updateRatioFromX = useCallback((pageX: number) => {
    if (graphWidth.current > 0) {
      const x = pageX - graphX.current;
      const ratio = Math.round((x / graphWidth.current) * (TOTAL_POSITIONS - 1));
      setMaleRatio(Math.max(0, Math.min(10, ratio)));
    }
  }, []);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (e) => {
        updateRatioFromX(e.nativeEvent.pageX);
      },
      onPanResponderMove: (e) => {
        updateRatioFromX(e.nativeEvent.pageX);
      },
    })
  ).current;

  const signLeftPercent = (maleRatio / 10) * 100;

  return (
    <View style={[styles.createGatheringGenderRatio, { paddingTop: insets.top }]}>
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
        {/* Title */}
        <View style={styles.title}>
          <Text style={styles.titleText}>
            최대 인원의 성비를{"\n"}정할까요?
          </Text>
        </View>

        {/* Space */}
        <View style={styles.space} />

        {/* Toggle */}
        <View style={styles.toggleGroup}>
          <Pressable
            style={[
              styles.toggleOption,
              !genderRatioEnabled && styles.toggleOptionSelected,
            ]}
            onPress={() => setGenderRatioEnabled(false)}
          >
            <Text
              style={[
                styles.toggleText,
                !genderRatioEnabled
                  ? styles.toggleTextSelected
                  : styles.toggleTextUnselected,
              ]}
            >
              아니오
            </Text>
          </Pressable>
          <Pressable
            style={[
              styles.toggleOption,
              genderRatioEnabled && styles.toggleOptionSelected,
            ]}
            onPress={() => setGenderRatioEnabled(true)}
          >
            <Text
              style={[
                styles.toggleText,
                genderRatioEnabled
                  ? styles.toggleTextSelected
                  : styles.toggleTextUnselected,
              ]}
            >
              네
            </Text>
          </Pressable>
        </View>

        {/* Gender Ratio (only when enabled) */}
        {genderRatioEnabled && (
          <>
            <View style={styles.genderRatioSpace} />

            <View style={styles.genderRatio} {...panResponder.panHandlers}>
              {/* Sign / Indicator */}
              <View style={styles.signContainer}>
                <View style={[styles.signPositioner, { left: `${signLeftPercent}%` }]}>
                  <View style={styles.sign}>
                    <Text style={styles.signText}>
                      {maleRatio}:{femaleRatio}
                    </Text>
                  </View>
                  {/* Triangle pointer */}
                  <View style={styles.signTriangle} />
                </View>
              </View>

              {/* GenderRatioGraph */}
              <View
                ref={graphRef}
                style={styles.genderRatioGraph}
                onLayout={onGraphLayout}
              >
                <View
                  style={[
                    styles.maleRatio,
                    { flex: maleRatio || 0.01 },
                  ]}
                />
                <View
                  style={[
                    styles.femaleRatio,
                    { flex: femaleRatio || 0.01 },
                  ]}
                />
              </View>
            </View>
          </>
        )}

        <View style={styles.space} />
      </View>

      {/* BottomCTAOnlyButton */}
      <View
        style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}
      >
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
  createGatheringGenderRatio: {
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
  toggleGroup: {
    flexDirection: "row",
    backgroundColor: colors.accent.primary,
    borderRadius: 28,
    padding: 4,
  },
  toggleOption: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 10,
    borderRadius: 24,
  },
  toggleOptionSelected: {
    backgroundColor: colors.white,
  },
  toggleText: {
    fontSize: 16,
    fontWeight: "700",
  },
  toggleTextSelected: {
    color: colors.accent.primary,
  },
  toggleTextUnselected: {
    color: colors.white,
  },
  genderRatioSpace: {
    height: 20,
  },
  genderRatio: {},
  signContainer: {
    height: 36,
    position: "relative",
  },
  signPositioner: {
    position: "absolute",
    bottom: 0,
    alignItems: "center",
    transform: [{ translateX: -20 }],
  },
  sign: {
    backgroundColor: colors.accent.primary,
    borderRadius: 8,
    paddingHorizontal: 6,
    paddingVertical: 4,
    alignItems: "center",
    minWidth: 40,
  },
  signText: {
    fontSize: 14,
    fontWeight: "700",
    color: colors.text.primary,
    textAlign: "center",
  },
  signTriangle: {
    width: 0,
    height: 0,
    borderLeftWidth: 6,
    borderRightWidth: 6,
    borderTopWidth: 6,
    borderLeftColor: "transparent",
    borderRightColor: "transparent",
    borderTopColor: colors.accent.primary,
  },
  genderRatioGraph: {
    flexDirection: "row",
    height: 11,
    borderRadius: 6,
    overflow: "hidden",
  },
  maleRatio: {
    backgroundColor: MALE_COLOR,
  },
  femaleRatio: {
    backgroundColor: FEMALE_COLOR,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
