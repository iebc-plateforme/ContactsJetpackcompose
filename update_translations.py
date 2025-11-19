#!/usr/bin/env python3
"""
Script to update all language resource files with new string translations.
This adds all the new strings from the base strings.xml to all language files.
"""

import os
import xml.etree.ElementTree as ET
from pathlib import Path

# Base strings that need to be added to all language files
NEW_STRINGS_TEMPLATE = """
    <!-- Address Fields -->
    <string name="street">{street}</string>
    <string name="city">{city}</string>
    <string name="state">{state}</string>
    <string name="postal_code">{postal_code}</string>
    <string name="country">{country}</string>
    <string name="type">{type}</string>

    <!-- Filter & Sort -->
    <string name="by_groups">{by_groups}</string>
    <string name="apply">{apply}</string>
    <string name="apply_filter">{apply_filter}</string>
    <string name="first_name">{first_name}</string>
    <string name="middle_name">{middle_name}</string>
    <string name="surname">{surname}</string>
    <string name="full_name">{full_name}</string>
    <string name="date_created">{date_created}</string>
    <string name="date_updated">{date_updated}</string>
    <string name="custom_order">{custom_order}</string>
    <string name="ascending">{ascending}</string>
    <string name="descending">{descending}</string>

    <!-- Contact Actions -->
    <string name="contact_deleted">{contact_deleted}</string>
    <string name="undo">{undo}</string>
    <string name="fill_required_fields">{fill_required_fields}</string>
    <string name="change_photo">{change_photo}</string>
    <string name="birthday">{birthday}</string>
    <string name="birthday_format">YYYY-MM-DD</string>
    <string name="pick_date">{pick_date}</string>
    <string name="dismiss">{dismiss}</string>
    <string name="ok">{ok}</string>

    <!-- Settings - Appearance -->
    <string name="color_theme">{color_theme}</string>
    <string name="theme_mode_light">{theme_light}</string>
    <string name="theme_mode_dark">{theme_dark}</string>
    <string name="theme_mode_system">{theme_system}</string>
    <string name="language">{language}</string>
    <string name="font_size">{font_size}</string>

    <!-- Settings - Display -->
    <string name="manage_visible_contact_fields">{manage_fields}</string>
    <string name="choose_which_fields_display">{choose_fields}</string>
    <string name="manage_visible_tabs">{manage_tabs}</string>
    <string name="select_which_tabs_show">{select_tabs}</string>
    <string name="show_contact_thumbnails_title">{show_thumbnails}</string>
    <string name="display_contact_photos_lists">{display_photos}</string>
    <string name="show_phone_numbers_title">{show_phones}</string>
    <string name="display_phone_numbers_below">{display_phones_below}</string>
    <string name="start_name_with_surname">{start_surname}</string>
    <string name="display_last_name_first">{display_lastname}</string>
    <string name="show_private_contacts_title">{show_private}</string>
    <string name="display_contacts_marked_private">{display_private}</string>
    <string name="edge_to_edge_display">{edge_display}</string>
    <string name="extend_content_screen_edges">{extend_content}</string>

    <!-- Settings - Behavior -->
    <string name="default_tab">{default_tab}</string>
    <string name="on_contact_clicked">{on_contact_clicked}</string>
    <string name="show_dialpad_button_title">{show_dialpad}</string>
    <string name="display_quick_dial_button">{display_dialpad}</string>
    <string name="call_confirmation">{call_confirm}</string>
    <string name="ask_before_making_call">{ask_call}</string>
    <string name="swipe_delete_confirmation">{swipe_confirm}</string>
    <string name="ask_before_deleting_swiped">{ask_delete}</string>
    <string name="format_phone_numbers_title">{format_phones}</string>
    <string name="automatically_format_phone">{auto_format}</string>

    <!-- Settings - Contact Management -->
    <string name="show_only_contacts_with_phone_title">{only_with_phone}</string>
    <string name="hide_contacts_without_phone">{hide_no_phone}</string>
    <string name="show_duplicates_title">{show_dupes}</string>
    <string name="display_potentially_duplicate">{display_dupes}</string>
    <string name="import_contacts_title">{import_title}</string>
    <string name="import_from_vcard">{import_vcard}</string>
    <string name="export_contacts_title">{export_title}</string>
    <string name="export_to_vcard">{export_vcard}</string>
    <string name="merge_duplicate_contacts_title">{merge_title}</string>
    <string name="find_merge_duplicate_entries">{find_merge}</string>
    <string name="automatic_backups">{auto_backup}</string>
    <string name="schedule_regular_backups">{schedule_backup}</string>

    <!-- Settings - Privacy & About -->
    <string name="privacy_policy">{privacy}</string>
    <string name="view_our_privacy_policy">{view_privacy}</string>
    <string name="about">{about}</string>
    <string name="version">Version 1.0.0</string>
    <string name="open_source_licenses">{licenses}</string>
    <string name="view_third_party_licenses">{view_licenses}</string>

    <!-- Dialog Titles -->
    <string name="select_color_theme">{select_theme}</string>
    <string name="selected">{selected}</string>
    <string name="select_theme_mode">{select_mode}</string>
    <string name="use_light_theme">{use_light}</string>
    <string name="use_dark_theme">{use_dark}</string>
    <string name="follow_system_settings">{follow_system}</string>
    <string name="select_language">{select_lang}</string>
    <string name="select_font_size">{select_font}</string>
    <string name="default_tab_title">{default_tab_title}</string>
    <string name="on_contact_clicked_title">{on_click_title}</string>

    <!-- About Dialog -->
    <string name="contacts_app">{app_name}</string>
    <string name="app_description">{app_desc}</string>
    <string name="built_with">{built_with}</string>

    <!-- Privacy Policy -->
    <string name="privacy_policy_title">{privacy_title}</string>
    <string name="data_collection_storage">{data_collection}</string>
    <string name="privacy_data_desc">{privacy_data}</string>
    <string name="permissions_title">{perms_title}</string>
    <string name="privacy_permissions_desc">{perms_desc}</string>
    <string name="your_data">{your_data}</string>
    <string name="privacy_your_data_desc">{your_data_desc}</string>
    <string name="security_title">{security}</string>
    <string name="privacy_security_desc">{security_desc}</string>

    <!-- Open Source Licenses -->
    <string name="open_source_licenses_title">{licenses_title}</string>

    <!-- License Items -->
    <string name="license_jetpack_compose">Jetpack Compose</string>
    <string name="license_apache_2">Apache License 2.0</string>
    <string name="license_copyright_google">Copyright 2023 The Android Open Source Project</string>
    <string name="license_material_design">Material Design 3</string>
    <string name="license_kotlin">Kotlin</string>
    <string name="license_copyright_jetbrains">Copyright 2023 JetBrains s.r.o.</string>
    <string name="license_hilt">Dagger Hilt</string>
    <string name="license_room">Room Database</string>
    <string name="license_navigation">Navigation Compose</string>
    <string name="license_datastore">DataStore</string>
    <string name="license_accompanist">Accompanist</string>
    <string name="license_coil">Coil</string>
    <string name="license_copyright_coil">Copyright 2023 Coil Contributors</string>

    <!-- Visible Contact Fields -->
    <string name="visible_contact_fields">{visible_fields}</string>
    <string name="choose_fields_display">{choose_display}</string>
    <string name="contact_photos">{photos}</string>
    <string name="show_contact_thumbnails_lists">{show_thumb_lists}</string>
    <string name="phone_numbers_field">{phone_field}</string>
    <string name="display_phone_below_names">{display_below}</string>
    <string name="private_contacts">{private}</string>
    <string name="show_contacts_marked_private">{show_marked}</string>
    <string name="start_name_surname">{start_name}</string>
    <string name="display_last_name_first_format">{display_format}</string>

    <!-- Visible Tabs -->
    <string name="visible_tabs">{tabs}</string>
    <string name="manage_visible_ui_elements_title">{manage_ui}</string>
    <string name="contacts_tab">{contacts}</string>
    <string name="favorites_tab">{favorites}</string>
    <string name="recents_tab">{recents}</string>
    <string name="groups_tab">{groups}</string>
    <string name="dialpad_button">{dialpad}</string>
    <string name="show_quick_dial_button">{quick_dial}</string>

    <!-- Merge Duplicates -->
    <string name="duplicate_contacts_found">{dupes_found}</string>
    <string name="group_index_of_count">Group %1$d of %2$d</string>
    <string name="same_name_reason">{same_name}</string>
    <string name="same_phone_reason">{same_phone}</string>
    <string name="similar_names_reason">{similar_names}</string>
    <string name="select_contact_keep_master">{select_master}</string>
    <string name="skip">{skip}</string>
    <string name="merge">{merge}</string>

    <!-- Backup Configuration -->
    <string name="backup_configuration">{backup_config}</string>
    <string name="automatic_backup_enabled">{backup_enabled}</string>
    <string name="automatic_backup_disabled">{backup_disabled}</string>
    <string name="backup_frequency_label">{backup_freq}</string>
    <string name="backup_every_day">{backup_daily}</string>
    <string name="backup_every_week">{backup_weekly}</string>
    <string name="backup_every_month">{backup_monthly}</string>
    <string name="backup_now">{backup_now}</string>
    <string name="disable">{disable}</string>
    <string name="update">{update}</string>
    <string name="enable">{enable}</string>
"""

