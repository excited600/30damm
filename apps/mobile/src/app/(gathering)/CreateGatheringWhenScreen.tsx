import { useState, useCallback } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  Alert,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { Calendar, LocaleConfig } from "react-native-calendars";
import { useMutation } from "@tanstack/react-query";
import { colors } from "@/shared/constants/colors";
import { Button } from "@/shared/components/ui/Button";
import { WheelPicker } from "@/shared/components/ui/WheelPicker";
import { useCreateGatheringStore } from "@/store/useCreateGatheringStore";
import { gatheringClient } from "@/api/clients/gatheringClient";
import type { OpenGatheringRequest } from "@/api/types/gathering";

const TOTAL_STEPS = 7;
const CURRENT_STEP = 7;

const DAY_NAMES = ["일", "월", "화", "수", "목", "금", "토"];

LocaleConfig.locales["ko"] = {
  monthNames: [
    "1월", "2월", "3월", "4월", "5월", "6월",
    "7월", "8월", "9월", "10월", "11월", "12월",
  ],
  monthNamesShort: [
    "1월", "2월", "3월", "4월", "5월", "6월",
    "7월", "8월", "9월", "10월", "11월", "12월",
  ],
  dayNames: ["일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"],
  dayNamesShort: ["일", "월", "화", "수", "목", "금", "토"],
  today: "오늘",
};
LocaleConfig.defaultLocale = "ko";

type ExpandedSection = "date" | "time" | "duration" | null;

