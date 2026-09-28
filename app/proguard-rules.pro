# Room Database keep rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Entity *;
}

# Gson Keep Rules for Backup Payload
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class io.github.hohojia886.callblocker.ui.settings.BackupPayload { *; }
-keep class io.github.hohojia886.callblocker.data.db.** { *; }

# Libphonenumber Keep Rules
-keep class com.google.i18n.phonenumbers.** { *; }

# DataStore & Coroutines
-dontwarn androidx.datastore.**
