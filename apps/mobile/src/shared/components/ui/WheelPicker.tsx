import { useRef, useCallback, useEffect, useMemo } from "react";
import {
  View,
  Text,
  StyleSheet,
  Animated,
  PanResponder,
} from "react-native";
import { colors } from "@/shared/constants/colors";

const ITEM_HEIGHT = 30;

interface WheelPickerBaseProps {
  title: string;
  value: number;
  onChange: (value: number) => void;
}

interface WheelPickerRangeProps extends WheelPickerBaseProps {
  min: number;
  max: number;
  items?: never;
}

interface WheelPickerItemsProps extends WheelPickerBaseProps {
  items: number[];
  min?: never;
  max?: never;
}

type WheelPickerProps = WheelPickerRangeProps | WheelPickerItemsProps;

export function WheelPicker(props: WheelPickerProps) {
  const { title, value, onChange } = props;

  const itemList = useMemo(() => {
    if (props.items) return props.items;
    const list: number[] = [];
    for (let i = props.min; i <= props.max; i++) list.push(i);
    return list;
  }, [props.items, props.min, props.max]);

  const currentIndex = useMemo(
    () => Math.max(0, itemList.indexOf(value)),
    [itemList, value],
  );

  const translateY = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    translateY.setValue(0);
  }, [value]);

  const clampIndex = useCallback(
    (idx: number) => Math.max(0, Math.min(itemList.length - 1, idx)),
    [itemList],
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
        const newIndex = clampIndex(currentIndex + steps);
        const newValue = itemList[newIndex];

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

  const prevIndex = currentIndex - 1;
  const nextIndex = currentIndex + 1;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <View style={styles.line} />
      <View style={styles.wheelContainer} {...panResponder.panHandlers}>
        <Animated.View style={[styles.wheelContent, { transform: [{ translateY }] }]}>
          {/* Previous item */}
          <View style={styles.itemWrapper}>
            <View style={styles.perspectiveTop}>
              <Text style={[styles.count, styles.countDimmed]}>
                {prevIndex >= 0 ? String(itemList[prevIndex]) : ""}
              </Text>
            </View>
          </View>

          {/* Selected item */}
          <View style={styles.itemWrapper}>
            <Text style={[styles.count, styles.countSelected]}>
              {String(value)}
            </Text>
          </View>

          {/* Next item */}
          <View style={styles.itemWrapper}>
            <View style={styles.perspectiveBottom}>
              <Text style={[styles.count, styles.countDimmed]}>
                {nextIndex < itemList.length ? String(itemList[nextIndex]) : ""}
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
