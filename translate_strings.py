
import json
import xml.etree.ElementTree as ET
from deep_translator import GoogleTranslator

def translate_and_update(missing_translations_path, br_strings_path):
    # Read the missing translations
    with open(missing_translations_path, 'r') as f:
        missing_translations = json.load(f)

    # Prepare the translator
    translator = GoogleTranslator(source='en', target='br')

    # Read the existing Breton strings.xml
    try:
        tree = ET.parse(br_strings_path)
        root = tree.getroot()
    except FileNotFoundError:
        root = ET.Element('resources')
        tree = ET.ElementTree(root)

    # Translate and add the missing strings
    for name, text in missing_translations.items():
        try:
            # Handle placeholders like %s, %d, etc.
            if '%' in text:
                # The translator might mess up the placeholders, so we just copy the original text
                translated_text = text
            else:
                translated_text = translator.translate(text)
            
            # Create a new string element
            new_string = ET.Element('string', name=name)
            new_string.text = translated_text
            root.append(new_string)

        except Exception as e:
            print(f"Error translating '{text}': {e}")
            # If translation fails, add the English text as a fallback
            new_string = ET.Element('string', name=name)
            new_string.text = text
            root.append(new_string)

    # Write the updated strings.xml
    tree.write(br_strings_path, encoding='utf-8', xml_declaration=True)

if __name__ == '__main__':
    translate_and_update(
        'missing_translations.json',
        'app/src/main/res/values-br/strings.xml'
    )
    print("Breton strings.xml updated successfully.")
