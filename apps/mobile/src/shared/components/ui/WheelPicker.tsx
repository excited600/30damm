import { useRef, useState, useCallback, useEffect, useMemo } from "react";
import {
  View,
  Text,
  StyleSheet,
  Animated,
  PanResponder,
} from "react-native";
import { colors } from "@/shared/constants/colors";

const ITEM_HEIGHT = 30;
const VISIBLE_ITEMS = 3;
const TOUCH_WIDTH = 70;

interface WheelPickerBaseProps {
  title: string;
  value: number;
  onChange: (value: number) => void;
  onDragStart?: () => void;
  onDragEnd?: () => void;
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
  const { title, value, onChange, onDragStart, onDragEnd } = props;

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

  // scrollY represents the offset from the position where currentIndex is centered.
  // Negative = scrolled up (showing higher indices), Positive = scrolled down (showing lower indices).
  const scrollY = useRef(new Animated.Value(0)).current;
  const scrollYOffset = useRef(0);
  // Tracks which index is visually centered during drag (for styling)
  const [visualCenterIndex, setVisualCenterIndex] = useState(currentIndex);

  useEffect(() => {
    scrollY.setValue(0);
    scrollYOffset.current = 0;
    setVisualCenterIndex(currentIndex);
  }, [value, scrollY, currentIndex]);

  const currentIndexRef = useRef(currentIndex);
  useEffect(() => {
    currentIndexRef.current = currentIndex;
  }, [currentIndex]);

  const itemListRef = useRef(itemList);
  useEffect(() => {
    itemListRef.current = itemList;
  }, [itemList]);

  const onChangeRef = useRef(onChange);
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  const onDragStartRef = useRef(onDragStart);
  useEffect(() => {
    onDragStartRef.current = onDragStart;
  }, [onDragStart]);

  const onDragEndRef = useRef(onDragEnd);
  useEffect(() => {
    onDragEndRef.current = onDragEnd;
  }, [onDragEnd]);

  const setVisualCenterIndexRef = useRef(setVisualCenterIndex);
  useEffect(() => {
    setVisualCenterIndexRef.current = setVisualCenterIndex;
  }, [setVisualCenterIndex]);

  const clampOffset = useCallback(
    (offset: number) => {
      const maxUp = -(itemList.length - 1 - currentIndex) * ITEM_HEIGHT;
      const maxDown = currentIndex * ITEM_HEIGHT;
      return Math.max(maxUp, Math.min(maxDown, offset));
    },
    [itemList, currentIndex],
  );

  const clampOffsetRef = useRef(clampOffset);
  useEffect(() => {
    clampOffsetRef.current = clampOffset;
  }, [clampOffset]);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onStartShouldSetPanResponderCapture: () => true,
      onMoveShouldSetPanResponderCapture: () => true,
      onPanResponderTerminationRequest: () => false,
      onPanResponderGrant: () => {
        onDragStartRef.current?.();
      },
      onPanResponderMove: (_, gestureState) => {
        const clamped = clampOffsetRef.current(gestureState.dy);
        scrollY.setValue(clamped);
        scrollYOffset.current = clamped;
        const steps = Math.round(-clamped / ITEM_HEIGHT);
        const visualIdx = Math.max(
          0,
          Math.min(
            itemListRef.current.length - 1,
            currentIndexRef.current + steps,
          ),
        );
        setVisualCenterIndexRef.current(visualIdx);
      },
      onPanResponderRelease: (_, gestureState) => {
        onDragEndRef.current?.();
        const clamped = clampOffsetRef.current(gestureState.dy);
        const steps = Math.round(-clamped / ITEM_HEIGHT);
        const newIndex = Math.max(
          0,
          Math.min(
            itemListRef.current.length - 1,
            currentIndexRef.current + steps,
          ),
        );
        const snappedOffset =
          -(newIndex - currentIndexRef.current) * ITEM_HEIGHT;

        Animated.spring(scrollY, {
          toValue: snappedOffset,
          useNativeDriver: true,
          tension: 100,
          friction: 12,
        }).start(() => {
          const newValue = itemListRef.current[newIndex];
          if (newValue !== undefined) {
            onChangeRef.current(newValue);
          }
        });
      },
      onPanResponderTerminate: () => {
        onDragEndRef.current?.();
      },
    }),
  ).current;

  // Render all items, positioned relative to the center slot
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <View style={styles.line} />
      <View style={styles.wheelContainer} {...panResponder.panHandlers}>
        <Animated.View
          style={[
            styles.wheelContent,
            {
              transform: [{ translateY: Animated.add(scrollY, 0) }],
            },
          ]}
        >
          {itemList.map((item, index) => {
            const posOffset = index - currentIndex;
            const visualOffset = index - visualCenterIndex;
            const perspectiveStyle =
              visualOffset < 0
                ? styles.perspectiveTop
                : visualOffset > 0
                  ? styles.perspectiveBottom
                  : undefined;
            return (
              <View
                key={item}
                style={[
                  styles.itemWrapper,
                  {
                    position: "absolute",
                    top: (posOffset + 1) * ITEM_HEIGHT,
                  },
                ]}
              >
                <View style={perspectiveStyle}>
                  <Text
                    style={[
                      styles.count,
                      visualOffset === 0
                        ? styles.countSelected
                        : styles.countDimmed,
                    ]}
                  >
                    {String(item)}
                  </Text>
                </View>
              </View>
            );
          })}
        </Animated.View>
      </View>
      <View style={styles.line} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: TOUCH_WIDTH,
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
    width: TOUCH_WIDTH,
    height: ITEM_HEIGHT * VISIBLE_ITEMS,
    overflow: "hidden",
    justifyContent: "center",
  },
  wheelContent: {
    width: TOUCH_WIDTH,
    height: ITEM_HEIGHT * VISIBLE_ITEMS,
  },
  itemWrapper: {
    height: ITEM_HEIGHT,
    justifyContent: "center",
    alignItems: "center",
    width: TOUCH_WIDTH,
  },
  count: {
    fontSize: 18,
    fontWeight: "600",
    lineHeight: 26,
    textAlign: "center",
    width: TOUCH_WIDTH,
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
  countSelected: {
    color: colors.text.primary,
  },
  countDimmed: {
    color: "#6B6B6B",
  },
});
