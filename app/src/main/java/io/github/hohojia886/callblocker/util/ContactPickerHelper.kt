package io.github.hohojia886.callblocker.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import io.github.hohojia886.callblocker.data.db.AppDatabase

object ContactPickerHelper {

    data class ContactDetails(
        val name: String,
        val phoneNumber: String
    )

    fun getContactDetails(context: Context, contactUri: Uri): ContactDetails? {
        var name = ""
        var phoneNumber = ""

        val cursor = context.contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = if (idIndex >= 0) it.getString(idIndex) else ""
                name = if (nameIndex >= 0) it.getString(nameIndex) else ""
                val hasPhoneNumber = if (hasPhoneIndex >= 0) it.getInt(hasPhoneIndex) > 0 else false

                if (hasPhoneNumber) {
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { pCursor ->
                        if (pCursor.moveToFirst()) {
                            val numberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            phoneNumber = if (numberIndex >= 0) pCursor.getString(numberIndex) else ""
                        }
                    }
                }
            }
        }

        return if (phoneNumber.isNotBlank()) ContactDetails(name, phoneNumber) else null
    }

    /**
     * Checks if the incoming E.164 number is in the contacts cache.
     * Uses Room database instead of querying ContactsContract directly to improve performance during call screening.
     */
    suspend fun isNumberInContacts(context: Context, e164Number: String): Boolean {
        if (e164Number.isBlank() || e164Number == "UNKNOWN" || e164Number == "*") return false
        val db = AppDatabase.getInstance(context)
        return db.contactsCacheDao().containsNumber(e164Number)
    }
}
