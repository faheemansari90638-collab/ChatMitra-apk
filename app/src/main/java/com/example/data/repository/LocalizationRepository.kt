package com.example.data.repository

import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * LocalizationRepository provides full multilingual support for ChatMitra:
 * English (en), Hindi (hi), Urdu (ur), and Arabic (ar).
 * Handles persistent language selection, RTL/LTR layout directions, and dynamic UI dictionary.
 */
object LocalizationRepository {

  private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

  val isRtl: Boolean
    get() = _currentLanguage.value.isRtl

  fun setLanguage(language: AppLanguage) {
    _currentLanguage.value = language
  }

  /**
   * Complete multilingual translation dictionary.
   */
  private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
    // Navigation
    "tab_chats" to mapOf(
      AppLanguage.ENGLISH to "Chats",
      AppLanguage.HINDI to "चैट्स",
      AppLanguage.URDU to "چیٹس",
      AppLanguage.ARABIC to "المحادثات"
    ),
    "tab_updates" to mapOf(
      AppLanguage.ENGLISH to "Updates",
      AppLanguage.HINDI to "अपडेट्स",
      AppLanguage.URDU to "اپڈیٹس",
      AppLanguage.ARABIC to "المستجدات"
    ),
    "tab_calls" to mapOf(
      AppLanguage.ENGLISH to "Calls",
      AppLanguage.HINDI to "कॉल्स",
      AppLanguage.URDU to "کالز",
      AppLanguage.ARABIC to "المكالمات"
    ),
    "tab_settings" to mapOf(
      AppLanguage.ENGLISH to "Settings",
      AppLanguage.HINDI to "सेटिंग्स",
      AppLanguage.URDU to "سیٹنگز",
      AppLanguage.ARABIC to "الإعدادات"
    ),
    "app_name" to mapOf(
      AppLanguage.ENGLISH to "ChatMitra",
      AppLanguage.HINDI to "चैटमित्र",
      AppLanguage.URDU to "چیٹ مترا",
      AppLanguage.ARABIC to "شات ميترا"
    ),

    // Chat List & Headers
    "search_chats" to mapOf(
      AppLanguage.ENGLISH to "Search chats or people...",
      AppLanguage.HINDI to "चैट्स या संपर्क खोजें...",
      AppLanguage.URDU to "چیٹس یا افراد تلاش کریں...",
      AppLanguage.ARABIC to "البحث في المحادثات أو الأشخاص..."
    ),
    "new_chat" to mapOf(
      AppLanguage.ENGLISH to "New Chat",
      AppLanguage.HINDI to "नई चैट",
      AppLanguage.URDU to "نئی چیٹ",
      AppLanguage.ARABIC to "محادثة جديدة"
    ),
    "new_group" to mapOf(
      AppLanguage.ENGLISH to "New Group",
      AppLanguage.HINDI to "नया ग्रुप",
      AppLanguage.URDU to "نیا گروپ",
      AppLanguage.ARABIC to "مجموعة جديدة"
    ),
    "group_call" to mapOf(
      AppLanguage.ENGLISH to "Group Call",
      AppLanguage.HINDI to "ग्रुप कॉल",
      AppLanguage.URDU to "گروپ کال",
      AppLanguage.ARABIC to "مكالمة جماعية"
    ),
    "contacts_friends" to mapOf(
      AppLanguage.ENGLISH to "Contacts & Friends",
      AppLanguage.HINDI to "संपर्क और मित्र",
      AppLanguage.URDU to "رابطے اور دوست",
      AppLanguage.ARABIC to "جهات الاتصال والأصدقاء"
    ),
    "admin_dashboard" to mapOf(
      AppLanguage.ENGLISH to "Admin Dashboard",
      AppLanguage.HINDI to "एडमिन डैशबोर्ड",
      AppLanguage.URDU to "ایڈمن ڈیش بورڈ",
      AppLanguage.ARABIC to "لوحة تحكم المشرف"
    ),

    // Chat Details & Composer
    "type_message" to mapOf(
      AppLanguage.ENGLISH to "Type a message...",
      AppLanguage.HINDI to "संदेश लिखें...",
      AppLanguage.URDU to "پیغام لکھیں...",
      AppLanguage.ARABIC to "اكتب رسالة..."
    ),
    "send" to mapOf(
      AppLanguage.ENGLISH to "Send",
      AppLanguage.HINDI to "भेजें",
      AppLanguage.URDU to "بھیجیں",
      AppLanguage.ARABIC to "إرسال"
    ),
    "online" to mapOf(
      AppLanguage.ENGLISH to "Online",
      AppLanguage.HINDI to "ऑनलाइन",
      AppLanguage.URDU to "آن لائن",
      AppLanguage.ARABIC to "متصل الآن"
    ),
    "typing" to mapOf(
      AppLanguage.ENGLISH to "typing...",
      AppLanguage.HINDI to "टाइप कर रहे हैं...",
      AppLanguage.URDU to "ٹائپ کر رہے ہیں...",
      AppLanguage.ARABIC to "يكتب الآن..."
    ),
    "reply" to mapOf(
      AppLanguage.ENGLISH to "Reply",
      AppLanguage.HINDI to "उत्तर दें",
      AppLanguage.URDU to "جواب دیں",
      AppLanguage.ARABIC to "رد"
    ),

