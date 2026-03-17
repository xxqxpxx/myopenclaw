package com.myopenclaw.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.myopenclaw.ui.viewmodel.profile.ProfileViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Localization Manager that provides translated strings based on user's language preference
 */
object LocalizationManager {
    private var currentLanguage: String = "English"
    private val languageChangeListeners = mutableListOf<() -> Unit>()

    /**
     * Update the current language and notify listeners
     */
    fun setLanguage(language: String) {
        if (currentLanguage != language) {
            currentLanguage = language
            languageChangeListeners.forEach { it.invoke() }
        }
    }

    /**
     * Get the current language
     */
    fun getCurrentLanguage(): String = currentLanguage

    /**
     * Register a listener for language changes
     */
    fun addLanguageChangeListener(listener: () -> Unit) {
        languageChangeListeners.add(listener)
    }

    /**
     * Remove a language change listener
     */
    fun removeLanguageChangeListener(listener: () -> Unit) {
        languageChangeListeners.remove(listener)
    }

    /**
     * Get translated string for a key
     */
    fun getString(key: String): String {
        val translations = getTranslationsForLanguage(currentLanguage)
        return translations[key] ?: translations[key] ?: key
    }

    /**
     * Get translations map for a specific language
     */
    private fun getTranslationsForLanguage(language: String): Map<String, String> {
        return when (language) {
            "Spanish" -> SpanishTranslations.strings
            "French" -> FrenchTranslations.strings
            "German" -> GermanTranslations.strings
            "Portuguese" -> PortugueseTranslations.strings
            "Italian" -> ItalianTranslations.strings
            "Japanese" -> JapaneseTranslations.strings
            "Chinese (Simplified)" -> ChineseTranslations.strings
            "Korean" -> KoreanTranslations.strings
            "Arabic" -> ArabicTranslations.strings
            else -> EnglishTranslations.strings
        }
    }
}

/**
 * Composable function to get localized string
 */
@Composable
fun localizedString(key: String, language: String = LocalizationManager.getCurrentLanguage()): String {
    val translations = remember(language) {
        when (language) {
            "Spanish" -> SpanishTranslations.strings
            "French" -> FrenchTranslations.strings
            "German" -> GermanTranslations.strings
            "Portuguese" -> PortugueseTranslations.strings
            "Italian" -> ItalianTranslations.strings
            "Japanese" -> JapaneseTranslations.strings
            "Chinese (Simplified)" -> ChineseTranslations.strings
            "Korean" -> KoreanTranslations.strings
            "Arabic" -> ArabicTranslations.strings
            else -> EnglishTranslations.strings
        }
    }
    return translations[key] ?: EnglishTranslations.strings[key] ?: key
}

/**
 * Translation keys
 */
object StringKeys {
    // Profile Screen
    const val PROFILE_CHANGE_LANGUAGE = "change_language"
    const val PROFILE_RATE_APP = "rate_myopenclaw_5_stars"
    const val PROFILE_SHARE_APP = "share_myopenclaw_app"
    const val PROFILE_CHAT_WITH_US = "chat_with_us"
    const val PROFILE_TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PROFILE_PRIVACY_POLICY = "privacy_policy"
    const val PROFILE_ACCOUNT_STATUS = "account_status"
    const val PROFILE_VERSION = "version"
    const val PROFILE_PAID_SUBSCRIBER = "paid_subscriber"
    const val PROFILE_FREE_SUBSCRIBER = "free_subscriber"
    const val PROFILE_ACTIVE_SUBSCRIPTION = "active_subscription"
    const val PROFILE_SIGNOUT = "signout"
    const val PROFILE_DELETE_ACCOUNT = "delete_account"
    const val PROFILE_DEVELOPER_TOOLS = "developer_tools"
    const val PROFILE_NETWORK_INSPECTOR = "network_inspector"
    const val PROFILE_DEBUG = "debug"
    const val PROFILE_PREMIUM = "premium"
    const val PROFILE_FREE = "free"
    const val PROFILE_BILLING_STARTS_IN = "billing_starts_in"
    const val PROFILE_FREE_ACCOUNT = "free_account"
    const val PROFILE_TRIAL_ENDS_IN = "trial_ends_in"
    const val PROFILE_TRIAL_ACCOUNT = "trial_account"
    const val PROFILE_NEXT_BILLING = "next_billing"
    
