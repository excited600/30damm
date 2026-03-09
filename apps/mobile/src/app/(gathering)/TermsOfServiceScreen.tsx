import { View, StyleSheet, Pressable, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { WebView } from "react-native-webview";
import Ionicons from "@expo/vector-icons/Ionicons";
import { colors } from "@/shared/constants/colors";

const TERMS_OF_SERVICE_HTML = `<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>이용약관 - 서티댐(Thirty Damm)</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: -apple-system, 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; font-size: 15px; line-height: 1.8; color: #eee; background: ${colors.background}; }
  .wrap { max-width: 800px; margin: 0 auto; padding: 20px 16px 60px; }
  h1 { font-size: 24px; font-weight: 700; margin-bottom: 8px; color: #fff; }
  .subtitle { color: #999; font-size: 14px; margin-bottom: 40px; border-bottom: 1px solid #3A3A3A; padding-bottom: 20px; }
  h2 { font-size: 16px; font-weight: 700; margin: 36px 0 12px; background: #2C2C2C; padding: 10px 14px; border-left: 4px solid #F5C842; color: #fff; }
  p { margin-bottom: 10px; }
  ul, ol { padding-left: 20px; margin-bottom: 10px; }
  li { margin-bottom: 6px; }
  .highlight { background: #2a2518; border: 1px solid #5a4a20; padding: 14px 16px; border-radius: 6px; margin: 12px 0; font-size: 14px; }
  .change-note { background: #1a2030; border: 1px solid #2a4060; padding: 14px 16px; border-radius: 6px; margin: 12px 0; font-size: 14px; }
  .footer { margin-top: 60px; padding-top: 20px; border-top: 1px solid #3A3A3A; font-size: 13px; color: #777; }
</style>
</head>
<body>
<div class="wrap">
  <h1>이용약관</h1>
  <p class="subtitle">서티댐(Thirty Damm) · 시행일: 2026년 3월 8일</p>

  <h2>제1조 (목적)</h2>
  <p>이 약관은 전계순(서비스명: 서티댐, 이하 "운영자")이 제공하는 모바일 애플리케이션 서티댐(Thirty Damm)(이하 "서비스")의 이용과 관련하여 운영자와 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.</p>

  <h2>제2조 (정의)</h2>
  <p>이 약관에서 사용하는 용어의 정의는 다음과 같습니다.</p>
  <ol>
    <li>"서비스"란 운영자가 제공하는 30대를 위한 모임 애플리케이션 서티댐 및 이와 관련된 제반 서비스를 말합니다.</li>
    <li>"이용자"란 이 약관에 따라 운영자와 이용계약을 체결하고 서비스를 이용하는 자를 말합니다.</li>
    <li>"회원"이란 서비스에 회원가입을 완료한 이용자로서 계속적으로 서비스를 이용할 수 있는 자를 말합니다.</li>
    <li>"계정"이란 회원의 식별 및 서비스 이용을 위하여 회원이 설정하고 운영자가 부여하는 이메일 주소 등 고유한 정보를 말합니다.</li>
    <li>"콘텐츠"란 서비스 내에서 회원이 게시하거나 공유하는 글, 사진, 모임 정보 등 일체의 정보를 말합니다.</li>
  </ol>

  <h2>제3조 (약관의 게시와 개정)</h2>
  <ol>
    <li>운영자는 이 약관의 내용을 이용자가 쉽게 알 수 있도록 서비스 초기 화면 또는 앱 내 설정 화면에 게시합니다.</li>
    <li>운영자는 「약관의 규제에 관한 법률」, 「정보통신망 이용촉진 및 정보보호 등에 관한 법률」 등 관련 법령을 위배하지 않는 범위에서 이 약관을 개정할 수 있습니다.</li>
    <li>운영자가 약관을 개정할 경우에는 적용일자 및 개정 사유를 명시하여 현행 약관과 함께 서비스 공지사항에 그 적용일자 7일 이전부터 공지합니다. 다만, 이용자에게 불리한 약관 개정의 경우에는 30일 이전부터 공지합니다.</li>
    <li>이용자가 개정 약관의 적용일자 이후에도 서비스를 계속 이용할 경우 개정 약관에 동의한 것으로 봅니다.</li>
  </ol>

  <h2>제4조 (이용계약의 체결)</h2>
  <ol>
    <li>이용계약은 이용자가 약관의 내용에 동의한 후 회원가입 신청을 하고, 운영자가 이를 승낙함으로써 체결됩니다.</li>
    <li>운영자는 다음 각 호의 경우 회원가입 신청을 승낙하지 않거나 사후에 이용계약을 해지할 수 있습니다.
      <ul>
        <li>실명이 아니거나 타인의 정보를 도용한 경우</li>
        <li>서비스 목적에 반하는 용도로 이용하려 하는 경우</li>
        <li>서비스의 성격상 운영자가 정한 가입 기준에 부합하지 않는 경우</li>
        <li>이전에 약관 위반으로 서비스 이용이 제한된 경우</li>
        <li>기타 관련 법령에 위반되거나 운영자가 정한 기준에 부합하지 않는 경우</li>
      </ul>
    </li>
  </ol>
  <div class="highlight">※ 서비스는 30대를 주요 대상으로 하며, 운영자는 서비스의 성격에 맞는 가입 기준을 별도로 공지할 수 있습니다.</div>

  <h2>제5조 (회원 정보의 관리)</h2>
  <ol>
    <li>회원은 자신의 계정 정보(이메일, 비밀번호 등)를 타인에게 제공하거나 공유해서는 안 됩니다.</li>
    <li>회원의 계정 관리 소홀로 인해 발생하는 손해에 대하여 운영자는 책임을 지지 않습니다.</li>
    <li>회원은 계정 정보가 도용되거나 제3자가 무단으로 사용하고 있음을 인지한 경우 즉시 운영자에게 통보하여야 합니다.</li>
    <li>회원은 자신의 정보가 변경된 경우 서비스 내 설정 화면을 통해 수정하여야 하며, 미수정으로 인한 불이익은 회원이 부담합니다.</li>
  </ol>

  <h2>제6조 (서비스의 제공 및 변경)</h2>
  <ol>
    <li>운영자는 다음과 같은 서비스를 제공합니다.
      <ul>
        <li>30대를 위한 모임 생성 및 참여 서비스</li>
        <li>회원 간 커뮤니티 및 소통 서비스</li>
        <li>기타 운영자가 추가 개발하거나 제휴를 통해 제공하는 서비스</li>
      </ul>
    </li>
    <li>운영자는 서비스 운영상 또는 기술상 필요한 경우 제공하는 서비스의 전부 또는 일부를 변경할 수 있으며, 이에 대해 관련 법령에 특별한 규정이 없는 한 별도의 보상을 하지 않습니다.</li>
    <li>운영자는 서비스 변경 시 변경 내용과 시행일을 사전에 공지하기 위해 노력합니다.</li>
  </ol>

  <h2>제7조 (서비스의 중단)</h2>
  <ol>
    <li>운영자는 다음 각 호의 경우 서비스의 전부 또는 일부를 일시적으로 중단할 수 있습니다.
      <ul>
        <li>서비스 설비의 유지·보수를 위한 정기 점검 시</li>
        <li>정전, 천재지변, 국가비상사태 등 불가항력적 상황 발생 시</li>
        <li>기타 운영상 필요한 경우</li>
      </ul>
    </li>
    <li>운영자는 서비스 중단 사실을 사전 또는 사후에 공지하기 위해 노력합니다. 단, 불가항력적 사유 또는 긴급한 경우에는 예외로 합니다.</li>
    <li>운영자는 서비스 중단으로 인해 이용자 또는 제3자가 입은 손해에 대하여 관련 법령에 특별한 규정이 없는 한 책임을 지지 않습니다.</li>
  </ol>

  <h2>제8조 (회원의 의무)</h2>
  <p>회원은 다음 각 호의 행위를 해서는 안 됩니다.</p>
  <ol>
    <li>타인의 정보 도용 및 허위 정보 등록</li>
    <li>운영자가 게시한 정보의 무단 변경</li>
    <li>운영자의 사전 동의 없이 서비스를 이용한 영리 목적의 활동</li>
    <li>운영자 및 제3자의 저작권 등 지식재산권 침해</li>
    <li>운영자 및 제3자의 명예를 손상시키거나 업무를 방해하는 행위</li>
    <li>음란 또는 폭력적인 콘텐츠를 게시하거나 전송하는 행위</li>
    <li>서비스를 이용하여 영리 목적의 상업적 광고, 스팸 메시지를 전송하는 행위</li>
    <li>기타 관련 법령 또는 이 약관에서 금지하는 행위</li>
  </ol>

  <h2>제9조 (콘텐츠에 대한 권리와 책임)</h2>
  <ol>
    <li>회원이 서비스 내에 게시한 콘텐츠에 대한 저작권은 해당 회원에게 있습니다.</li>
    <li>회원은 서비스 내에 콘텐츠를 게시함으로써 운영자에게 해당 콘텐츠를 <strong>서비스 운영 목적 범위 내에서</strong> 사용, 복사, 수정, 전시할 수 있는 비독점적·무상 라이선스를 부여합니다. 해당 라이선스는 회원 탈퇴 또는 콘텐츠 삭제 시 종료됩니다.</li>
    <li>회원이 게시한 콘텐츠로 인해 제3자와 분쟁이 발생한 경우 해당 회원이 책임을 부담하며, 운영자는 이에 대한 책임을 지지 않습니다.</li>
    <li>운영자는 회원이 게시한 콘텐츠가 관련 법령, 이 약관 또는 공서양속에 위반된다고 판단되는 경우 해당 콘텐츠를 사전 통보 없이 삭제하거나 노출을 제한할 수 있습니다.</li>
  </ol>

  <h2>제10조 (운영자의 의무)</h2>
  <ol>
    <li>운영자는 관련 법령과 이 약관을 준수하며 안정적인 서비스를 제공하기 위해 노력합니다.</li>
    <li>운영자는 이용자의 개인정보를 「개인정보처리방침」에 따라 안전하게 관리합니다.</li>
    <li>운영자는 이용자로부터 제기된 의견이나 불만이 정당하다고 인정할 경우 이를 처리하기 위해 노력하며, 처리 결과를 이용자에게 통보합니다.</li>
  </ol>

  <h2>제11조 (서비스 이용 제한)</h2>
  <ol>
    <li>운영자는 회원이 이 약관의 의무를 위반하거나 서비스의 정상적인 운영을 방해한 경우 경고, 일시 정지, 영구 이용 정지 등의 단계적 제재를 가할 수 있습니다.</li>
    <li>운영자는 이용 제한 사실을 해당 회원에게 통보하기 위해 노력하며, 회원은 이에 대해 이의신청을 할 수 있습니다.</li>
    <li>긴급하거나 불가피한 사유가 있는 경우 운영자는 사전 통보 없이 이용을 제한할 수 있습니다.</li>
  </ol>

  <h2>제12조 (책임의 제한)</h2>
  <ol>
    <li>운영자는 천재지변, 전쟁, 기간통신사업자의 서비스 중지, 해킹, DDoS 공격 등 불가항력적인 사유로 서비스를 제공하지 못한 경우 책임이 면제됩니다.</li>
    <li>운영자는 회원의 귀책사유로 인한 서비스 이용 장애에 대하여 책임을 지지 않습니다.</li>
    <li>운영자는 회원이 서비스를 이용하여 기대하는 수익을 얻지 못하거나 상실한 것에 대하여 책임을 지지 않습니다.</li>
    <li>운영자는 회원 간 또는 회원과 제3자 간에 발생한 분쟁에 대하여 개입할 의무가 없으며 이로 인한 손해를 배상할 책임도 없습니다.</li>
    <li>운영자가 제공하는 서비스를 통해 이루어지는 회원 간 모임, 만남 등의 결과에 대해 운영자는 책임을 지지 않습니다.</li>
    <li>운영자의 손해배상 책임은 관련 법령에서 달리 정하지 않는 한, 해당 이용자가 서비스 이용을 위해 운영자에게 지급한 금액을 한도로 합니다.</li>
  </ol>

  <h2>제13조 (이용계약의 해지)</h2>
  <ol>
    <li>회원은 언제든지 서비스 내 설정 화면 또는 고객센터를 통해 탈퇴(이용계약 해지)를 신청할 수 있으며, 운영자는 관련 법령에서 정한 바에 따라 이를 처리합니다.</li>
    <li>탈퇴 시 회원의 개인정보는 「개인정보처리방침」에 따라 처리됩니다.</li>
    <li>탈퇴 후 회원이 서비스에 게시한 콘텐츠는 삭제되지 않을 수 있으며, 삭제를 원하는 경우 탈퇴 전 직접 삭제하여야 합니다.</li>
  </ol>

  <h2>제14조 (준거법 및 관할법원)</h2>
  <ol>
    <li>이 약관의 해석 및 운영자와 이용자 간의 분쟁에 대해서는 대한민국 법령을 적용합니다.</li>
    <li>서비스 이용으로 발생한 분쟁에 대해 소송이 제기될 경우 민사소송법상의 관할 법원을 관할 법원으로 합니다.</li>
  </ol>

  <div class="highlight">
    본 약관은 2026년 3월 8일부터 시행됩니다. 이전 약관은 본 약관으로 대체됩니다.
  </div>

  <div class="footer">
    <p>공고일자: 2026년 3월 8일 &nbsp;|&nbsp; 시행일자: 2026년 3월 8일</p>
    <p>서티댐(Thirty Damm) · 운영자: 전계순</p>
  </div>
</div>
</body>
</html>`;

export default function TermsOfServiceScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();

  return (
    <View style={[styles.screen, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={8} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color={colors.text.primary} />
        </Pressable>
        <Text style={styles.headerTitle}>이용약관</Text>
        <View style={styles.headerRight} />
      </View>

      {/* Content */}
      <WebView
        source={{ html: TERMS_OF_SERVICE_HTML }}
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
