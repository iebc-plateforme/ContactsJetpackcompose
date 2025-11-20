
import xml.etree.ElementTree as ET
import sys

def get_strings_from_file(file_path):
    """Parses an XML file and returns a set of string names."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        return {elem.attrib['name'] for elem in root.findall('string')}
    except (ET.ParseError, FileNotFoundError):
        return set()

def get_full_strings_from_file(file_path):
    """Parses an XML file and returns a dictionary of string name -> value."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        return {elem.attrib['name']: elem.text for elem in root.findall('string')}
    except (ET.ParseError, FileNotFoundError):
        return {}

def main():
    if len(sys.argv) != 3:
        print("Usage: python update_translations.py <base_strings.xml> <locale_strings.xml>")
        sys.exit(1)

    base_strings_path = sys.argv[1]
    locale_strings_path = sys.argv[2]

    base_strings_map = get_full_strings_from_file(base_strings_path)
    locale_strings_set = get_strings_from_file(locale_strings_path)

    missing_strings = {name: value for name, value in base_strings_map.items() if name not in locale_strings_set}

    if not missing_strings:
        return

    print("<!-- Missing translations -->")
    for name, value in missing_strings.items():
        translated_value = f"NEEDS TRANSLATION: {value}"
        print(f'    <string name="{name}">{translated_value}</string>')

if __name__ == '__main__':
    main()
