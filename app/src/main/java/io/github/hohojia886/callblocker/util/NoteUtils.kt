/**
 * Utility for formatting and resolving system default note tags and legacy localized strings.
 */
package io.github.hohojia886.callblocker.util

import android.content.Context
import io.github.hohojia886.callblocker.R

object NoteUtils {

    const val TAG_AUTO_BLOCKED = "@string/note_auto_blocked"
    const val TAG_SYSTEM_BLOCK_LIST = "@string/import_source_system_block_list"
    const val TAG_BLOCKED_CONTACT = "@string/import_source_blocked_contact"
    const val TAG_BLOCKED_CONTACT_WITH_NAME_PREFIX = "@string/import_source_blocked_contact_with_name:"

    private val legacyAutoBlockedStrings = setOf(
        "Auto-added rejected call",
        "自動封鎖拒接電話",
        "自动拦截拒接电话",
        "自動拒否された電話",
        "자동 차단된 거절 전화",
        "Automatisch hinzugefügter abgewiesener Anruf",
        "Llamada rechazada añadida automáticamente",
        "Appel rejeté ajouté automatiquement",
        "Panggilan ditolak ditambahkan secara otomatis",
        "Chiamata rifiutata aggiunta automaticamente",
        "Chamada rejeitada adicionada automaticamente",
        "Автоматически добавленный отклоненный звонок",
        "เพิ่มเบอร์ที่ปฏิเสธเข้าบล็อกอัตโนมัติ",
        "Tự động thêm cuộc gọi bị từ chối"
    )

    private val legacySystemBlockListStrings = setOf(
        "System Block List",
        "系統封鎖名單",
        "系统拦截名单",
        "システム拒否リスト",
        "시스템 차단 목록",
        "System-Sperrliste",
        "Lista del Sistema",
        "Liste de blocage système",
        "Daftar Blokir Sistem",
        "Lista di Sistema",
        "Lista do Sistema",
        "Системный черный список",
        "รายการบล็อกระบบ",
        "Danh sách chặn hệ thống"
    )

    private val legacyBlockedContactStrings = setOf(
        "Blocked Contact",
        "封鎖聯絡人",
        "拦截联系人",
        "ブロックされた連絡先",
        "차단된 연락처",
        "Blockierter Kontakt",
        "Contacto Bloqueado",
        "Contact bloqué",
        "Kontak Diblokir",
        "Contatto Bloccato",
        "Contato Bloqueado",
        "Заблокированный контакт",
        "ผู้ติดต่อที่ถูกบล็อก",
        "Liên hệ bị chặn"
    )

    private val legacyBlockedContactWithNamePrefixes = listOf(
        "Blocked Contact (",
        "封鎖聯絡人 (",
        "拦截联系人 (",
        "ブロックされた連絡先 (",
        "차단된 연락처 (",
        "Blockierter Kontakt (",
        "Contacto Bloqueado (",
        "Contact bloqué (",
        "Kontak Diblokir (",
        "Contatto Bloccato (",
        "Contato Bloqueado (",
        "Заблокированный контакт (",
        "ผู้ติดต่อที่ถูกบล็อก (",
        "Liên hệ bị chặn ("
    )

    /**
     * Resolves stored note tag or legacy string to currently active system language string resource.
     * Returns custom user notes unchanged.
     */
    fun getLocalizedNote(note: String?, context: Context): String? {
        if (note.isNullOrBlank()) return null

        val trimmed = note.trim()

        // 1. Tag resolution
        when {
            trimmed == TAG_AUTO_BLOCKED -> return context.getString(R.string.note_auto_blocked)
            trimmed == TAG_SYSTEM_BLOCK_LIST -> return context.getString(R.string.import_source_system_block_list)
            trimmed == TAG_BLOCKED_CONTACT -> return context.getString(R.string.import_source_blocked_contact)
            trimmed.startsWith(TAG_BLOCKED_CONTACT_WITH_NAME_PREFIX) -> {
                val contactName = trimmed.removePrefix(TAG_BLOCKED_CONTACT_WITH_NAME_PREFIX)
                return context.getString(R.string.import_source_blocked_contact_with_name, contactName)
            }
        }

        // 2. Legacy string mapping
        when {
            legacyAutoBlockedStrings.contains(trimmed) -> return context.getString(R.string.note_auto_blocked)
            legacySystemBlockListStrings.contains(trimmed) -> return context.getString(R.string.import_source_system_block_list)
            legacyBlockedContactStrings.contains(trimmed) -> return context.getString(R.string.import_source_blocked_contact)
        }

        for (prefix in legacyBlockedContactWithNamePrefixes) {
            if (trimmed.startsWith(prefix) && trimmed.endsWith(")")) {
                val contactName = trimmed.substring(prefix.length, trimmed.length - 1)
                return context.getString(R.string.import_source_blocked_contact_with_name, contactName)
            }
        }

        // 3. Custom user note
        return note
    }

    /**
     * Creates formatted tag for blocked contact with name.
     */
    fun buildBlockedContactWithNameTag(name: String): String {
        return "$TAG_BLOCKED_CONTACT_WITH_NAME_PREFIX$name"
    }
}