    // Language Selection Dialog
    const val SELECT_LANGUAGE = "select_language"
    const val CANCEL = "cancel"
}

/**
 * English translations (default)
 */
object EnglishTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Change language",
        StringKeys.PROFILE_RATE_APP to "Rate my openClaw 5 stars",
        StringKeys.PROFILE_SHARE_APP to "Share my openClaw app",
        StringKeys.PROFILE_CHAT_WITH_US to "Chat with us",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Terms and conditions",
        StringKeys.PROFILE_PRIVACY_POLICY to "Privacy policy",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Account status",
        StringKeys.PROFILE_VERSION to "Version",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Paid subscriber",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Free subscriber",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Active subscription",
        StringKeys.PROFILE_SIGNOUT to "Signout",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Delete account",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Developer Tools",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Network Inspector",
        StringKeys.PROFILE_DEBUG to "Debug",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Free",
        StringKeys.PROFILE_BILLING_STARTS_IN to "Billing starts in %d days",
        StringKeys.PROFILE_FREE_ACCOUNT to "Free account",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "Trial ends in %d days",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Trial account",
        StringKeys.PROFILE_NEXT_BILLING to "Next billing %s",
        StringKeys.SELECT_LANGUAGE to "Select Language",
        StringKeys.CANCEL to "Cancel"
    )
}

/**
 * Spanish translations
 */
object SpanishTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Cambiar idioma",
        StringKeys.PROFILE_RATE_APP to "Calificar my openClaw 5 estrellas",
        StringKeys.PROFILE_SHARE_APP to "Compartir aplicación my openClaw",
        StringKeys.PROFILE_CHAT_WITH_US to "Chatea con nosotros",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Términos y condiciones",
        StringKeys.PROFILE_PRIVACY_POLICY to "Política de privacidad",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Estado de la cuenta",
        StringKeys.PROFILE_VERSION to "Versión",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Suscriptor de pago",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Suscriptor gratuito",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Suscripción activa",
        StringKeys.PROFILE_SIGNOUT to "Cerrar sesión",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Eliminar cuenta",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Herramientas de desarrollador",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Inspector de red",
        StringKeys.PROFILE_DEBUG to "Depurar",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Gratis",
        StringKeys.PROFILE_BILLING_STARTS_IN to "La facturación comienza en %d días",
        StringKeys.PROFILE_FREE_ACCOUNT to "Cuenta gratuita",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "El período de prueba termina en %d días",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Cuenta de prueba",
        StringKeys.PROFILE_NEXT_BILLING to "Próxima facturación %s",
        StringKeys.SELECT_LANGUAGE to "Seleccionar idioma",
        StringKeys.CANCEL to "Cancelar"
    )
}

/**
 * French translations
 */
object FrenchTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Changer la langue",
        StringKeys.PROFILE_RATE_APP to "Noter my openClaw 5 étoiles",
        StringKeys.PROFILE_SHARE_APP to "Partager l'application my openClaw",
        StringKeys.PROFILE_CHAT_WITH_US to "Discutez avec nous",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Termes et conditions",
        StringKeys.PROFILE_PRIVACY_POLICY to "Politique de confidentialité",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Statut du compte",
        StringKeys.PROFILE_VERSION to "Version",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Abonné payant",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Abonné gratuit",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Abonnement actif",
        StringKeys.PROFILE_SIGNOUT to "Déconnexion",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Supprimer le compte",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Outils de développement",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Inspecteur réseau",
        StringKeys.PROFILE_DEBUG to "Déboguer",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Gratuit",
        StringKeys.PROFILE_BILLING_STARTS_IN to "La facturation commence dans %d jours",
        StringKeys.PROFILE_FREE_ACCOUNT to "Compte gratuit",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "L'essai se termine dans %d jours",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Compte d'essai",
        StringKeys.PROFILE_NEXT_BILLING to "Prochaine facturation %s",
        StringKeys.SELECT_LANGUAGE to "Sélectionner la langue",
        StringKeys.CANCEL to "Annuler"
    )
}

