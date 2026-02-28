import { View, Text, StyleSheet, Pressable, Image } from "react-native";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { typography } from "@/shared/constants/typography";

interface GatheringCardProps {
  title: string;
  location: string;
  dateTime: string;
  duration: string;
  participants: string;
  hostName: string;
  hostAvatarUri?: string;
  price: string;
  thumbnailUri?: string;
  onPress?: () => void;
}

export function GatheringCard({
  title,
  location,
  dateTime,
  duration,
  participants,
  hostName,
  hostAvatarUri,
  price,
  thumbnailUri,
  onPress,
}: GatheringCardProps) {
  const subtitle = `${location} · ${dateTime} ${duration} ${participants}`;

  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.container, pressed && styles.pressed]}>
      <View style={styles.content}>
        <View style={styles.thumbnail}>
          {thumbnailUri && (
            <Image source={{ uri: thumbnailUri }} style={styles.thumbnailImage} />
          )}
        </View>
        <View style={styles.textGroup}>
          <Text style={styles.title} numberOfLines={1}>
            {title}
          </Text>
          <Text style={styles.subtitle} numberOfLines={1}>
            {subtitle}
          </Text>
          <View style={styles.hostPriceRow}>
            <View style={styles.host}>
              {hostAvatarUri ? (
                <Image source={{ uri: hostAvatarUri }} style={styles.avatar} />
              ) : (
                <View style={[styles.avatar, styles.avatarPlaceholder]} />
              )}
              <Text style={styles.hostName}>{hostName}</Text>
            </View>
            <Text style={styles.price}>{price}</Text>
          </View>
        </View>
      </View>
      <View style={styles.divider} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: spacing.md,
    paddingVertical: 20,
    gap: 12,
  },
  pressed: {
    opacity: 0.7,
  },
  content: {
    flexDirection: "row",
    gap: 10,
    alignItems: "center",
  },
  thumbnail: {
    width: 80,
    height: 80,
    borderRadius: 8,
    backgroundColor: colors.surface,
    overflow: "hidden",
  },
  thumbnailImage: {
    width: "100%",
    height: "100%",
  },
  textGroup: {
    flex: 1,
    gap: 10,
  },
  title: {
    ...typography.body.lg,
    color: colors.text.primary,
  },
  subtitle: {
    ...typography.body.sm,
    color: colors.text.secondary,
  },
  hostPriceRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  host: {
    flexDirection: "row",
    alignItems: "center",
    gap: 5,
  },
  avatar: {
    width: 20,
    height: 20,
    borderRadius: 10,
    overflow: "hidden",
  },
  avatarPlaceholder: {
    backgroundColor: colors.surface,
  },
  hostName: {
    ...typography.label.sm,
    color: colors.text.primary,
  },
  price: {
    ...typography.label.sm,
    color: colors.text.primary,
  },
  divider: {
    height: 1,
    backgroundColor: colors.surface,
  },
});
