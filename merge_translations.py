
import xml.etree.ElementTree as ET
import sys

def main():
    if len(sys.argv) != 4:
        print("Usage: python merge_translations.py <base_file> <new_strings_file> <output_file>")
        sys.exit(1)

    base_file_path = sys.argv[1]
    new_strings_file_path = sys.argv[2]
    output_file_path = sys.argv[3]

    try:
        base_tree = ET.parse(base_file_path)
        base_root = base_tree.getroot()
    except (ET.ParseError, FileNotFoundError):
        base_root = ET.Element('resources')
        base_tree = ET.ElementTree(base_root)

    new_strings_tree = ET.parse(new_strings_file_path)
    new_strings_root = new_strings_tree.getroot()

    existing_string_names = {elem.attrib['name'] for elem in base_root.findall('string')}

    for new_string in new_strings_root.findall('string'):
        if new_string.attrib['name'] not in existing_string_names:
            base_root.append(new_string)

    base_tree.write(output_file_path, encoding='utf-8', xml_declaration=True)

if __name__ == '__main__':
    main()
