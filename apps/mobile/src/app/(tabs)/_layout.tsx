import { Tabs } from "expo-router";

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: true,
        tabBarActiveTintColor: "#007AFF",
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "홈",
          tabBarLabel: "홈",
        }}
      />
      <Tabs.Screen
        name="search"
        options={{
          title: "탐색",
          tabBarLabel: "탐색",
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "프로필",
          tabBarLabel: "프로필",
        }}
      />
    </Tabs>
  );
}