# Translation mappings for each language (populated from Fossify)
TRANSLATIONS = {
    "de": {
        "street": "Straße",
        "city": "Stadt",
        "state": "Bundesland",
        "postal_code": "Postleitzahl",
        "country": "Land",
        "type": "Typ",
        "by_groups": "Nach Gruppen",
        "apply": "Anwenden",
        "apply_filter": "Filter anwenden",
        "first_name": "Vorname",
        "middle_name": "Zweiter Vorname",
        "surname": "Nachname",
        "full_name": "Vollständiger Name",
        "date_created": "Erstellungsdatum",
        "date_updated": "Aktualisierungsdatum",
        "custom_order": "Benutzerdefinierte Reihenfolge",
        "ascending": "Aufsteigend",
        "descending": "Absteigend",
        "contact_deleted": "Kontakt gelöscht",
        "undo": "Rückgängig",
        "fill_required_fields": "Pflichtfelder ausfüllen",
        "change_photo": "Foto ändern",
        "birthday": "Geburtstag",
        "pick_date": "Datum wählen",
        "dismiss": "Schließen",
        "ok": "OK",
        # Add more German translations...
    },
    # Add more language mappings...
}

def update_language_file(lang_code, translations):
    """Update a language file with new translations"""
    res_dir = Path("app/src/main/res")
    lang_dir = res_dir / f"values-{lang_code}"
    strings_file = lang_dir / "strings.xml"

    if not strings_file.exists():
        print(f"Creating new strings.xml for {lang_code}")
        # Create new file
        create_new_strings_file(lang_dir, lang_code, translations)
    else:
        print(f"Updating existing strings.xml for {lang_code}")
        # Update existing file
        update_existing_strings_file(strings_file, translations)

def create_new_strings_file(lang_dir, lang_code, translations):
    """Create a new strings.xml file for a language"""
    lang_dir.mkdir(parents=True, exist_ok=True)
    strings_file = lang_dir / "strings.xml"

    # Generate content based on translations
    content = NEW_STRINGS_TEMPLATE.format(**translations)

    with open(strings_file, 'w', encoding='utf-8') as f:
        f.write('<resources>\n')
        f.write(content)
        f.write('</resources>\n')

def update_existing_strings_file(strings_file, translations):
    """Add new strings to an existing strings.xml file"""
    with open(strings_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Check if new strings are already added
    if '<string name="street">' in content:
        print(f"  Strings already updated, skipping")
        return

    # Find the closing </resources> tag and insert before it
    content = content.replace('</resources>', NEW_STRINGS_TEMPLATE.format(**translations) + '\n</resources>')

    with open(strings_file, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    print("Localization update script")
    print("This is a template - translations need to be added")
    print("Consider using the Fossify Contacts repository for complete translations")
