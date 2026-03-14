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
const VISIBLE_ITEMS = 3;

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

  // scrollY represents the offset from the position where currentIndex is centered.
  // Negative = scrolled up (showing higher indices), Positive = scrolled down (showing lower indices).
  const scrollY = useRef(new Animated.Value(0)).current;
  const scrollYOffset = useRef(0);

  useEffect(() => {
    scrollY.setValue(0);
    scrollYOffset.current = 0;
  }, [value, scrollY]);

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
      onPanResponderMove: (_, gestureState) => {
        const clamped = clampOffsetRef.current(gestureState.dy);
        scrollY.setValue(clamped);
        scrollYOffset.current = clamped;
      },
      onPanResponderRelease: (_, gestureState) => {
        const clamped = clampOffsetRef.current(gestureState.dy);
        // Snap to nearest item
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
            const offset = index - currentIndex;
            return (
              <View
                key={item}
                style={[
                  styles.itemWrapper,
                  {
                    position: "absolute",
                    top: (offset + 1) * ITEM_HEIGHT,
                  },
                ]}
              >
                <Text
                  style={[
                    styles.count,
                    offset === 0 ? styles.countSelected : styles.countDimmed,
                  ]}
                >
                  {String(item)}
                </Text>
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
    height: ITEM_HEIGHT * VISIBLE_ITEMS,
    overflow: "hidden",
    justifyContent: "center",
  },
  wheelContent: {
    width: 41,
    height: ITEM_HEIGHT * VISIBLE_ITEMS,
  },
  itemWrapper: {
    height: ITEM_HEIGHT,
    justifyContent: "center",
    alignItems: "center",
    width: 41,
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
