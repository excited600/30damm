import { useEffect, useRef } from "react";
import { View, Text, StyleSheet, Animated } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";

interface ToastProps {
  message: string;
  visible: boolean;
  duration?: number;
  onHide?: () => void;
}

export function Toast({ message, visible, duration = 2000, onHide }: ToastProps) {
  const insets = useSafeAreaInsets();
  const translateY = useRef(new Animated.Value(100)).current;
  const opacity = useRef(new Animated.Value(0)).current;
  const onHideRef = useRef(onHide);
  useEffect(() => { onHideRef.current = onHide; }, [onHide]);

  useEffect(() => {
    if (visible) {
      translateY.stopAnimation();
      opacity.stopAnimation();
      translateY.setValue(100);
      opacity.setValue(0);

      Animated.parallel([
        Animated.spring(translateY, {
          toValue: 0,
          useNativeDriver: true,
          tension: 80,
          friction: 12,
        }),
        Animated.timing(opacity, {
          toValue: 1,
          duration: 200,
          useNativeDriver: true,
        }),
      ]).start();

      const timer = setTimeout(() => {
        Animated.parallel([
          Animated.timing(translateY, {
            toValue: 100,
            duration: 300,
            useNativeDriver: true,
          }),
          Animated.timing(opacity, {
            toValue: 0,
            duration: 300,
            useNativeDriver: true,
          }),
        ]).start(() => {
          onHideRef.current?.();
        });
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [visible, duration, translateY, opacity]);

  if (!visible) return null;

  return (
    <Animated.View
      style={[
        styles.container,
        {
          bottom: Math.max(insets.bottom, 16) + 60,
          transform: [{ translateY }],
          opacity,
        },
      ]}
    >
      <View style={styles.toast}>
        <Text style={styles.message}>{message}</Text>
      </View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: "absolute",
    left: 20,
    right: 20,
    alignItems: "center",
    zIndex: 999,
  },
  toast: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    paddingHorizontal: 24,
    paddingVertical: 16,
    width: "100%",
    alignItems: "center",
    justifyContent: "center",
  },
  message: {
    fontSize: 16,
    fontWeight: "700",
    lineHeight: 24,
    color: colors.text.primary,
    textAlign: "center",
  },
});
