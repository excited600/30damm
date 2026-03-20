import { useEffect, useState } from "react";
import { View, Text, Pressable, StyleSheet, Modal } from "react-native";
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from "react-native-reanimated";
import Ionicons from "@expo/vector-icons/Ionicons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "@/shared/constants/colors";
import { Button } from "./Button";

interface AgreementBottomSheetProps {
  visible: boolean;
  onClose: () => void;
  onConfirm: () => void;
  onTerms: () => void;
  onPrivacy: () => void;
}

export function AgreementBottomSheet({
  visible,
  onClose,
  onConfirm,
  onTerms,
  onPrivacy,
}: AgreementBottomSheetProps) {
  const insets = useSafeAreaInsets();
  const translateY = useSharedValue(400);
  const backdropOpacity = useSharedValue(0);

  const [termsChecked, setTermsChecked] = useState(false);
  const [privacyChecked, setPrivacyChecked] = useState(false);

  const allChecked = termsChecked && privacyChecked;

  useEffect(() => {
    if (visible) {
      translateY.value = withTiming(0, { duration: 300 });
      backdropOpacity.value = withTiming(1, { duration: 300 });
    } else {
      translateY.value = withTiming(400, { duration: 250 });
      backdropOpacity.value = withTiming(0, { duration: 250 });
    }
  }, [visible, translateY, backdropOpacity]);

  const sheetStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
  }));

  const backdropStyle = useAnimatedStyle(() => ({
    opacity: backdropOpacity.value,
  }));

  const handleToggleAll = () => {
    const next = !allChecked;
    setTermsChecked(next);
    setPrivacyChecked(next);
  };

  if (!visible) return null;

  return (
    <Modal transparent visible={visible} animationType="none" onRequestClose={onClose}>
      <Animated.View style={[styles.backdrop, backdropStyle]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
      </Animated.View>
      <Animated.View
        style={[styles.sheet, { paddingBottom: Math.max(insets.bottom, 16) }, sheetStyle]}
      >
        {/* Handle */}
        <View style={styles.handle} />

        {/* Title */}
        <Text style={styles.title}>서티담 이용을 위해 동의가 필요해요</Text>

        {/* 모두 동의 */}
        <Pressable style={styles.allAgreeRow} onPress={handleToggleAll}>
          <Ionicons
            name={allChecked ? "checkmark-circle" : "ellipse-outline"}
            size={21}
            color={allChecked ? colors.accent.primary : colors.text.tertiary}
          />
          <View style={styles.allAgreeTextGroup}>
            <Text style={styles.allAgreeLabel}>모두 동의</Text>
            <Text style={styles.allAgreeDesc}>
              서비스 이용에 필수적인 최소한의 개인정보{"\n"}수집 및 이용에 동의합니다.
            </Text>
          </View>
        </Pressable>

        {/* Divider */}
        <View style={styles.divider} />

        {/* 개별 약관 */}
        <View style={styles.itemGroup}>
          <View style={styles.itemRow}>
            <Pressable
              style={styles.itemLeft}
              onPress={() => setTermsChecked((v) => !v)}
            >
              <Ionicons
                name="checkmark"
                size={16}
                color={termsChecked ? colors.accent.primary : colors.text.tertiary}
              />
              <Text style={styles.itemLabel}>(필수) 서비스 이용 약관</Text>
            </Pressable>
            <Pressable onPress={onTerms} hitSlop={8}>
              <Ionicons name="chevron-forward" size={16} color={colors.text.tertiary} />
            </Pressable>
          </View>

          <View style={styles.itemRow}>
            <Pressable
              style={styles.itemLeft}
              onPress={() => setPrivacyChecked((v) => !v)}
            >
              <Ionicons
                name="checkmark"
                size={16}
                color={privacyChecked ? colors.accent.primary : colors.text.tertiary}
              />
              <Text style={styles.itemLabel}>(필수) 개인정보처리 관련 고지사항</Text>
            </Pressable>
            <Pressable onPress={onPrivacy} hitSlop={8}>
              <Ionicons name="chevron-forward" size={16} color={colors.text.tertiary} />
            </Pressable>
          </View>
        </View>

        {/* 확인 버튼 */}
        <View style={[styles.confirmWrapper, !allChecked && styles.confirmWrapperDisabled]}>
          <Button
            label="확인"
            onPress={allChecked ? onConfirm : undefined}
            disabled={!allChecked}
            color={colors.accent.primary}
            labelColor={colors.text.primary}
            style={styles.confirmButton}
          />
        </View>
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
  handle: {
    width: 40,
    height: 6,
    borderRadius: 3,
    backgroundColor: colors.text.tertiary,
  },
  title: {
    fontSize: 16,
    fontWeight: "700",
    lineHeight: 24,
    color: colors.text.primary,
    textAlign: "center",
  },
  allAgreeRow: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 10,
    width: "100%",
  },
  allAgreeTextGroup: {
    flex: 1,
    gap: 4,
  },
  allAgreeLabel: {
    fontSize: 14,
    fontWeight: "700",
    lineHeight: 20,
    color: colors.text.primary,
  },
  allAgreeDesc: {
    fontSize: 11,
    fontWeight: "400",
    lineHeight: 14,
    color: colors.text.tertiary,
  },
  divider: {
    height: 1,
    width: "100%",
    backgroundColor: colors.border,
  },
  itemGroup: {
    width: "100%",
    gap: 6,
  },
  itemRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    width: "100%",
  },
  itemLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    flex: 1,
  },
  itemLabel: {
    fontSize: 12,
    fontWeight: "400",
    lineHeight: 18,
    color: colors.text.secondary,
  },
  confirmWrapper: {
    width: "100%",
  },
  confirmWrapperDisabled: {
    opacity: 0.4,
  },
  confirmButton: {
    width: "100%",
  },
});
