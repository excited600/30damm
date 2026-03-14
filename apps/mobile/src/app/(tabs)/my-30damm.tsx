import { View, Text, StyleSheet, ScrollView, Pressable, Alert } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";
import { useAuthStore } from "@/store/useAuthStore";
import { useDeleteUser } from "@/features/auth/hooks/useAuth";

function MenuSection({ children }: { children: React.ReactNode }) {
  return <View style={styles.menuSection}>{children}</View>;
}

function MenuTitle({ title }: { title: string }) {
  return (
    <View style={styles.menuTitle}>
      <Text style={styles.menuTitleText}>{title}</Text>
    </View>
  );
}

function MenuWithRightIcon({
  label,
  onPress,
}: {
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable style={styles.menuItem} onPress={onPress}>
      <Text style={styles.menuItemText}>{label}</Text>
      <Ionicons name="chevron-forward" size={26} color={colors.text.primary} />
    </Pressable>
  );
}

export default function My30dammScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const logout = useAuthStore((s) => s.logout);
  const deleteUser = useDeleteUser();

  const handleLogout = () => {
    Alert.alert("로그아웃", "로그아웃 하시겠습니까?", [
      { text: "취소", style: "cancel" },
      {
        text: "확인",
        onPress: () => {
          logout();
          router.replace("/welcome" as any);
        },
      },
    ]);
  };

  const handleDeleteUser = () => {
    Alert.alert("탈퇴하기", "탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.", [
      { text: "취소", style: "cancel" },
      {
        text: "탈퇴",
        style: "destructive",
        onPress: () => {
          deleteUser.mutate(undefined, {
            onSuccess: () => {
              router.replace("/(auth)/login" as any);
            },
          });
        },
      },
    ]);
  };

  return (
    <View style={[styles.screen, { paddingTop: insets.top }]}>
      <ScrollView
        style={styles.content}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        <MenuSection>
          <MenuTitle title="약관 및 정책" />
          <MenuWithRightIcon
            label="개인정보 처리방침"
            onPress={() =>
              router.push("/(gathering)/PrivacyPolicyScreen" as any)
            }
          />
          <MenuWithRightIcon
            label="이용약관"
            onPress={() =>
              router.push("/(gathering)/TermsOfServiceScreen" as any)
            }
          />
        </MenuSection>

        <MenuSection>
          <MenuTitle title="나가기" />
          <MenuWithRightIcon label="로그아웃" onPress={handleLogout} />
          <MenuWithRightIcon
            label="탈퇴하기"
            onPress={handleDeleteUser}
          />
        </MenuSection>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 10,
    gap: 10,
  },
  content: {
    flex: 1,
  },
  contentContainer: {
    gap: 10,
    paddingHorizontal: 15,
    paddingTop: "10%",
  },
  menuSection: {
    backgroundColor: colors.surface,
    borderRadius: 25,
    padding: 8,
    gap: 10,
    overflow: "hidden",
  },
  menuTitle: {
    padding: 10,
  },
  menuTitleText: {
    fontSize: 20,
    fontWeight: "400",
    color: colors.text.primary,
  },
  menuItem: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    padding: 10,
  },
  menuItemText: {
    fontSize: 18,
    fontWeight: "400",
    color: colors.text.primary,
  },
});
