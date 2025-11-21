import xml.etree.ElementTree as ET
import json

def find_missing_translations(default_strings_path, br_strings_path):
    # Parse the default strings.xml
    default_tree = ET.parse(default_strings_path)
    default_root = default_tree.getroot()
    default_strings = {string.get('name'): string.text for string in default_root.findall('string')}

    # Parse the Breton strings.xml
    try:
        br_tree = ET.parse(br_strings_path)
        br_root = br_tree.getroot()
        br_strings = {string.get('name') for string in br_root.findall('string')}
    except FileNotFoundError:
        br_strings = set()

    # Find missing strings
    missing_strings = {}
    for name, text in default_strings.items():
        if name not in br_strings:
            missing_strings[name] = text

    return missing_strings

if __name__ == '__main__':
    missing = find_missing_translations(
        'app/src/main/res/values/strings.xml',
        'app/src/main/res/values-br/strings.xml'
    )
    with open('missing_translations.json', 'w') as f:
        json.dump(missing, f, indent=4)
    print("Missing translations saved to missing_translations.json")