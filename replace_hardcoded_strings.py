#!/usr/bin/env python3
"""
Replace hard-coded strings with string resources across all Kotlin files
"""

import os
import re
from pathlib import Path

# Mapping of hard-coded strings to their resource names
STRING_REPLACEMENTS = {
    # Photo Picker Dialog
    '"Add Photo"': 'R.string.add_photo',
    '"Change Photo"': 'R.string.change_photo_title',
    '"Take Photo"': 'R.string.take_photo',
    '"Capture using camera"': 'R.string.capture_using_camera',
    '"Choose from Gallery"': 'R.string.choose_from_gallery',
    '"Select from photos"': 'R.string.select_from_photos',
    '"Remove Photo"': 'R.string.remove_photo',
    '"Delete current photo"': 'R.string.delete_current_photo',

    # Address Fields (already done in EditContactFields.kt)
    '"Street"': 'R.string.street',
    '"City"': 'R.string.city',
    '"State"': 'R.string.state',
    '"Postal Code"': 'R.string.postal_code',
    '"Country"': 'R.string.country',
    '"Type"': 'R.string.type',

    # Filter & Sort
    '"By groups"': 'R.string.by_groups',
    '"Apply"': 'R.string.apply',
    '"Apply Filter"': 'R.string.apply_filter',
    '"First name"': 'R.string.first_name',
    '"Middle name"': 'R.string.middle_name',
    '"Surname"': 'R.string.surname',
    '"Full name"': 'R.string.full_name',
    '"Date created"': 'R.string.date_created',
    '"Date updated"': 'R.string.date_updated',
    '"Custom order"': 'R.string.custom_order',
    '"Ascending"': 'R.string.ascending',
    '"Descending"': 'R.string.descending',

    # Contact Actions
    '"Share contact"': 'R.string.share_contact',
    '"Message"': 'R.string.message',
    '"Error"': 'R.string.error',
    '"Call"': 'R.string.call',
    '"Contact deleted"': 'R.string.contact_deleted',
    '"Undo"': 'R.string.undo',

    # Contact Detail
    '"Websites"': 'R.string.websites',
    '"Instant Messages"': 'R.string.instant_messages',
    '"Important Dates"': 'R.string.important_dates',
    '"Ringtone"': 'R.string.ringtone',
    '"Open website"': 'R.string.open_website',
    '"Open app"': 'R.string.open_app',

    # Actions
    '"Send message"': 'R.string.send_message',
    '"Open in maps"': 'R.string.open_in_maps',
    '"Favorite"': 'R.string.favorite',
    '"Remove from favorites"': 'R.string.remove_from_favorites',
    '"Add to favorites"': 'R.string.add_to_favorites',

    # Selection Mode
    '"Exit selection mode"': 'R.string.exit_selection_mode',
    '"Deselect all"': 'R.string.deselect_all',
    '"Select all"': 'R.string.select_all',
    '"Delete selected contacts"': 'R.string.delete_selected_contacts',
    '"Share contacts"': 'R.string.share_contacts',
    '"Export as VCF"': 'R.string.export_as_vcf',
    '"Merge contacts"': 'R.string.merge_contacts',

    # Edit Contact Screen
    '"Fill required fields"': 'R.string.fill_required_fields',
    '"Birthday"': 'R.string.birthday',
    '"YYYY-MM-DD"': 'R.string.birthday_format',
    '"Dismiss"': 'R.string.dismiss',
    '"OK"': 'R.string.ok',
    '"Cancel"': 'R.string.action_cancel',
    '"Change photo"': 'R.string.change_photo',
    '"Pick date"': 'R.string.pick_date',

    # Favorites Screen
    '"Favorites"': 'R.string.favorites',
    '"Settings"': 'R.string.nav_settings',
    '"No favorite contacts"': 'R.string.favorites_empty_title',
    '"Mark contacts as favorites to see them here"': 'R.string.favorites_empty_description',
    '"Delete Contact"': 'R.string.contact_delete',

    # Dial Pad Screen
    '"Dial Pad"': 'R.string.dialpad_title',
    '"Back"': 'R.string.action_back',
    '"Enter phone number"': 'R.string.dialpad_enter_number',
    '"Backspace"': 'R.string.action_backspace',
    '"Clear all"': 'R.string.action_clear_all',

    # Main Screen
    '"Export contacts"': 'R.string.export_contacts_title',
    '"Import contacts"': 'R.string.import_contacts_title',
    '"Privacy Policy"': 'R.string.privacy_policy',
    '"Contacts"': 'R.string.nav_contacts',
    '"Groups"': 'R.string.nav_groups',
    '"Close search"': 'R.string.close_search',
    '"Clear"': 'R.string.action_clear',
    '"Search"': 'R.string.action_search',
    '"Filter"': 'R.string.action_filter',
    '"Sort"': 'R.string.action_sort',
    '"More options"': 'R.string.more_options',
    '"Launch dialer"': 'R.string.launch_dialer',

    # Settings Screen
    '"Select Color Theme"': 'R.string.select_color_theme',
    '"Select Theme Mode"': 'R.string.select_theme_mode',
    '"Select Language"': 'R.string.select_language',
    '"Select Font Size"': 'R.string.select_font_size',
    '"Default Tab"': 'R.string.default_tab_title',
    '"On Contact Clicked"': 'R.string.on_contact_clicked_title',
    '"Version 1.0.0"': 'R.string.version',
    '"Open Source Licenses"': 'R.string.open_source_licenses',
    '"Skip"': 'R.string.skip',
    '"Merge"': 'R.string.merge',
    '"Backup Now"': 'R.string.backup_now',
    '"Disable"': 'R.string.disable',
    '"Update"': 'R.string.update',
    '"Enable"': 'R.string.enable',
    '"Visible Contact Fields"': 'R.string.visible_contact_fields',
    '"Visible Tabs"': 'R.string.visible_tabs',
    '"No duplicate contacts found"': 'R.string.no_duplicate_contacts_found',
    '"Contacts merged successfully"': 'R.string.contacts_merged_successfully',
    '"Color theme"': 'R.string.color_theme',
    '"Theme mode"': 'R.string.settings_theme_mode',
    '"Language"': 'R.string.language',
    '"Font size"': 'R.string.font_size',

    # Permissions
    '"Camera Permission Required"': 'R.string.camera_permission_required',
    '"This app needs camera access to take photos for contacts. Please grant the permission."': 'R.string.camera_permission_description',
    '"Grant Permission"': 'R.string.grant_permission',
    '"Open Settings"': 'R.string.open_settings',

    # Quick Action Bar
    '"Email"': 'R.string.contact_email',

    # Content Descriptions
    '"Remove"': 'R.string.remove',
    '"Selected"': 'R.string.selected',

    # Types
    '"Home"': 'R.string.type_home',
    '"Work"': 'R.string.type_work',
    '"Other"': 'R.string.type_other',
    '"Custom"': 'R.string.type_custom',
    '"Mobile"': 'R.string.type_mobile',
    '"Fax"': 'R.string.type_fax',
    '"Pager"': 'R.string.type_pager',

    # Account Types
    '"Phone"': 'R.string.account_type_phone',
    '"Google"': 'R.string.account_type_google',
    '"WhatsApp"': 'R.string.account_type_whatsapp',
    '"Telegram"': 'R.string.account_type_telegram',
    '"Signal"': 'R.string.account_type_signal',
    '"Viber"': 'R.string.account_type_viber',
    '"Microsoft"': 'R.string.account_type_microsoft',
    '"Yahoo"': 'R.string.account_type_yahoo',
    '"SIM"': 'R.string.account_type_sim',

    # Font Sizes
    '"Small"': 'R.string.font_size_small',
    '"Medium"': 'R.string.font_size_medium',
    '"Large"': 'R.string.font_size_large',
    '"Extra Large"': 'R.string.font_size_extra_large',

    # Contact Click Actions
    '"View details"': 'R.string.view_details',
    '"Ask every time"': 'R.string.ask_every_time',
}


