
import xml.etree.ElementTree as ET
import re

def fix_breton_strings(br_strings_path):
    tree = ET.parse(br_strings_path)
    root = tree.getroot()

    for string in root.findall('string'):
        if string.text:
            # Escape apostrophes
            string.text = string.text.replace("'", "'\'")

            # Fix non-positional format strings
            placeholders = re.findall(r'%[sd]', string.text)
            if len(placeholders) > 1:
                count = 1
                def replace_placeholder(match):
                    nonlocal count
                    replacement = f'%{count}${match.group(0)[-1]}'
                    count += 1
                    return replacement
                string.text = re.sub(r'%[sd]', replace_placeholder, string.text)

    tree.write(br_strings_path, encoding='utf-8', xml_declaration=True)

if __name__ == '__main__':
    fix_breton_strings('app/src/main/res/values-br/strings.xml')
    print("Breton strings.xml fixed.")
