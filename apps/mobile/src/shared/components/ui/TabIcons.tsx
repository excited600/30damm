import Ionicons from "@expo/vector-icons/Ionicons";

interface TabIconProps {
  color: string;
}

export function HomeIcon({ color }: TabIconProps) {
  return <Ionicons name="home" size={21} color={color} />;
}

export function ChatIcon({ color }: TabIconProps) {
  return <Ionicons name="chatbubble" size={21} color={color} />;
}

export function MeIcon({ color }: TabIconProps) {
  return <Ionicons name="person" size={21} color={color} />;
}
