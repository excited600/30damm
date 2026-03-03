import { useState } from "react";
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  Pressable,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Alert,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { useCreateGatheringStore } from "@/store/useCreateGatheringStore";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 3;

export default function CreateGatheringPriceScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const store = useCreateGatheringStore();
  const [isPaid, setIsPaid] = useState(!store.isFree);
  const [price, setPrice] = useState(store.price ? String(store.price) : "");
  const [splitByN, setSplitByN] = useState(store.isSplit);

  const handleNext = () => {
    const isFree = !isPaid;
    if (isPaid && (!price.trim() || isNaN(parseInt(price, 10)) || parseInt(price, 10) <= 0)) {
      Alert.alert("알림", "가격을 입력해주세요.");
      return;
    }
    const parsedPrice = isPaid ? parseInt(price, 10) || null : null;
    store.setPrice(isFree, parsedPrice, splitByN);
    router.push("/(gathering)/CreateGatheringCategoryScreen");
  };

  return (
    <KeyboardAvoidingView
      style={[styles.createGatheringPrice, { paddingTop: insets.top }]}
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
          <Text style={styles.titleText}>모임에 비용이 드나요?</Text>
        </View>

        {/* Space */}
        <View style={styles.space} />

        {/* Toggle */}
        <View style={styles.toggleGroup}>
          <Pressable
            style={[
              styles.toggleOption,
              !isPaid && styles.toggleOptionSelected,
            ]}
            onPress={() => setIsPaid(false)}
          >
            <Text
              style={[
                styles.toggleText,
                !isPaid ? styles.toggleTextSelected : styles.toggleTextUnselected,
              ]}
            >
              무료
            </Text>
          </Pressable>
          <Pressable
            style={[
              styles.toggleOption,
              isPaid && styles.toggleOptionSelected,
            ]}
            onPress={() => setIsPaid(true)}
          >
            <Text
              style={[
                styles.toggleText,
                isPaid ? styles.toggleTextSelected : styles.toggleTextUnselected,
              ]}
            >
              유료
            </Text>
          </Pressable>
        </View>

        {/* Price section (only when isPaid) */}
        {isPaid && (
          <>
            <View style={styles.paidSpace} />

            <View style={styles.priceSection}>
              {/* UnderlineInput */}
              <View style={styles.underlineInput}>
                <TextInput
                  style={styles.priceInput}
                  placeholder="₩ 가격을 입력해주세요."
                  placeholderTextColor={colors.text.tertiary}
                  keyboardType="number-pad"
                  value={price}
                  onChangeText={setPrice}
                />
                <View style={styles.underline} />
              </View>

              {/* Checkbox */}
              <Pressable
                style={styles.checkboxRow}
                onPress={() => setSplitByN((v) => !v)}
              >
                <View
                  style={[
                    styles.checkbox,
                    splitByN && styles.checkboxChecked,
                  ]}
                >
                  {splitByN && (
                    <Ionicons
                      name="checkmark"
                      size={18}
                      color={colors.text.primary}
                    />
                  )}
                </View>
                <Text style={styles.checkboxLabel}>
                  1/N 으로 정산하실 건가요?
                </Text>
              </Pressable>
            </View>
          </>
        )}
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
  createGatheringPrice: {
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
  paidSpace: {
    height: 20,
  },
  priceSection: {
    gap: 10,
  },
  underlineInput: {
    gap: 8,
  },
  priceInput: {
    fontSize: 16,
    fontWeight: "400",
    lineHeight: 24,
    color: colors.text.primary,
  },
  underline: {
    height: 1,
    backgroundColor: colors.surface,
  },
  checkboxRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  checkbox: {
    width: 26,
    height: 26,
    borderRadius: 5,
    borderWidth: 2,
    borderColor: colors.accent.primary,
    alignItems: "center",
    justifyContent: "center",
  },
  checkboxChecked: {
    backgroundColor: colors.accent.primary,
  },
  checkboxLabel: {
    fontSize: 16,
    fontWeight: "400",
    lineHeight: 24,
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
