import { useRef, useCallback, useEffect } from "react";
import {
  View,
  Text,
  StyleSheet,
  Animated,
  PanResponder,
} from "react-native";
import { colors } from "@/shared/constants/colors";

const ITEM_HEIGHT = 30;

interface WheelPickerProps {
  title: string;
  min: number;
  max: number;
  value: number;
  onChange: (value: number) => void;
}

export function WheelPicker({ title, min, max, value, onChange }: WheelPickerProps) {
  const translateY = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    translateY.setValue(0);
  }, [value]);

  const clampValue = useCallback(
    (v: number) => Math.max(min, Math.min(max, v)),
    [min, max],
  );

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderMove: (_, gestureState) => {
        translateY.setValue(gestureState.dy);
      },
      onPanResponderRelease: (_, gestureState) => {
        const steps = Math.round(-gestureState.dy / ITEM_HEIGHT);
        const newValue = clampValue(value + steps);

        Animated.spring(translateY, {
          toValue: 0,
          useNativeDriver: true,
          tension: 100,
          friction: 12,
        }).start();

        if (newValue !== value) {
          onChange(newValue);
        }
      },
    }),
  ).current;

  const prev = value - 1;
  const next = value + 1;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <View style={styles.line} />
      <View style={styles.wheelContainer} {...panResponder.panHandlers}>
        <Animated.View style={[styles.wheelContent, { transform: [{ translateY }] }]}>
          {/* Previous item - rotated away like top of a wheel */}
          <View style={styles.itemWrapper}>
            <View style={styles.perspectiveTop}>
              <Text style={[styles.count, styles.countDimmed]}>
                {prev >= min ? String(prev) : ""}
              </Text>
            </View>
          </View>

          {/* Selected item - flat, front-facing */}
          <View style={styles.itemWrapper}>
            <Text style={[styles.count, styles.countSelected]}>
              {String(value)}
            </Text>
          </View>

          {/* Next item - rotated away like bottom of a wheel */}
          <View style={styles.itemWrapper}>
            <View style={styles.perspectiveBottom}>
              <Text style={[styles.count, styles.countDimmed]}>
                {next <= max ? String(next) : ""}
              </Text>
            </View>
          </View>
        </Animated.View>
      </View>
      <View style={styles.line} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: 41,
    alignItems: "center",
    gap: 4,
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    lineHeight: 26,
    color: colors.text.primary,
    textAlign: "center",
  },
  line: {
    width: "100%",
    height: 1,
    backgroundColor: colors.text.primary,
  },
  wheelContainer: {
    height: ITEM_HEIGHT * 3,
    overflow: "hidden",
    justifyContent: "center",
  },
  wheelContent: {
    alignItems: "center",
  },
  itemWrapper: {
    height: ITEM_HEIGHT,
    justifyContent: "center",
    alignItems: "center",
  },
  perspectiveTop: {
    transform: [
      { perspective: 150 },
      { rotateX: "45deg" },
      { scaleY: 0.8 },
    ],
  },
  perspectiveBottom: {
    transform: [
      { perspective: 150 },
      { rotateX: "-45deg" },
      { scaleY: 0.8 },
    ],
  },
  count: {
    fontSize: 18,
    fontWeight: "600",
    lineHeight: 26,
    textAlign: "center",
    width: 41,
  },
  countSelected: {
    color: colors.text.primary,
  },
  countDimmed: {
    color: "#6B6B6B",
  },
});
