import xml.etree.ElementTree as ET
import os
from google.cloud import translate_v2 as translate

def get_strings_from_file(file_path):
    """Parses an XML file and returns a dictionary of string name -> value."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        strings = {elem.attrib['name']: elem.text for elem in root.findall('string')}
        return strings
    except ET.ParseError:
        print(f"Error parsing {file_path}")
        return {}

def translate_text(text, target_language):
    """Translates text to the target language using Google Translate API."""
    translate_client = translate.Client()
    result = translate_client.translate(text, target_language=target_language)
    return result['translatedText']

def main():
    base_strings_path = 'app/src/main/res/values/strings.xml'
    base_strings = get_strings_from_file(base_strings_path)
    
    res_path = 'app/src/main/res'
    for dir_name in os.listdir(res_path):
        if dir_name.startswith('values-'):
            locale = dir_name.split('values-')[1]
            if '-r' in locale:
                parts = locale.split('-r')
                language = parts[0]
                region = parts[1]
                target_language = f"{language}-{region}"
            else:
                target_language = locale

            locale_strings_path = os.path.join(res_path, dir_name, 'strings.xml')
            
            if not os.path.exists(locale_strings_path):
                print(f"No strings.xml in {dir_name}, skipping.")
                continue

            locale_strings = get_strings_from_file(locale_strings_path)
            
            missing_strings = {}
            for name, value in base_strings.items():
                if name not in locale_strings:
                    missing_strings[name] = value
            
            if not missing_strings:
                print(f"All strings translated for {locale}")
                continue

            print(f"Found {len(missing_strings)} missing strings for {locale}")

            # Translate missing strings
            translated_strings = {}
            for name, value in missing_strings.items():
                try:
                    translated_text = translate_text(value, target_language)
                    translated_strings[name] = translated_text
                    print(f"  {name}: {value} -> {translated_text}")
                except Exception as e:
                    print(f"Could not translate '{name}' for {locale}: {e}")

            # Append translated strings to the file
            try:
                tree = ET.parse(locale_strings_path)
                root = tree.getroot()
            except ET.ParseError:
                # If file is empty or malformed, create a new root
                root = ET.Element('resources')
                tree = ET.ElementTree(root)

            for name, value in translated_strings.items():
                new_string = ET.SubElement(root, 'string')
                new_string.set('name', name)
                new_string.text = value
            
            tree.write(locale_strings_path, encoding='utf-8', xml_declaration=True)
            print(f"Successfully updated {locale_strings_path}")

if __name__ == '__main__':
    main()