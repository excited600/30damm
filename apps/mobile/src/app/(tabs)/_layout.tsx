import { Tabs } from "expo-router";
import { colors } from "@/shared/constants/colors";
import { HomeIcon, MeIcon } from "@/shared/components/ui/TabIcons";

export default function TabLayout() {
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
        name="profile"
        options={{
          tabBarIcon: ({ color }) => <MeIcon color={color} />,
        }}
      />
    </Tabs>
  );
}
