import { useEffect } from "react";
import { Text, Pressable, StyleSheet, Modal } from "react-native";
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from "react-native-reanimated";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";

interface ProfileMenuBottomSheetProps {
  visible: boolean;
  onClose: () => void;
  onReport: () => void;
}

export function ProfileMenuBottomSheet({ visible, onClose, onReport }: ProfileMenuBottomSheetProps) {
  const insets = useSafeAreaInsets();
  const translateY = useSharedValue(300);
  const backdropOpacity = useSharedValue(0);

  useEffect(() => {
    if (visible) {
      translateY.value = withTiming(0, { duration: 300 });
      backdropOpacity.value = withTiming(1, { duration: 300 });
    } else {
      translateY.value = withTiming(300, { duration: 250 });
      backdropOpacity.value = withTiming(0, { duration: 250 });
    }
  }, [visible, translateY, backdropOpacity]);

  const sheetStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
  }));

  const backdropStyle = useAnimatedStyle(() => ({
    opacity: backdropOpacity.value,
  }));

  if (!visible) return null;

  return (
    <Modal transparent visible={visible} animationType="none" onRequestClose={onClose}>
      <Animated.View style={[styles.backdrop, backdropStyle]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
      </Animated.View>
      <Animated.View style={[styles.sheet, { paddingBottom: Math.max(insets.bottom, 16) }, sheetStyle]}>
        <Pressable style={styles.menuItem} onPress={() => {}}>
          <Text style={styles.menuText}>차단하기</Text>
        </Pressable>
        <Pressable style={styles.menuItem} onPress={onReport}>
          <Text style={styles.menuText}>신고하기</Text>
        </Pressable>
        <Pressable style={styles.menuItem} onPress={onClose}>
          <Text style={styles.menuText}>닫기</Text>
        </Pressable>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: "rgba(0, 0, 0, 0.5)",
  },
  sheet: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.bg.tertiary,
    borderTopLeftRadius: 30,
    borderTopRightRadius: 30,
    paddingTop: 12,
    paddingHorizontal: 18,
    gap: 20,
    alignItems: "center",
  },
  menuItem: {
    padding: 10,
  },
  menuText: {
    fontSize: 16,
    fontWeight: "700",
    lineHeight: 24,
    color: colors.text.primary,
  },
});
