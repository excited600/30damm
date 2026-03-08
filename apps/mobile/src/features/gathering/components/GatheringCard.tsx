import { memo, useMemo } from "react";
import { View, Text, StyleSheet, Pressable, Image } from "react-native";
import { colors } from "@/shared/constants/colors";
import { spacing } from "@/shared/constants/spacing";
import { typography } from "@/shared/constants/typography";

const EMOJIS = ["😀", "😎", "🤩", "🥳", "😺", "🐶", "🐱", "🦊", "🐻", "🐼", "🐸", "🐵", "🦁", "🐯", "🐰", "🐨", "🐷", "🌸", "🌺", "🍀", "🔥", "⭐", "🎉", "🎈", "🍕", "🎸", "🏀", "⚽", "🎮", "🚀"];

function getRandomEmoji(seed: string): string {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0;
  }
  return EMOJIS[Math.abs(hash) % EMOJIS.length];
}

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

export const GatheringCard = memo(function GatheringCard({
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
  const thumbnailEmoji = useMemo(() => getRandomEmoji(title), [title]);
  const hostEmoji = useMemo(() => getRandomEmoji(hostName), [hostName]);

  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.container, pressed && styles.pressed]}>
      <View style={styles.content}>
        <View style={styles.thumbnail}>
          {thumbnailUri ? (
            <Image source={{ uri: thumbnailUri }} style={styles.thumbnailImage} />
          ) : (
            <Text style={styles.thumbnailEmoji}>{thumbnailEmoji}</Text>
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
                <View style={[styles.avatar, styles.avatarPlaceholder]}>
                  <Text style={styles.avatarEmoji}>{hostEmoji}</Text>
                </View>
              )}
              <Text style={styles.hostName} numberOfLines={1}>{hostName}</Text>
            </View>
            <View style={styles.priceBadge}>
              <Text style={styles.price} numberOfLines={1}>{price}</Text>
            </View>
          </View>
        </View>
      </View>
      <View style={styles.divider} />
    </Pressable>
  );
});

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: spacing.md,
    paddingVertical: 5,
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
  thumbnailEmoji: {
    fontSize: 36,
    textAlign: "center",
    lineHeight: 80,
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
    flex: 1,
    minWidth: 0,
  },
  avatar: {
    width: 20,
    height: 20,
    borderRadius: 10,
    overflow: "hidden",
  },
  avatarPlaceholder: {
    backgroundColor: colors.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  avatarEmoji: {
    fontSize: 14,
  },
  hostName: {
    ...typography.label.sm,
    color: colors.text.primary,
    flexShrink: 1,
  },
  priceBadge: {
    backgroundColor: colors.text.secondary,
    borderRadius: 5,
    padding: 3,
  },
  price: {
    ...typography.label.sm,
    color: colors.text.primary,
  },
  divider: {
    height: 1,
    backgroundColor: colors.surface,
    opacity: 0.2,
  },
});