export default function CreateGatheringWhenScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const store = useCreateGatheringStore();

  const [expandedSection, setExpandedSection] = useState<ExpandedSection>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(store.date);
  const [hour, setHour] = useState(12);
  const [minute, setMinute] = useState(0);
  const [durationHour, setDurationHour] = useState(0);
  const [durationMinute, setDurationMinute] = useState(0);

  const openGathering = useMutation({
    mutationFn: (request: OpenGatheringRequest) => gatheringClient.open(request),
  });

  const toggleSection = useCallback(
    (section: ExpandedSection) => {
      setExpandedSection((prev) => (prev === section ? null : section));
    },
    [],
  );

  const formatDate = () => {
    if (!selectedDate) return "- 월 - 일 (- 요일)";
    const [, month, day] = selectedDate.split("-").map(Number);
    const d = new Date(Number(selectedDate.split("-")[0]), month - 1, day);
    const dayOfWeek = DAY_NAMES[d.getDay()];
    return `${month}월 ${day}일 (${dayOfWeek})`;
  };

  const formatTime = () => {
    if (hour === 12 && minute === 0 && expandedSection !== "time") {
      return "오후 · 시 · 분";
    }
    const period = hour < 12 ? "오전" : "오후";
    const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
    return `${period} ${displayHour}시 ${minute}분`;
  };

  const formatDuration = () => {
    if (durationHour === 0 && durationMinute === 0) return "미정";
    const parts = [];
    if (durationHour > 0) parts.push(`${durationHour}시간`);
    if (durationMinute > 0) parts.push(`${durationMinute}분`);
    return parts.join(" ");
  };

  const markedDates = selectedDate
    ? {
        [selectedDate]: {
          selected: true,
          selectedColor: colors.accent.primary,
          selectedTextColor: colors.background,
        },
      }
    : {};

  const handleOpenGathering = () => {
    const startTime = `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
    const durationMinutes = durationHour * 60 + durationMinute;

    store.setWhen(
      selectedDate,
      startTime,
      durationMinutes > 0 ? durationMinutes : null,
    );

    const request: OpenGatheringRequest = {
      title: store.title,
      description: store.description,
      category: store.category,
      location: store.location,
      date: selectedDate,
      startTime,
      duration: durationMinutes > 0 ? durationMinutes : null,
      minCapacity: store.minCapacity,
      maxCapacity: store.maxCapacity,
      isGenderRatioEnabled: store.isGenderRatioEnabled,
      maxMaleCapacity: store.maxMaleCapacity,
      maxFemaleCapacity: store.maxFemaleCapacity,
      isFree: store.isFree,
      price: store.price,
      isSplit: store.isSplit,
    };

    openGathering.mutate(request, {
      onSuccess: (data) => {
        store.reset();
        router.replace({
          pathname: "/(gathering)/GatheringDetailScreen",
          params: { gatheringUuid: data.gatheringUuid, showToast: "true" },
        });
      },
      onError: () => {
        Alert.alert("오류", "모임 생성에 실패했습니다. 다시 시도해주세요.");
      },
    });
  };

  return (
    <View style={[styles.createGatheringWhen, { paddingTop: insets.top }]}>
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
        showsVerticalScrollIndicator={false}
      >
        {/* Title */}
        <View style={styles.title}>
          <Text style={styles.titleText}>언제 만날까요?</Text>
        </View>

        {/* Date Field */}
        <Pressable
          style={styles.formField}
          onPress={() => toggleSection("date")}
        >
          <Text style={styles.fieldLabel}>날짜를 정해주세요</Text>
          <View style={styles.valueRow}>
            <Ionicons name="calendar-outline" size={22} color={colors.text.primary} />
            <Text
              style={[
                styles.fieldValue,
                selectedDate && styles.fieldValueFilled,
              ]}
            >
              {formatDate()}
            </Text>
          </View>
          <View style={styles.underline} />
        </Pressable>

        {/* Calendar */}
        {expandedSection === "date" && (
          <View style={styles.calendarContainer}>
            <Calendar
              onDayPress={(day: { dateString: string }) => {
                setSelectedDate(day.dateString);
              }}
              markedDates={markedDates}
              minDate={new Date().toISOString().split("T")[0]}
              theme={{
                backgroundColor: colors.surface,
                calendarBackground: colors.surface,
                textSectionTitleColor: colors.text.secondary,
                selectedDayBackgroundColor: colors.accent.primary,
                selectedDayTextColor: colors.background,
                todayTextColor: colors.accent.primary,
                dayTextColor: colors.text.primary,
                textDisabledColor: colors.text.tertiary,
                monthTextColor: colors.text.primary,
                arrowColor: colors.accent.primary,
                textDayFontSize: 16,
                textMonthFontSize: 16,
                textDayHeaderFontSize: 13,
                textDayFontWeight: "400" as const,
                textMonthFontWeight: "700" as const,
                textDayHeaderFontWeight: "600" as const,
              }}
              style={styles.calendar}
            />
          </View>
        )}

        {/* Time Field */}
        <Pressable
          style={styles.formField}
          onPress={() => toggleSection("time")}
        >
          <Text style={styles.fieldLabel}>시작 시간을 정해주세요</Text>
          <View style={styles.valueRow}>
            <Ionicons name="time-outline" size={22} color={colors.text.primary} />
            <Text
              style={[
                styles.fieldValue,
                expandedSection === "time" && styles.fieldValueFilled,
              ]}
            >
              {formatTime()}
            </Text>
          </View>
          <View style={styles.underline} />
        </Pressable>

        {/* Time Pickers */}
        {expandedSection === "time" && (
          <View style={styles.pickersRow}>
            <WheelPicker
              title="시"
              min={0}
              max={23}
              value={hour}
              onChange={setHour}
            />
            <WheelPicker
              title="분"
              items={[0, 30]}
              value={minute}
              onChange={setMinute}
            />
          </View>
        )}

        {/* Duration Field */}
        <Pressable
          style={styles.formField}
          onPress={() => toggleSection("duration")}
        >
          <Text style={styles.fieldLabel}>얼마나 오래 모일까요?</Text>
          <View style={styles.valueRow}>
            <Ionicons name="hourglass-outline" size={22} color={colors.text.primary} />
            <Text
              style={[
                styles.fieldValue,
                (durationHour > 0 || durationMinute > 0) &&
                  styles.fieldValueFilled,
              ]}
            >
              {formatDuration()}
            </Text>
          </View>
          <View style={styles.underline} />
        </Pressable>

        {/* Duration Pickers */}
        {expandedSection === "duration" && (
          <View style={styles.pickersRow}>
            <WheelPicker
              title="시간"
              min={0}
              max={8}
              value={durationHour}
              onChange={setDurationHour}
            />
            <WheelPicker
              title="분"
              items={[0]}
              value={durationMinute}
              onChange={setDurationMinute}
            />
          </View>
        )}

        {/* Bottom spacing for scroll */}
        <View style={styles.scrollSpacer} />
      </ScrollView>

      {/* BottomCTAOnlyButton */}
      <View
        style={[styles.bottomCTA, { paddingBottom: Math.max(insets.bottom, 16) }]}
      >
        <Button
          label={openGathering.isPending ? "생성 중..." : "모임 열기"}
          onPress={handleOpenGathering}
          disabled={openGathering.isPending}
          color={colors.accent.primary}
          labelColor={colors.text.primary}
          style={styles.button}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  createGatheringWhen: {
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
  formField: {
    gap: 8,
    marginBottom: 10,
  },
  fieldLabel: {
    fontSize: 14,
    fontWeight: "400",
    lineHeight: 20,
    color: colors.text.primary,
  },
  valueRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  fieldValue: {
    fontSize: 14,
    fontWeight: "700",
    lineHeight: 20,
    color: colors.text.tertiary,
  },
  fieldValueFilled: {
    color: colors.text.primary,
  },
  underline: {
    height: 1,
    backgroundColor: colors.surface,
  },
  calendarContainer: {
    marginBottom: 10,
    borderRadius: 13,
    overflow: "hidden",
  },
  calendar: {
    borderRadius: 13,
  },
  pickersRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 33,
    paddingVertical: 10,
    marginBottom: 10,
  },
  scrollSpacer: {
    height: 20,
  },
  bottomCTA: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  button: {
    width: "100%",
  },
});
