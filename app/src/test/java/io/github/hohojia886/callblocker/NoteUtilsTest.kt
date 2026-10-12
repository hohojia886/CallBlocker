package io.github.hohojia886.callblocker

import android.content.Context
import io.github.hohojia886.callblocker.util.NoteUtils
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoteUtilsTest {

    @Test
    fun testGetLocalizedNote_NullOrBlank() {
        val context = mockk<Context>()
        assertNull(NoteUtils.getLocalizedNote(null, context))
        assertNull(NoteUtils.getLocalizedNote("", context))
        assertNull(NoteUtils.getLocalizedNote("   ", context))
    }

    @Test
    fun testGetLocalizedNote_Tags() {
        val context = mockk<Context>()
        every { context.getString(R.string.note_auto_blocked) } returns "Auto-added rejected call"
        every { context.getString(R.string.import_source_system_block_list) } returns "System Block List"
        every { context.getString(R.string.import_source_blocked_contact) } returns "Blocked Contact"
        every { context.getString(R.string.import_source_blocked_contact_with_name, "John") } returns "Blocked Contact (John)"

        assertEquals("Auto-added rejected call", NoteUtils.getLocalizedNote(NoteUtils.TAG_AUTO_BLOCKED, context))
        assertEquals("System Block List", NoteUtils.getLocalizedNote(NoteUtils.TAG_SYSTEM_BLOCK_LIST, context))
        assertEquals("Blocked Contact", NoteUtils.getLocalizedNote(NoteUtils.TAG_BLOCKED_CONTACT, context))

        val contactTag = NoteUtils.buildBlockedContactWithNameTag("John")
        assertEquals("Blocked Contact (John)", NoteUtils.getLocalizedNote(contactTag, context))
    }

    @Test
    fun testGetLocalizedNote_LegacyStrings() {
        val context = mockk<Context>()
        every { context.getString(R.string.note_auto_blocked) } returns "自動封鎖拒接電話"
        every { context.getString(R.string.import_source_system_block_list) } returns "系統封鎖名單"
        every { context.getString(R.string.import_source_blocked_contact_with_name, "Alice") } returns "封鎖聯絡人 (Alice)"

        // Legacy English string mapped to current active locale (Traditional Chinese)
        assertEquals("自動封鎖拒接電話", NoteUtils.getLocalizedNote("Auto-added rejected call", context))
        assertEquals("系統封鎖名單", NoteUtils.getLocalizedNote("System Block List", context))
        assertEquals("封鎖聯絡人 (Alice)", NoteUtils.getLocalizedNote("Blocked Contact (Alice)", context))
    }

    @Test
    fun testGetLocalizedNote_CustomUserNote() {
        val context = mockk<Context>()
        val customNote = "Telemarketer Insurance Scam"
        assertEquals(customNote, NoteUtils.getLocalizedNote(customNote, context))
    }

    @Test
    fun testGetLocalizedNote_MultilingualLegacyStrings() {
        val context = mockk<Context>()
        every { context.getString(R.string.note_auto_blocked) } returns "Auto-added rejected call"
        every { context.getString(R.string.import_source_system_block_list) } returns "System Block List"
        every { context.getString(R.string.import_source_blocked_contact_with_name, "Bob") } returns "Blocked Contact (Bob)"

        // Japanese legacy string mapped to active English locale
        assertEquals("Auto-added rejected call", NoteUtils.getLocalizedNote("自動拒否された電話", context))
        // German legacy string mapped to active English locale
        assertEquals("System Block List", NoteUtils.getLocalizedNote("System-Sperrliste", context))
        // Spanish legacy contact with name mapped to active English locale
        assertEquals("Blocked Contact (Bob)", NoteUtils.getLocalizedNote("Contacto Bloqueado (Bob)", context))
    }
}
