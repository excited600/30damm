import { View, Text, StyleSheet, Pressable } from "react-native";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import type { Gathering } from "@/api/types/gathering";

interface GatheringCardProps {
  gathering: Gathering;
  onPress?: () => void;
}

export function GatheringCard({ gathering, onPress }: GatheringCardProps) {
  return (
    <Pressable onPress={onPress} style={styles.container}>
      <Text style={styles.title}>{gathering.title}</Text>
      <Text style={styles.description} numberOfLines={2}>
        {gathering.description}
      </Text>
      <View style={styles.footer}>
        <Text style={styles.participants}>
          {gathering.currentParticipants}/{gathering.maxParticipants}명
        </Text>
        <Text style={styles.status}>{gathering.status}</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.background,
    borderRadius: 12,
    padding: spacing.md,
    marginBottom: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    color: colors.text.primary,
    marginBottom: spacing.xs,
  },
  description: {
    fontSize: 14,
    color: colors.text.secondary,
    marginBottom: spacing.sm,
  },
  footer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  participants: {
    fontSize: 13,
    color: colors.text.tertiary,
  },
  status: {
    fontSize: 13,
    color: colors.primary,
    fontWeight: "500",
  },
});
