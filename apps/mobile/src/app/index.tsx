import { Redirect } from "expo-router";
// import { useAuthStore } from "@/store/useAuthStore";

export default function Index() {
  // TODO: 개발 완료 후 토큰 인증 복원
  // const token = useAuthStore((s) => s.token);
  // if (!token) {
  //   return <Redirect href="/welcome" />;
  // }
  // return <Redirect href="/(gathering)/GatheringCardListScreen" />;

  return <Redirect href="/welcome" />;
}
