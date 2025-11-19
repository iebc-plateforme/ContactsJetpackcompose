#!/usr/bin/env python3
import re

files_to_fix = [
    "app/src/main/res/values-az/strings.xml",
    "app/src/main/res/values-be/strings.xml"
]

for file_path in files_to_fix:
    print(f"Fixing: {file_path}")

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix XML declaration
    content = content.replace("<?xml version='1.0' encoding='utf-8'?>",
                             '<?xml version="1.0" encoding="utf-8"?>')

    # Pattern to match string tags
    pattern = r'(<string name="[^"]+">)(.*?)(</string>)'

    def fix_match(match):
        prefix = match.group(1)
        text = match.group(2)
        suffix = match.group(3)

        # First, unescape any previously escaped characters
        text = text.replace("\\'", "'").replace("\\\\", "\\")

        # Now escape backslashes (must be done before apostrophes)
        text = text.replace("\\", "\\\\")

        # Then escape apostrophes using XML entity
        text = text.replace("'", "&#39;")

        return prefix + text + suffix

    content = re.sub(pattern, fix_match, content, flags=re.DOTALL)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"  Fixed: {file_path}")

print("Done!")
