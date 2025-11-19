#!/usr/bin/env python3
import os
import re
import glob

# Define the path to the res directory
res_dir = "app/src/main/res"

# Find all strings.xml files
strings_files = glob.glob(f"{res_dir}/values-*/strings.xml")

def fix_string_content(content):
    """Fix common XML string issues"""
    # Replace single quotes with escaped version
    #content = content.replace("'", "\\'")
    # Actually, in Android XML, apostrophes should be escaped OR the whole string should use double quotes
    # But we need to be more careful

    # Pattern to match string tags
    pattern = r'(<string name="[^"]+">)(.*?)(</string>)'

    def fix_match(match):
        prefix = match.group(1)
        text = match.group(2)
        suffix = match.group(3)

        # In Android XML resources, apostrophes should be escaped using XML entities
        # First unescape any previously escaped apostrophes
        text = text.replace("\\'", "'")
        # Then escape using XML entity
        text = text.replace("'", "&#39;")

        return prefix + text + suffix

    content = re.sub(pattern, fix_match, content, flags=re.DOTALL)
    return content

print("Fixing strings.xml files...")
for file_path in strings_files:
    print(f"Processing: {file_path}")
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Fix XML declaration
        content = content.replace("<?xml version='1.0' encoding='utf-8'?>",
                                 '<?xml version="1.0" encoding="utf-8"?>')

        # Fix string content
        content = fix_string_content(content)

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

        print(f"  [OK] Fixed: {file_path}")
    except Exception as e:
        print(f"  [ERROR] Error fixing {file_path}: {e}")

print("Done!")
