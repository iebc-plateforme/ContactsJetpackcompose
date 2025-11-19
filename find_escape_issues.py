#!/usr/bin/env python3
import re

# Check specific files
files_to_check = [
    "app/src/main/res/values-az/strings.xml",
    "app/src/main/res/values-be/strings.xml"
]

output = []

for file_path in files_to_check:
    output.append(f"\nChecking: {file_path}")
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the privacy_security_desc string
    match = re.search(r'<string name="privacy_security_desc">(.*?)</string>', content, re.DOTALL)
    if match:
        text = match.group(1)
        output.append(f"privacy_security_desc: {repr(text)[:200]}")

    # Find merge strings
    for name in ["merge", "merge_contacts", "merge_contacts_description", "find_merge_duplicate_entries",
                 "merge_duplicate_contacts_title", "contacts_merged_successfully"]:
        match = re.search(rf'<string name="{name}">(.*?)</string>', content, re.DOTALL)
        if match:
            text = match.group(1)
            if '\\' in text:
                output.append(f"{name} contains backslash: {repr(text)[:100]}")

# Write to file
with open("escape_issues.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(output))

print("Results written to escape_issues.txt")
