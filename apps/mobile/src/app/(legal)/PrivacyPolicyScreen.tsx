import { View, StyleSheet, Pressable, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { WebView } from "react-native-webview";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";

const PRIVACY_POLICY_HTML = `<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>개인정보처리방침 - 서티댐(Thirty Damm)</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: -apple-system, 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; font-size: 15px; line-height: 1.8; color: #eee; background: ${colors.background}; }
  .wrap { max-width: 800px; margin: 0 auto; padding: 20px 16px 60px; }
  h1 { font-size: 24px; font-weight: 700; margin-bottom: 8px; color: #fff; }
  .subtitle { color: #999; font-size: 14px; margin-bottom: 40px; border-bottom: 1px solid #3A3A3A; padding-bottom: 20px; }
  h2 { font-size: 16px; font-weight: 700; margin: 36px 0 12px; background: #2C2C2C; padding: 10px 14px; border-left: 4px solid #F5C842; color: #fff; }
  p { margin-bottom: 10px; }
  table { width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 14px; }
  th { background: #2C2C2C; color: #F5C842; padding: 10px 12px; text-align: left; }
  td { padding: 10px 12px; border-bottom: 1px solid #3A3A3A; vertical-align: top; color: #ddd; }
  tr:nth-child(even) td { background: #1a1a1a; }
  ul { padding-left: 20px; margin-bottom: 10px; }
  li { margin-bottom: 6px; }
  .highlight { background: #2a2518; border: 1px solid #5a4a20; padding: 14px 16px; border-radius: 6px; margin: 12px 0; font-size: 14px; }
  .change-note { background: #1a2030; border: 1px solid #2a4060; padding: 14px 16px; border-radius: 6px; margin: 12px 0; font-size: 14px; }
  .footer { margin-top: 60px; padding-top: 20px; border-top: 1px solid #3A3A3A; font-size: 13px; color: #777; }
</style>
</head>
<body>
<div class="wrap">
  <h1>개인정보처리방침</h1>
  <p class="subtitle">서티댐(Thirty Damm) · 시행일: 2026년 3월 8일</p>

  <p>전계순(서비스명: 서티댐, 이하 "운영자")은 「개인정보 보호법」 제30조에 따라 이용자의 개인정보를 보호하고 이와 관련한 고충을 신속하고 원활하게 처리할 수 있도록 다음과 같이 개인정보처리방침을 수립·공개합니다.</p>

  <h2>제1조 개인정보의 처리 목적</h2>
  <p>운영자는 다음의 목적을 위하여 개인정보를 처리합니다. 처리한 개인정보는 다음의 목적 이외의 용도로는 사용되지 않으며, 이용 목적이 변경되는 경우에는 「개인정보 보호법」 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행합니다.</p>
  <ul>
    <li>회원 가입 및 관리: 회원 가입 의사 확인, 회원제 서비스 제공에 따른 본인 식별·인증, 회원자격 유지·관리, 서비스 부정이용 방지, 각종 고지·통지</li>
    <li>서비스 제공: 30대 모임 매칭 및 커뮤니티 서비스 제공, 맞춤형 콘텐츠 제공, 서비스 개선 및 신규 서비스 개발</li>
    <li>고충 처리: 민원인의 신원 확인, 민원사항 확인, 사실조사를 위한 연락·통지, 처리결과 통보</li>
  </ul>

  <h2>제2조 개인정보의 처리 및 보유 기간</h2>
  <p>운영자는 법령에 따른 개인정보 보유·이용기간 또는 이용자로부터 개인정보 수집 시에 동의 받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
  <table>
    <tr><th>처리 목적</th><th>보유 항목</th><th>보유 기간</th></tr>
    <tr><td>회원 가입 및 관리</td><td>이메일, 이름, 나이, 성별</td><td>회원 탈퇴 시까지</td></tr>
    <tr><td>서비스 이용 기록</td><td>접속 로그, 서비스 이용 기록</td><td>3년 (통신비밀보호법)</td></tr>
    <tr><td>소비자 불만·분쟁 처리</td><td>이메일</td><td>3년 (전자상거래법)</td></tr>
  </table>
  <div class="highlight">※ 단, 관계 법령에 의한 정보 보유 사유가 있는 경우 해당 기간 동안 보관합니다.</div>

  <h2>제3조 처리하는 개인정보 항목</h2>
  <p>운영자는 다음의 개인정보 항목을 처리하고 있습니다.</p>
  <table>
    <tr><th>구분</th><th>수집 항목</th><th>수집 방법</th></tr>
    <tr><td>현재 필수 항목</td><td>이메일 주소</td><td>앱 내 회원가입 화면</td></tr>
    <tr><td>추후 수집 예정 (선택)</td><td>이름, 나이, 성별</td><td>앱 내 프로필 설정 화면</td></tr>
    <tr><td>자동 수집</td><td>서비스 이용 기록, 접속 로그, 기기 정보(OS, 앱 버전)</td><td>서비스 이용 과정에서 자동 생성</td></tr>
  </table>
  <div class="highlight">※ 추후 수집 예정 항목(이름, 나이, 성별)은 수집 전 별도 동의를 받은 후 처리합니다.</div>

  <h2>제4조 개인정보의 제3자 제공</h2>
  <p>운영자는 이용자의 개인정보를 제1조에서 명시한 목적 범위 내에서만 처리하며, 이용자의 동의, 법률의 특별한 규정 등 「개인정보 보호법」 제17조에 해당하는 경우에만 개인정보를 제3자에게 제공합니다.</p>
  <p>현재 운영자는 이용자의 개인정보를 제3자에게 제공하지 않습니다.</p>

  <h2>제5조 개인정보처리의 위탁</h2>
  <p>운영자는 현재 개인정보 처리 업무를 외부에 위탁하지 않습니다. 향후 위탁 계약 체결 시 「개인정보 보호법」 제26조에 따라 위탁업무 내용과 수탁자를 공개하겠습니다.</p>

  <h2>제6조 이용자의 권리·의무 및 행사 방법</h2>
  <p>이용자는 운영자에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.</p>
  <ul>
    <li>개인정보 열람 요구</li>
    <li>오류 등이 있을 경우 정정 요구</li>
    <li>삭제 요구</li>
    <li>처리 정지 요구</li>
  </ul>
  <p>위 권리 행사는 운영자에 대해 이메일(아래 개인정보보호 책임자 연락처)을 통해 할 수 있으며, 운영자는 이에 대해 관련 법령에 따라 <strong>10일 이내</strong>에 조치하겠습니다.</p>
  <p>이용자가 개인정보의 오류 등에 대한 정정 또는 삭제를 요구한 경우에는 운영자는 정정 또는 삭제를 완료할 때까지 당해 개인정보를 이용하거나 제공하지 않습니다.</p>

  <h2>제7조 개인정보의 파기</h2>
  <p>운영자는 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다.</p>
  <ul>
    <li><strong>전자적 파일:</strong> 복원이 불가능한 방법으로 영구 삭제</li>
    <li><strong>종이 문서:</strong> 분쇄기로 분쇄하거나 소각</li>
  </ul>

  <h2>제8조 개인정보의 안전성 확보 조치</h2>
  <p>운영자는 「개인정보 보호법」 제29조에 따라 다음과 같이 안전성 확보에 필요한 기술적·관리적 조치를 하고 있습니다.</p>
  <ul>
    <li>개인정보의 암호화: 이용자의 이메일 등 개인정보는 암호화되어 저장·관리되며, 전송 시 SSL 등 암호화 통신을 사용합니다.</li>
    <li>보안 취약점 점검 및 접근 통제: 개인정보 처리 시스템에 대한 보안 취약점을 주기적으로 점검하고 접근권한을 최소화합니다.</li>
    <li>개인정보 접근 제한: 개인정보를 처리하는 데이터베이스 시스템에 대한 접근권한을 최소한의 인원으로 제한합니다.</li>
  </ul>

  <h2>제9조 개인정보 자동 수집 장치의 설치·운영 및 거부에 관한 사항</h2>
  <p>운영자는 이용자에게 개별적인 맞춤서비스를 제공하기 위해 이용정보를 저장하고 수시로 불러오는 기술(쿠키 및 이에 준하는 기술)을 사용합니다. 이용자는 앱 설정을 통해 자동 수집을 거부할 수 있으나, 이 경우 일부 서비스 이용이 제한될 수 있습니다.</p>

  <h2>제10조 개인정보 보호책임자</h2>
  <p>운영자는 개인정보 처리에 관한 업무를 총괄해서 책임지고, 이용자의 개인정보 관련 불만 처리 및 피해구제 등을 위해 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.</p>
  <table>
    <tr><th>항목</th><th>내용</th></tr>
    <tr><td>성명</td><td>전계순</td></tr>
    <tr><td>직위</td><td>개인정보보호 책임자</td></tr>
    <tr><td>이메일</td><td>excited600@gmail.com</td></tr>
  </table>
  <p>이용자는 서티댐 서비스를 이용하시면서 발생한 모든 개인정보 보호 관련 문의, 불만, 피해구제 등에 관한 사항을 개인정보 보호책임자에게 문의하실 수 있습니다.</p>

  <h2>제11조 권익침해 구제 방법</h2>
  <p>이용자는 개인정보침해로 인한 구제를 받기 위하여 개인정보분쟁조정위원회, 한국인터넷진흥원 개인정보침해신고센터 등에 분쟁해결이나 상담 등을 신청할 수 있습니다.</p>
  <ul>
    <li>개인정보분쟁조정위원회: 1833-6972 / www.kopico.go.kr</li>
    <li>개인정보침해신고센터: 118 / privacy.kisa.or.kr</li>
    <li>대검찰청: 1301 / www.spo.go.kr</li>
    <li>경찰청: 182 / ecrm.cyber.go.kr</li>
  </ul>

  <h2>제12조 개인정보처리방침의 변경</h2>
  <p>이 개인정보처리방침은 2026년 3월 8일부터 적용됩니다. 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 앱 공지사항을 통하여 고지합니다.</p>

  <div class="footer">
    <p>공고일자: 2026년 3월 8일 &nbsp;|&nbsp; 시행일자: 2026년 3월 8일</p>
    <p>서티댐(Thirty Damm) · 개인정보보호책임자: 전계순</p>
  </div>
</div>
</body>
</html>`;

export default function PrivacyPolicyScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  return (
    <View style={[styles.screen, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={8} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <Text style={styles.headerTitle}>개인정보 처리방침</Text>
        <View style={styles.headerRight} />
      </View>

      {/* Content */}
      <WebView
        source={{ html: PRIVACY_POLICY_HTML }}
        style={styles.webview}
        scrollEnabled
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: colors.surface,
  },
  backButton: {
    width: 24,
    height: 24,
    alignItems: "center",
    justifyContent: "center",
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: colors.text.primary,
  },
  headerRight: {
    width: 24,
    height: 24,
  },
  webview: {
    flex: 1,
    backgroundColor: colors.background,
  },
});
