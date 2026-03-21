import { Tabs, Redirect } from "expo-router";
import { colors } from "@/shared/constants/colors";
import { HomeIcon, MeIcon } from "@/shared/components/ui/TabIcons";
import { useAuthStore } from "@/store/useAuthStore";

export default function TabLayout() {
  const token = useAuthStore((s) => s.token);

  if (!token) {
    return <Redirect href="/welcome" />;
  }

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.text.primary,
        tabBarInactiveTintColor: colors.text.tertiary,
        tabBarShowLabel: false,
        tabBarStyle: {
          backgroundColor: colors.background,
          borderTopWidth: 1,
          borderTopColor: colors.surface,
          paddingTop: 8,
          paddingBottom: 24,
          paddingHorizontal: 40,
          height: 53,
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          tabBarIcon: ({ color }) => <HomeIcon color={color} />,
        }}
      />
      <Tabs.Screen
        name="chat"
        options={{
          href: null,
        }}
      />
      <Tabs.Screen
        name="my-30damm"
        options={{
          tabBarIcon: ({ color }) => <MeIcon color={color} />,
        }}
      />
    </Tabs>
  );
}