/**
 * German translations
 */
object GermanTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Sprache ändern",
        StringKeys.PROFILE_RATE_APP to "my openClaw 5 Sterne bewerten",
        StringKeys.PROFILE_SHARE_APP to "my openClaw App teilen",
        StringKeys.PROFILE_CHAT_WITH_US to "Chatten Sie mit uns",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Allgemeine Geschäftsbedingungen",
        StringKeys.PROFILE_PRIVACY_POLICY to "Datenschutzrichtlinie",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Kontostatus",
        StringKeys.PROFILE_VERSION to "Version",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Bezahlter Abonnent",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Kostenloser Abonnent",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Aktives Abonnement",
        StringKeys.PROFILE_SIGNOUT to "Abmelden",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Konto löschen",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Entwicklertools",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Netzwerkinspector",
        StringKeys.PROFILE_DEBUG to "Debuggen",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Kostenlos",
        StringKeys.PROFILE_BILLING_STARTS_IN to "Abrechnung beginnt in %d Tagen",
        StringKeys.PROFILE_FREE_ACCOUNT to "Kostenloses Konto",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "Testversion endet in %d Tagen",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Testkonto",
        StringKeys.PROFILE_NEXT_BILLING to "Nächste Abrechnung %s",
        StringKeys.SELECT_LANGUAGE to "Sprache auswählen",
        StringKeys.CANCEL to "Abbrechen"
    )
}

/**
 * Portuguese translations
 */
object PortugueseTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Alterar idioma",
        StringKeys.PROFILE_RATE_APP to "Avaliar my openClaw 5 estrelas",
        StringKeys.PROFILE_SHARE_APP to "Compartilhar aplicativo my openClaw",
        StringKeys.PROFILE_CHAT_WITH_US to "Converse conosco",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Termos e condições",
        StringKeys.PROFILE_PRIVACY_POLICY to "Política de privacidade",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Status da conta",
        StringKeys.PROFILE_VERSION to "Versão",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Assinante pago",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Assinante gratuito",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Assinatura ativa",
        StringKeys.PROFILE_SIGNOUT to "Sair",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Excluir conta",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Ferramentas de desenvolvedor",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Inspetor de rede",
        StringKeys.PROFILE_DEBUG to "Depurar",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Grátis",
        StringKeys.PROFILE_BILLING_STARTS_IN to "Cobrança começa em %d dias",
        StringKeys.PROFILE_FREE_ACCOUNT to "Conta gratuita",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "Período de teste termina em %d dias",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Conta de teste",
        StringKeys.PROFILE_NEXT_BILLING to "Próxima cobrança %s",
        StringKeys.SELECT_LANGUAGE to "Selecionar idioma",
        StringKeys.CANCEL to "Cancelar"
    )
}

/**
 * Italian translations
 */
object ItalianTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "Cambia lingua",
        StringKeys.PROFILE_RATE_APP to "Valuta my openClaw 5 stelle",
        StringKeys.PROFILE_SHARE_APP to "Condividi app my openClaw",
        StringKeys.PROFILE_CHAT_WITH_US to "Chatta con noi",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "Termini e condizioni",
        StringKeys.PROFILE_PRIVACY_POLICY to "Informativa sulla privacy",
        StringKeys.PROFILE_ACCOUNT_STATUS to "Stato dell'account",
        StringKeys.PROFILE_VERSION to "Versione",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "Abbonato a pagamento",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "Abbonato gratuito",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "Abbonamento attivo",
        StringKeys.PROFILE_SIGNOUT to "Esci",
        StringKeys.PROFILE_DELETE_ACCOUNT to "Elimina account",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "Strumenti per sviluppatori",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "Ispettore di rete",
        StringKeys.PROFILE_DEBUG to "Debug",
        StringKeys.PROFILE_PREMIUM to "Premium",
        StringKeys.PROFILE_FREE to "Gratuito",
        StringKeys.PROFILE_BILLING_STARTS_IN to "La fatturazione inizia tra %d giorni",
        StringKeys.PROFILE_FREE_ACCOUNT to "Account gratuito",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "Il periodo di prova termina tra %d giorni",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "Account di prova",
        StringKeys.PROFILE_NEXT_BILLING to "Prossima fatturazione %s",
        StringKeys.SELECT_LANGUAGE to "Seleziona lingua",
        StringKeys.CANCEL to "Annulla"
    )
}

/**
 * Japanese translations
 */
object JapaneseTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "言語を変更",
        StringKeys.PROFILE_RATE_APP to "my openClawを5つ星で評価",
        StringKeys.PROFILE_SHARE_APP to "my openClawアプリを共有",
        StringKeys.PROFILE_CHAT_WITH_US to "お問い合わせ",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "利用規約",
        StringKeys.PROFILE_PRIVACY_POLICY to "プライバシーポリシー",
        StringKeys.PROFILE_ACCOUNT_STATUS to "アカウントステータス",
        StringKeys.PROFILE_VERSION to "バージョン",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "有料購読者",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "無料購読者",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "アクティブな購読",
        StringKeys.PROFILE_SIGNOUT to "サインアウト",
        StringKeys.PROFILE_DELETE_ACCOUNT to "アカウントを削除",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "開発者ツール",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "ネットワークインスペクター",
        StringKeys.PROFILE_DEBUG to "デバッグ",
        StringKeys.PROFILE_PREMIUM to "プレミアム",
        StringKeys.PROFILE_FREE to "無料",
        StringKeys.PROFILE_BILLING_STARTS_IN to "%d日後に請求開始",
        StringKeys.PROFILE_FREE_ACCOUNT to "無料アカウント",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "%d日後にトライアル終了",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "トライアルアカウント",
        StringKeys.PROFILE_NEXT_BILLING to "次回請求日 %s",
        StringKeys.SELECT_LANGUAGE to "言語を選択",
        StringKeys.CANCEL to "キャンセル"
    )
}

/**
 * Chinese (Simplified) translations
 */
object ChineseTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "更改语言",
        StringKeys.PROFILE_RATE_APP to "为my openClaw评分5星",
        StringKeys.PROFILE_SHARE_APP to "分享my openClaw应用",
        StringKeys.PROFILE_CHAT_WITH_US to "与我们聊天",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "条款和条件",
        StringKeys.PROFILE_PRIVACY_POLICY to "隐私政策",
        StringKeys.PROFILE_ACCOUNT_STATUS to "账户状态",
        StringKeys.PROFILE_VERSION to "版本",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "付费订阅者",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "免费订阅者",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "活跃订阅",
        StringKeys.PROFILE_SIGNOUT to "退出登录",
        StringKeys.PROFILE_DELETE_ACCOUNT to "删除账户",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "开发者工具",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "网络检查器",
        StringKeys.PROFILE_DEBUG to "调试",
        StringKeys.PROFILE_PREMIUM to "高级版",
        StringKeys.PROFILE_FREE to "免费",
        StringKeys.PROFILE_BILLING_STARTS_IN to "%d天后开始计费",
        StringKeys.PROFILE_FREE_ACCOUNT to "免费账户",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "%d天后试用结束",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "试用账户",
        StringKeys.PROFILE_NEXT_BILLING to "下次计费 %s",
        StringKeys.SELECT_LANGUAGE to "选择语言",
        StringKeys.CANCEL to "取消"
    )
}

