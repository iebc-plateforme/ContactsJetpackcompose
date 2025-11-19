
import os
import xml.etree.ElementTree as ET
from deep_translator import GoogleTranslator

def get_strings_from_file(file_path):
    """Reads an XML strings file and returns a dictionary of string names and values."""
    strings = {}
    if not os.path.exists(file_path):
        return strings
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for string_tag in root.findall('string'):
            name = string_tag.get('name')
            value = string_tag.text
            strings[name] = value
    except ET.ParseError:
        print(f"Error parsing {file_path}")
    return strings

def translate_strings(strings_to_translate, dest_lang):
    """Translates a dictionary of strings to the destination language."""
    if not strings_to_translate:
        return {}
    
    # Correct language codes for the translator
    if dest_lang == "b+es+419":
        dest_lang = "es"  # Spanish (Latin America)
    elif dest_lang == "en-rGB":
        dest_lang = "en-GB" # English (Great Britain)
    elif dest_lang == "en-rIN":
        dest_lang = "en-IN" # English (India)
    elif dest_lang == "es-rUS":
        dest_lang = "es" # Spanish (United States)
    elif dest_lang == "hi-rIN":
        dest_lang = "hi" # Hindi (India)
    elif dest_lang == "ko-rKR":
        dest_lang = "ko" # Korean (South Korea)
    elif dest_lang == "nb-rNO":
        dest_lang = "no" # Norwegian Bokmål
    elif dest_lang == "pa-rPK":
        dest_lang = "pa" # Punjabi (Pakistan)
    elif dest_lang == "pt-rBR":
        dest_lang = "pt" # Portuguese (Brazil)
    elif dest_lang == "pt-rPT":
        dest_lang = "pt" # Portuguese (Portugal)
    elif dest_lang == "zh-rCN":
        dest_lang = "zh-CN" # Chinese (Simplified)
    elif dest_lang == "zh-rHK":
        dest_lang = "zh-TW" # Chinese (Traditional, Hong Kong)
    elif dest_lang == "zh-rTW":
        dest_lang = "zh-TW" # Chinese (Traditional, Taiwan)
    
    
    translated_strings = {}
    original_values = list(strings_to_translate.values())
    
    try:
        # The deep-translator library can also handle a list of strings
        translator = GoogleTranslator(source='auto', target=dest_lang)
        translations = translator.translate_batch(original_values)
        
        # translations will be a list of translated strings
        # or None if translation failed for all.
        if translations:
            for i, key in enumerate(strings_to_translate.keys()):
                translated_strings[key] = translations[i]
        else:
            print(f"Warning: Translation returned None for language {dest_lang}. Skipping.")
            return {}
            
    except Exception as e:
        print(f"Error translating to {dest_lang}: {e}")
        # Fallback to individual translation if batch fails
        try:
            for key, value in strings_to_translate.items():
                translated_strings[key] = translator.translate(value)
        except Exception as e2:
             print(f"Individual translation also failed for {dest_lang}: {e2}")
             return {}

    return translated_strings


def write_strings_to_file(file_path, strings):
    """Writes a dictionary of strings to an XML file."""
    root = ET.Element('resources')
    for name, value in strings.items():
        ET.SubElement(root, 'string', name=name).text = value
    tree = ET.ElementTree(root)
    tree.write(file_path, encoding='utf-8', xml_declaration=True)

def main():
    """Main function to translate missing strings in all language variants."""
    res_path = os.path.join('app', 'src', 'main', 'res')
    default_strings_path = os.path.join(res_path, 'values', 'strings.xml')
    
    if not os.path.exists(default_strings_path):
        print("Default strings.xml not found!")
        return

    default_strings = get_strings_from_file(default_strings_path)
    
    for dirname in os.listdir(res_path):
        if dirname.startswith('values-'):
            lang = dirname.split('values-')[1]
            if '-r' in lang:
                parts = lang.split('-r')
                lang = parts[0]
            
            lang_strings_path = os.path.join(res_path, dirname, 'strings.xml')
            lang_strings = get_strings_from_file(lang_strings_path)
            
            missing_strings = {}
            for name, value in default_strings.items():
                if name not in lang_strings:
                    missing_strings[name] = value
            
            if missing_strings:
                print(f"Translating {len(missing_strings)} missing strings for language: {lang}")
                translated = translate_strings(missing_strings, lang)
                
                # Combine existing and newly translated strings
                all_strings = {**lang_strings, **translated}

                # Preserve the order of default strings
                ordered_strings = {}
                for name in default_strings.keys():
                    if name in all_strings:
                        ordered_strings[name] = all_strings[name]

                # Add any strings that are not in the default file (e.g. comments or special strings for that language)
                for name, value in all_strings.items():
                    if name not in ordered_strings:
                        ordered_strings[name] = value

                write_strings_to_file(lang_strings_path, ordered_strings)
                print(f"Finished updating strings for language: {lang}")

if __name__ == '__main__':
    main()