    // Calls & Group Calls
    "incoming_call" to mapOf(
      AppLanguage.ENGLISH to "Incoming Call",
      AppLanguage.HINDI to "आने वाली कॉल",
      AppLanguage.URDU to "آمدہ کال",
      AppLanguage.ARABIC to "مكالمة واردة"
    ),
    "end_call" to mapOf(
      AppLanguage.ENGLISH to "End Call",
      AppLanguage.HINDI to "कॉल समाप्त करें",
      AppLanguage.URDU to "کال ختم کریں",
      AppLanguage.ARABIC to "إنهاء المكالمة"
    ),
    "mute_mic" to mapOf(
      AppLanguage.ENGLISH to "Mute",
      AppLanguage.HINDI to "म्यूट करें",
      AppLanguage.URDU to "میوٹ کریں",
      AppLanguage.ARABIC to "كتم الصوت"
    ),
    "unmute_mic" to mapOf(
      AppLanguage.ENGLISH to "Unmute",
      AppLanguage.HINDI to "अनम्यूट करें",
      AppLanguage.URDU to "ان میوٹ کریں",
      AppLanguage.ARABIC to "إلغاء كتم الصوت"
    ),
    "add_participant" to mapOf(
      AppLanguage.ENGLISH to "Add Person",
      AppLanguage.HINDI to "सदस्य जोड़ें",
      AppLanguage.URDU to "شخص شامل کریں",
      AppLanguage.ARABIC to "إضافة مشارك"
    ),
    "call_quality" to mapOf(
      AppLanguage.ENGLISH to "Call Quality",
      AppLanguage.HINDI to "कॉल गुणवत्ता",
      AppLanguage.URDU to "کال کوالٹی",
      AppLanguage.ARABIC to "جودة المكالمة"
    ),

    // Reliability & Offline
    "offline_banner" to mapOf(
      AppLanguage.ENGLISH to "Waiting for network... Messages will send automatically.",
      AppLanguage.HINDI to "नेटवर्क की प्रतीक्षा... संदेश स्वतः भेजे जाएंगे।",
      AppLanguage.URDU to "نیٹ ورک کا انتظار... پیغامات خود بخود چلے جائیں گے۔",
      AppLanguage.ARABIC to "في انتظار الشبكة... سيتم إرسال الرسائل تلقائياً."
    ),
    "queued_messages" to mapOf(
      AppLanguage.ENGLISH to "Pending Queue",
      AppLanguage.HINDI to "लंबित कतार",
      AppLanguage.URDU to "زیر التواء قطار",
      AppLanguage.ARABIC to "قائمة الانتظار"
    ),

    // Low Data Mode
    "low_data_mode" to mapOf(
      AppLanguage.ENGLISH to "Low Data Mode",
      AppLanguage.HINDI to "कम डेटा मोड",
      AppLanguage.URDU to "کم ڈیٹا موڈ",
      AppLanguage.ARABIC to "وضع توفير البيانات"
    ),
    "low_data_desc" to mapOf(
      AppLanguage.ENGLISH to "Reduces media quality and limits background data consumption",
      AppLanguage.HINDI to "मीडिया गुणवत्ता कम करता है और बैकग्राउंड डेटा बचाता है",
      AppLanguage.URDU to "میڈیا کوالٹی کم کرتا ہے اور پس منظر کا ڈیٹا بچاتا ہے",
      AppLanguage.ARABIC to "يقلل من جودة الوسائط ويحد من استهلاك البيانات في الخلفية"
    ),

    // Language Selector
    "select_language" to mapOf(
      AppLanguage.ENGLISH to "App Language",
      AppLanguage.HINDI to "ऐप की भाषा",
      AppLanguage.URDU to "ایپ کی زبان",
      AppLanguage.ARABIC to "لغة التطبيق"
    ),

    // Security & Block
    "blocked_users" to mapOf(
      AppLanguage.ENGLISH to "Blocked Contacts",
      AppLanguage.HINDI to "अवरुद्ध संपर्क",
      AppLanguage.URDU to "بلاک شدہ رابطے",
      AppLanguage.ARABIC to "جهات الاتصال المحظورة"
    ),
    "block_user" to mapOf(
      AppLanguage.ENGLISH to "Block User",
      AppLanguage.HINDI to "ब्लॉक करें",
      AppLanguage.URDU to "بلاک کریں",
      AppLanguage.ARABIC to "حظر المستخدم"
    ),
    "unblock_user" to mapOf(
      AppLanguage.ENGLISH to "Unblock",
      AppLanguage.HINDI to "अनब्लॉक करें",
      AppLanguage.URDU to "ان بلاک کریں",
      AppLanguage.ARABIC to "إلغاء الحظر"
    ),
    "report_user" to mapOf(
      AppLanguage.ENGLISH to "Report User",
      AppLanguage.HINDI to "रिपोर्ट करें",
      AppLanguage.URDU to "رپورٹ کریں",
      AppLanguage.ARABIC to "إبلاغ عن مستخدم"
    )
  )

  /**
   * Retrieves the translated string for the current language.
   */
  fun t(key: String, default: String = key): String {
    val lang = _currentLanguage.value
    return translations[key]?.get(lang) ?: default
  }
}