/**
 * Korean translations
 */
object KoreanTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "언어 변경",
        StringKeys.PROFILE_RATE_APP to "my openClaw 5점 평가",
        StringKeys.PROFILE_SHARE_APP to "my openClaw 앱 공유",
        StringKeys.PROFILE_CHAT_WITH_US to "문의하기",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "이용 약관",
        StringKeys.PROFILE_PRIVACY_POLICY to "개인정보 보호정책",
        StringKeys.PROFILE_ACCOUNT_STATUS to "계정 상태",
        StringKeys.PROFILE_VERSION to "버전",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "유료 구독자",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "무료 구독자",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "활성 구독",
        StringKeys.PROFILE_SIGNOUT to "로그아웃",
        StringKeys.PROFILE_DELETE_ACCOUNT to "계정 삭제",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "개발자 도구",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "네트워크 검사기",
        StringKeys.PROFILE_DEBUG to "디버그",
        StringKeys.PROFILE_PREMIUM to "프리미엄",
        StringKeys.PROFILE_FREE to "무료",
        StringKeys.PROFILE_BILLING_STARTS_IN to "%d일 후 청구 시작",
        StringKeys.PROFILE_FREE_ACCOUNT to "무료 계정",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "%d일 후 평가판 종료",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "평가판 계정",
        StringKeys.PROFILE_NEXT_BILLING to "다음 청구일 %s",
        StringKeys.SELECT_LANGUAGE to "언어 선택",
        StringKeys.CANCEL to "취소"
    )
}

/**
 * Arabic translations
 */
object ArabicTranslations {
    val strings = mapOf(
        StringKeys.PROFILE_CHANGE_LANGUAGE to "تغيير اللغة",
        StringKeys.PROFILE_RATE_APP to "قيم my openClaw 5 نجوم",
        StringKeys.PROFILE_SHARE_APP to "شارك تطبيق my openClaw",
        StringKeys.PROFILE_CHAT_WITH_US to "تحدث معنا",
        StringKeys.PROFILE_TERMS_AND_CONDITIONS to "الشروط والأحكام",
        StringKeys.PROFILE_PRIVACY_POLICY to "سياسة الخصوصية",
        StringKeys.PROFILE_ACCOUNT_STATUS to "حالة الحساب",
        StringKeys.PROFILE_VERSION to "الإصدار",
        StringKeys.PROFILE_PAID_SUBSCRIBER to "مشترك مدفوع",
        StringKeys.PROFILE_FREE_SUBSCRIBER to "مشترك مجاني",
        StringKeys.PROFILE_ACTIVE_SUBSCRIPTION to "اشتراك نشط",
        StringKeys.PROFILE_SIGNOUT to "تسجيل الخروج",
        StringKeys.PROFILE_DELETE_ACCOUNT to "حذف الحساب",
        StringKeys.PROFILE_DEVELOPER_TOOLS to "أدوات المطور",
        StringKeys.PROFILE_NETWORK_INSPECTOR to "مفتش الشبكة",
        StringKeys.PROFILE_DEBUG to "تصحيح",
        StringKeys.PROFILE_PREMIUM to "بريميوم",
        StringKeys.PROFILE_FREE to "مجاني",
        StringKeys.PROFILE_BILLING_STARTS_IN to "يبدأ الفوترة خلال %d أيام",
        StringKeys.PROFILE_FREE_ACCOUNT to "حساب مجاني",
        StringKeys.PROFILE_TRIAL_ENDS_IN to "تنتهي الفترة التجريبية خلال %d أيام",
        StringKeys.PROFILE_TRIAL_ACCOUNT to "حساب تجريبي",
        StringKeys.PROFILE_NEXT_BILLING to "الفوترة القادمة %s",
        StringKeys.SELECT_LANGUAGE to "اختر اللغة",
        StringKeys.CANCEL to "إلغاء"
    )
}