def replace_in_file(file_path):
    """Replace hard-coded strings in a single file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content
        modified = False

        # Check if file is a @Composable context (has stringResource available)
        is_composable = '@Composable' in content or 'stringResource' in content

        for old_string, resource_id in STRING_REPLACEMENTS.items():
            if old_string not in content:
                continue

            # Replace Text(...) with Text(stringResource(...))
            pattern = rf'Text\({re.escape(old_string)}\)'
            if re.search(pattern, content):
                replacement = f'Text(stringResource({resource_id}))'
                new_content = re.sub(pattern, replacement, content)
                if new_content != content:
                    content = new_content
                    modified = True
                    print(f"  Replaced {old_string} -> stringResource({resource_id})")

        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False

    except Exception as e:
        print(f"  Error processing {file_path}: {e}")
        return False


def main():
    """Main function to process all Kotlin files."""
    presentation_path = Path('app/src/main/java/com/contacts/android/contacts/presentation')

    if not presentation_path.exists():
        print(f"Error: Presentation path not found: {presentation_path}")
        return

    kt_files = list(presentation_path.rglob('*.kt'))
    print(f"Found {len(kt_files)} Kotlin files in presentation layer\n")

    modified_files = []

    for kt_file in kt_files:
        print(f"Processing: {kt_file.name}")
        if replace_in_file(kt_file):
            modified_files.append(kt_file)
            print(f"  [MODIFIED]\n")
        else:
            print(f"  - No changes\n")

    print("=" * 60)
    print(f"Replacement complete!")
    print(f"Modified {len(modified_files)} files out of {len(kt_files)} total files")
    print("=" * 60)

    if modified_files:
        print("\nModified files:")
        for f in modified_files:
            print(f"  - {f}")


if __name__ == '__main__':
    main()
